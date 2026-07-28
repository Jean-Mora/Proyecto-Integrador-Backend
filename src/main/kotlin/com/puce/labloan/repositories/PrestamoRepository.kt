package com.puce.labloan.repositories

import com.puce.labloan.entities.Prestamo
import org.springframework.data.jpa.repository.JpaRepository

interface PrestamoRepository : JpaRepository<Prestamo, Long> {
    fun findByEstudianteUser(estudianteUser: String): List<Prestamo>
}
