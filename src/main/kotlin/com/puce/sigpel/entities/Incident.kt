package com.puce.sigpel.entities

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
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "incidents")
class Incident(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // A loan can have several incidents over time (damage, loss, delay), so this is
    // the "many" side of a 1:N relationship, not a 1:1.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    var loan: Loan,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: IncidentType,

    @Column(length = 255)
    var description: String? = null,

    @Column(name = "report_date", nullable = false)
    var reportDate: Instant = Instant.now()
)
