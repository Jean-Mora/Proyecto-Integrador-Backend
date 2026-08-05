package com.puce.sigpel.services

import com.puce.sigpel.config.CurrentUser
import com.puce.sigpel.dto.LoanRequest
import com.puce.sigpel.entities.Equipment
import com.puce.sigpel.entities.EquipmentCategory
import com.puce.sigpel.entities.EquipmentStatus
import com.puce.sigpel.entities.Loan
import com.puce.sigpel.entities.LoanStatus
import com.puce.sigpel.exceptions.EquipmentNotAvailableException
import com.puce.sigpel.exceptions.ForbiddenOperationException
import com.puce.sigpel.repositories.LoanAuditRepository
import com.puce.sigpel.repositories.LoanRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class LoanServiceTest {

    private val loanRepository = mockk<LoanRepository>()
    private val equipmentService = mockk<EquipmentService>()
    private val loanAuditRepository = mockk<LoanAuditRepository>(relaxed = true)
    private lateinit var loanService: LoanService
    private lateinit var availableEquipment: Equipment

    @BeforeEach
    fun setUp() {
        loanService = LoanService(loanRepository, equipmentService, loanAuditRepository)
        mockkObject(CurrentUser)
        every { CurrentUser.username() } returns "student01"

        availableEquipment = Equipment(
            id = 1L,
            category = EquipmentCategory(id = 1L, name = "Electronics"),
            name = "Multimeter",
            status = EquipmentStatus.AVAILABLE
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(CurrentUser)
    }

    @Test
    fun `request creates the loan and marks the equipment as LOANED when it is available`() {
        every { equipmentService.get(1L) } returns availableEquipment
        val savedSlot = slot<Loan>()
        every { loanRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        loanService.request(LoanRequest(equipmentId = 1L))

        assertEquals(EquipmentStatus.LOANED, availableEquipment.status)
        assertEquals("student01", savedSlot.captured.studentUser)
        verify(exactly = 1) { loanRepository.save(any()) }
    }

    @Test
    fun `request throws EquipmentNotAvailableException when the equipment is already loaned`() {
        availableEquipment.status = EquipmentStatus.LOANED
        every { equipmentService.get(1L) } returns availableEquipment

        assertThrows(EquipmentNotAvailableException::class.java) {
            loanService.request(LoanRequest(equipmentId = 1L))
        }
        verify(exactly = 0) { loanRepository.save(any()) }
    }

    @Test
    fun `cancel throws ForbiddenOperationException when the loan does not belong to the user`() {
        val someoneElsesLoan = Loan(
            id = 5L,
            equipment = availableEquipment,
            studentUser = "other_student",
            status = LoanStatus.PENDING
        )
        every { loanRepository.findById(5L) } returns Optional.of(someoneElsesLoan)

        assertThrows(ForbiddenOperationException::class.java) {
            loanService.cancel(5L)
        }
        verify(exactly = 0) { loanRepository.delete(any()) }
    }

    @Test
    fun `cancel throws ForbiddenOperationException when the loan is no longer pending`() {
        val approvedLoan = Loan(
            id = 7L,
            equipment = availableEquipment,
            studentUser = "student01",
            status = LoanStatus.APPROVED
        )
        every { loanRepository.findById(7L) } returns Optional.of(approvedLoan)

        assertThrows(ForbiddenOperationException::class.java) {
            loanService.cancel(7L)
        }
    }

    @Test
    fun `cancel deletes the loan and frees the equipment when it belongs to the owner and is pending`() {
        availableEquipment.status = EquipmentStatus.LOANED
        val ownLoan = Loan(
            id = 6L,
            equipment = availableEquipment,
            studentUser = "student01",
            status = LoanStatus.PENDING
        )
        every { loanRepository.findById(6L) } returns Optional.of(ownLoan)
        every { loanRepository.delete(ownLoan) } returns Unit

        loanService.cancel(6L)

        assertEquals(EquipmentStatus.AVAILABLE, availableEquipment.status)
        verify(exactly = 1) { loanRepository.delete(ownLoan) }
    }

    @Test
    fun `request throws IllegalArgumentException when the estimated return date is before or equal to the current moment`() {
        val invalidRequest = LoanRequest(
            equipmentId = 1L,
            estimatedReturnDate = java.time.Instant.now().minusSeconds(3600) // One hour in the past (invalid)
        )

        assertThrows(IllegalArgumentException::class.java) {
            loanService.request(invalidRequest)
        }

        verify(exactly = 0) { equipmentService.get(any()) }
        verify(exactly = 0) { loanRepository.save(any()) }
    }
}
