package com.puce.labloan.entities

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
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "prestamos")
class Prestamo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false)
    var equipo: Equipo,

    // Username tomado del JWT de Cognito. NO es FK: no existe tabla de usuarios
    // en este backend, los usuarios y roles viven en Cognito.
    @Column(name = "estudiante_user", nullable = false, length = 60)
    var estudianteUser: String,

    @Column(name = "fecha_solicitud", nullable = false)
    var fechaSolicitud: Instant = Instant.now(),

    @Column(name = "fecha_devolucion_estimada")
    var fechaDevolucionEstimada: Instant? = null,

    @Column(name = "fecha_devolucion_real")
    var fechaDevolucionReal: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var estado: EstadoPrestamo = EstadoPrestamo.PENDIENTE,

    @Column(length = 255)
    var comentario: String? = null,

    // Relacion 1:1 con incidencias (un prestamo tiene como maximo una incidencia).
    @OneToOne(mappedBy = "prestamo", cascade = [CascadeType.ALL], orphanRemoval = true)
    var incidencia: Incidencia? = null
)
