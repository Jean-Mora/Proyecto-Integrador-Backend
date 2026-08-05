package com.puce.sigpel.services

import com.puce.sigpel.dto.IncidentRequest
import com.puce.sigpel.entities.Incident
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.IncidentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IncidentService(
    private val incidentRepository: IncidentRepository,
    private val loanService: LoanService
) {
    @Transactional(readOnly = true)
    fun get(id: Long): Incident =
        incidentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Incident $id not found") }

    /** The loan-incident relationship is 1:N: a loan can have several incidents. */
    fun register(request: IncidentRequest): Incident {
        val loan = loanService.get(request.loanId)
        val incident = Incident(
            loan = loan,
            type = request.type,
            description = request.description
        )
        return incidentRepository.save(incident)
    }

    fun update(id: Long, request: IncidentRequest): Incident {
        val incident = get(id)
        incident.type = request.type
        incident.description = request.description
        return incidentRepository.save(incident)
    }

    fun delete(id: Long) {
        incidentRepository.delete(get(id))
    }
}
