package com.puce.sigpel.repositories

import com.puce.sigpel.entities.Prestamo
import org.springframework.data.jpa.repository.JpaRepository

interface PrestamoRepository : JpaRepository<Prestamo, Long>
