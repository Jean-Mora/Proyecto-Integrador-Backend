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
import jakarta.persistence.Version

@Entity
@Table(name = "equipment")
class Equipment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: EquipmentCategory,

    @Column(nullable = false, length = 80)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: EquipmentStatus = EquipmentStatus.AVAILABLE,

    @Column(length = 255)
    var description: String? = null,

    // Optimistic locking: protects against two students requesting the same
    // equipment at the same time. If two transactions read the same version and
    // both try to save, the second one fails with ObjectOptimisticLockingFailureException
    // (translated to 409 Conflict in GlobalExceptionHandler).
    @Version
    var version: Long? = null
)
