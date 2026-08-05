package com.puce.sigpel.repositories

import com.puce.sigpel.entities.EquipmentCategory
import org.springframework.data.jpa.repository.JpaRepository

interface EquipmentCategoryRepository : JpaRepository<EquipmentCategory, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
}
