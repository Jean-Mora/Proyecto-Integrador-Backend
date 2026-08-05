package com.puce.sigpel.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "loan_audit")
data class LoanAudit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "loan_id", nullable = false)
    val loanId: Long,

    @Column(nullable = false)
    val previousStatus: String,

    @Column(nullable = false)
    val newStatus: String,

    @Column(nullable = false)
    val modifiedBy: String, // Stores the email or username of the "STAFF" (ENCARGADO)

    @Column(nullable = false)
    val modificationDate: LocalDateTime = LocalDateTime.now()
)
