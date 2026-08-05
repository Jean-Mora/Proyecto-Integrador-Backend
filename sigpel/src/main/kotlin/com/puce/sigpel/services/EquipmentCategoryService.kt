package com.puce.sigpel.services

import com.puce.sigpel.dto.EquipmentCategoryRequest
import com.puce.sigpel.entities.EquipmentCategory
import com.puce.sigpel.exceptions.DuplicateResourceException
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.EquipmentCategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EquipmentCategoryService(
    private val equipmentCategoryRepository: EquipmentCategoryRepository
) {
    private val log = LoggerFactory.getLogger(EquipmentCategoryService::class.java)

    @Transactional(readOnly = true)
    fun list(): List<EquipmentCategory> = equipmentCategoryRepository.findAll()

    @Transactional(readOnly = true)
    fun get(id: Long): EquipmentCategory =
        equipmentCategoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Category $id not found") }

    fun create(request: EquipmentCategoryRequest): EquipmentCategory {
        if (equipmentCategoryRepository.existsByNameIgnoreCase(request.name)) {
            log.warn("event=category.rejected | msg=Duplicate category name | name=\"${request.name}\"")
            throw DuplicateResourceException("A category named '${request.name}' already exists")
        }
        val saved = equipmentCategoryRepository.save(EquipmentCategory(name = request.name))
        log.info("event=category.created | msg=Category created | categoryId=${saved.id} name=\"${saved.name}\"")
        return saved
    }

    fun update(id: Long, request: EquipmentCategoryRequest): EquipmentCategory {
        val category = get(id)
        if (!category.name.equals(request.name, ignoreCase = true) &&
            equipmentCategoryRepository.existsByNameIgnoreCase(request.name)
        ) {
            log.warn("event=category.rejected | msg=Duplicate category name | name=\"${request.name}\"")
            throw DuplicateResourceException("A category named '${request.name}' already exists")
        }
        category.name = request.name
        val saved = equipmentCategoryRepository.save(category)
        log.info("event=category.updated | msg=Category updated | categoryId=${saved.id} name=\"${saved.name}\"")
        return saved
    }

    fun delete(id: Long) {
        val category = get(id)
        equipmentCategoryRepository.delete(category)
        log.info("event=category.deleted | msg=Category deleted | categoryId=${category.id}")
    }
}
