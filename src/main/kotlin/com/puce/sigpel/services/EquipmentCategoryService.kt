package com.puce.sigpel.services

import com.puce.sigpel.dto.EquipmentCategoryRequest
import com.puce.sigpel.entities.EquipmentCategory
import com.puce.sigpel.exceptions.DuplicateResourceException
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.EquipmentCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EquipmentCategoryService(
    private val equipmentCategoryRepository: EquipmentCategoryRepository
) {
    @Transactional(readOnly = true)
    fun list(): List<EquipmentCategory> = equipmentCategoryRepository.findAll()

    @Transactional(readOnly = true)
    fun get(id: Long): EquipmentCategory =
        equipmentCategoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Category $id not found") }

    fun create(request: EquipmentCategoryRequest): EquipmentCategory {
        if (equipmentCategoryRepository.existsByNameIgnoreCase(request.name)) {
            throw DuplicateResourceException("A category named '${request.name}' already exists")
        }
        return equipmentCategoryRepository.save(EquipmentCategory(name = request.name))
    }

    fun update(id: Long, request: EquipmentCategoryRequest): EquipmentCategory {
        val category = get(id)
        if (!category.name.equals(request.name, ignoreCase = true) &&
            equipmentCategoryRepository.existsByNameIgnoreCase(request.name)
        ) {
            throw DuplicateResourceException("A category named '${request.name}' already exists")
        }
        category.name = request.name
        return equipmentCategoryRepository.save(category)
    }

    fun delete(id: Long) {
        equipmentCategoryRepository.delete(get(id))
    }
}
