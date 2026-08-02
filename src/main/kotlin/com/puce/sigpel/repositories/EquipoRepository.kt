package com.puce.sigpel.repositories

import com.puce.sigpel.entities.Equipo
import org.springframework.data.jpa.repository.JpaRepository

interface EquipoRepository : JpaRepository<Equipo, Long>
