package com.puce.sigpel.services

import com.puce.sigpel.config.CurrentUser
import com.puce.sigpel.dto.LoanRequest
import com.puce.sigpel.dto.LoanStatusRequest
import com.puce.sigpel.entities.EquipmentStatus
import com.puce.sigpel.entities.Loan
import com.puce.sigpel.entities.LoanAudit
import com.puce.sigpel.entities.LoanStatus
import com.puce.sigpel.exceptions.EquipmentNotAvailableException
import com.puce.sigpel.exceptions.ForbiddenOperationException
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.LoanAuditRepository
import com.puce.sigpel.repositories.LoanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class LoanService(
    private val loanRepository: LoanRepository,
    private val equipmentService: EquipmentService,
    private val loanAuditRepository: LoanAuditRepository
) {
    @Transactional(readOnly = true)
    fun get(id: Long): Loan =
        loanRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Loan $id not found") }

    @Transactional(readOnly = true)
    fun listMine(): List<Loan> =
        loanRepository.findByStudentUser(CurrentUser.username())

    /** Allows STAFF (ENCARGADO) to see every loan in the system (HU-22). */
    @Transactional(readOnly = true)
    fun listAll(): List<Loan> =
        loanRepository.findAll()

    fun request(request: LoanRequest): Loan {
        if (request.estimatedReturnDate?.isBefore(Instant.now()) == true) {
            throw IllegalArgumentException("The estimated return date must be after the current date")
        }

        val equipment = equipmentService.get(request.equipmentId)
        if (equipment.status != EquipmentStatus.AVAILABLE) {
            throw EquipmentNotAvailableException("Equipment '${equipment.name}' is not available")
        }
        equipment.status = EquipmentStatus.LOANED

        val loan = Loan(
            equipment = equipment,
            studentUser = CurrentUser.username(),
            estimatedReturnDate = request.estimatedReturnDate
        )
        return loanRepository.save(loan)
    }

    /** STAFF (ENCARGADO) approves, rejects or marks a loan as returned. */
    fun changeStatus(id: Long, request: LoanStatusRequest): Loan {
        val loan = get(id)

        // Keep the previous status for the audit trail (HU-25)
        val previousStatusStr = loan.status.name
        val newStatusStr = request.status.name

        loan.status = request.status
        request.comment?.let { loan.comment = it }

        when (request.status) {
            LoanStatus.REJECTED -> loan.equipment.status = EquipmentStatus.AVAILABLE
            LoanStatus.RETURNED -> {
                loan.equipment.status = EquipmentStatus.AVAILABLE
                loan.actualReturnDate = Instant.now()
            }
            else -> Unit
        }

        val savedLoan = loanRepository.save(loan)

        // --- HU-25: Record the status change audit trail with date and user ---
        val audit = LoanAudit(
            loanId = savedLoan.id!!,
            previousStatus = previousStatusStr,
            newStatus = newStatusStr,
            modifiedBy = CurrentUser.username() // Automatically picks up the logged-in STAFF user
        )
        loanAuditRepository.save(audit)

        return savedLoan
    }

    fun cancel(id: Long) {
        val loan = get(id)
        if (loan.studentUser != CurrentUser.username()) {
            throw ForbiddenOperationException("You cannot cancel a loan that is not yours")
        }
        if (loan.status != LoanStatus.PENDING) {
            throw ForbiddenOperationException("A loan can only be cancelled while it is PENDING")
        }
        loan.equipment.status = EquipmentStatus.AVAILABLE
        loanRepository.delete(loan)
    }
}
