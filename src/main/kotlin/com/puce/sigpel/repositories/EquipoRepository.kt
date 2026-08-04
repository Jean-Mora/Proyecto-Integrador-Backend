package com.puce.sigpel.repositories

import com.puce.sigpel.entities.Equipo
import com.puce.sigpel.entities.EstadoEquipo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EquipoRepository : JpaRepository<Equipo, Long> {
    fun findByEstado(estado: EstadoEquipo): List<Equipo>

    // join fetch evita el N+1 al mapear categoria.nombre por cada equipo (ver toResponse()).
    @Query("select e from Equipo e join fetch e.categoria")
    fun findAllWithCategoria(): List<Equipo>

    @Query("select e from Equipo e join fetch e.categoria where e.estado = :estado")
    fun findByEstadoWithCategoria(estado: EstadoEquipo): List<Equipo>

    // HU-23: filtro combinado por categoría y/o estado (ambos opcionales)
    @Query(
        """
        select e from Equipo e
        join fetch e.categoria c
        where (:categoriaId is null or c.id = :categoriaId)
          and (:estado is null or e.estado = :estado)
        """
    )
    fun findByCategoriaAndEstado(
        @Param("categoriaId") categoriaId: Long?,
        @Param("estado") estado: EstadoEquipo?
    ): List<Equipo>
}