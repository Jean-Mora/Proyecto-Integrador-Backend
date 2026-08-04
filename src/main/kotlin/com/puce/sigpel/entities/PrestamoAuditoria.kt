package com.puce.sigpel.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "prestamo_auditoria")
data class PrestamoAuditoria(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "prestamo_id", nullable = false)
    val prestamoId: Long,

    @Column(nullable = false)
    val estadoAnterior: String,

    @Column(nullable = false)
    val estadoNuevo: String,

    @Column(nullable = false)
    val modificadoPor: String, // Aquí guardaremos el correo o username del "ENCARGADO"

    @Column(nullable = false)
    val fechaModificacion: LocalDateTime = LocalDateTime.now()
)