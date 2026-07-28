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
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "incidencias")
class Incidencia(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // FK + UK: prestamo_id es unico para que la relacion 1:1 se respete tambien
    // a nivel de base de datos, no solo en el mapeo de JPA.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestamo_id", nullable = false, unique = true)
    var prestamo: Prestamo,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var tipo: TipoIncidencia,

    @Column(length = 255)
    var descripcion: String? = null,

    @Column(name = "fecha_reporte", nullable = false)
    var fechaReporte: Instant = Instant.now()
)
