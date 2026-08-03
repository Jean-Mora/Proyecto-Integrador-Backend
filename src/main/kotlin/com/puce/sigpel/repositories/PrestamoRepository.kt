package com.puce.sigpel.repositories

import com.puce.sigpel.entities.Prestamo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PrestamoRepository : JpaRepository<Prestamo, Long> {
    // join fetch evita el N+1 al mapear equipo.nombre por cada prestamo (ver toResponse()).
    @Query("select p from Prestamo p join fetch p.equipo where p.estudianteUser = :estudianteUser")
    fun findByEstudianteUser(estudianteUser: String): List<Prestamo>
}
