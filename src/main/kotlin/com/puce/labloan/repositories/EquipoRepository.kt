package com.puce.labloan.repositories

import com.puce.labloan.entities.Equipo
import com.puce.labloan.entities.EstadoEquipo
import org.springframework.data.jpa.repository.JpaRepository

interface EquipoRepository : JpaRepository<Equipo, Long> {
    fun findByEstado(estado: EstadoEquipo): List<Equipo>
}
