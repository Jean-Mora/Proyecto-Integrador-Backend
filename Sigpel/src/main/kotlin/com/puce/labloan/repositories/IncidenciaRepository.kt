package com.puce.labloan.repositories

import com.puce.labloan.entities.Incidencia
import org.springframework.data.jpa.repository.JpaRepository

interface IncidenciaRepository : JpaRepository<Incidencia, Long> {
    fun existsByPrestamoId(prestamoId: Long): Boolean
}
