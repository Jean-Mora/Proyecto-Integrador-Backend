package com.puce.sigpel.repositories

import com.puce.sigpel.entities.Loan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LoanRepository : JpaRepository<Loan, Long> {
    // join fetch avoids N+1 when mapping equipment.name for each loan (see toResponse()).
    @Query("select l from Loan l join fetch l.equipment where l.studentUser = :studentUser")
    fun findByStudentUser(studentUser: String): List<Loan>
}
