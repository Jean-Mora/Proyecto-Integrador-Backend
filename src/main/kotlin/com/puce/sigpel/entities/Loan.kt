package com.puce.sigpel.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "loans")
class Loan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    var equipment: Equipment,

    // Username taken from the Cognito JWT. NOT a FK: there is no users table
    // in this backend, users and roles live in Cognito.
    @Column(name = "student_user", nullable = false, length = 60)
    var studentUser: String,

    @Column(name = "request_date", nullable = false)
    var requestDate: Instant = Instant.now(),

    @Column(name = "estimated_return_date")
    var estimatedReturnDate: Instant? = null,

    @Column(name = "actual_return_date")
    var actualReturnDate: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: LoanStatus = LoanStatus.PENDING,

    @Column(length = 255)
    var comment: String? = null,

    // 1:N relationship with incidents (a loan can have several incidents).
    @OneToMany(mappedBy = "loan", cascade = [CascadeType.ALL], orphanRemoval = true)
    var incidents: MutableList<Incident> = mutableListOf()
)
