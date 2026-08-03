package com.puce.sigpel.repositories

import com.puce.sigpel.entities.Incidencia
import org.springframework.data.jpa.repository.JpaRepository

interface IncidenciaRepository : JpaRepository<Incidencia, Long> {
    fun existsByPrestamoId(prestamoId: Long): Boolean
}
