package com.puce.sigpel.repositories

import com.puce.sigpel.entities.LoanAudit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoanAuditRepository : JpaRepository<LoanAudit, Long> {
    fun findByLoanId(loanId: Long): List<LoanAudit>
}
