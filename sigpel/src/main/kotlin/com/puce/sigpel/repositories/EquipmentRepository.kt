package com.puce.sigpel.repositories

import com.puce.sigpel.entities.Equipment
import com.puce.sigpel.entities.EquipmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EquipmentRepository : JpaRepository<Equipment, Long> {
    fun findByStatus(status: EquipmentStatus): List<Equipment>

    fun existsBySerialNumber(serialNumber: String): Boolean

    // join fetch avoids N+1 when mapping category.name for each equipment item (see toResponse()).
    @Query("select e from Equipment e join fetch e.category")
    fun findAllWithCategory(): List<Equipment>

    @Query("select e from Equipment e join fetch e.category where e.status = :status")
    fun findByStatusWithCategory(status: EquipmentStatus): List<Equipment>

    // HU-23: combined filter by category and/or status (both optional)
    @Query(
        """
        select e from Equipment e
        join fetch e.category c
        where (:categoryId is null or c.id = :categoryId)
          and (:status is null or e.status = :status)
        """
    )
    fun findByCategoryAndStatus(
        @Param("categoryId") categoryId: Long?,
        @Param("status") status: EquipmentStatus?
    ): List<Equipment>
}
