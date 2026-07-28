package com.puce.labloan.entities

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
@Table(name = "equipos")
class Equipo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    var categoria: CategoriaEquipo,

    @Column(nullable = false, length = 80)
    var nombre: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var estado: EstadoEquipo = EstadoEquipo.DISPONIBLE,

    @Column(length = 255)
    var descripcion: String? = null,

    // Optimistic locking: protege contra dos estudiantes solicitando el mismo
    // equipo al mismo tiempo. Si dos transacciones leen la misma version y ambas
    // intentan guardar, la segunda falla con ObjectOptimisticLockingFailureException
    // (se traduce a 409 Conflict en GlobalExceptionHandler).
    @Version
    var version: Long? = null
)
