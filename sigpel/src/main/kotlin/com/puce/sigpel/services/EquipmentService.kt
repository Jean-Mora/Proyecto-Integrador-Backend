package com.puce.sigpel.services

import com.puce.sigpel.dto.EquipmentRequest
import com.puce.sigpel.dto.EquipmentStatusRequest
import com.puce.sigpel.entities.Equipment
import com.puce.sigpel.entities.EquipmentStatus
import com.puce.sigpel.exceptions.ResourceNotFoundException
import com.puce.sigpel.repositories.EquipmentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EquipmentService(
    private val equipmentRepository: EquipmentRepository,
    private val equipmentCategoryService: EquipmentCategoryService
) {
    private val log = LoggerFactory.getLogger(EquipmentService::class.java)

    @Transactional(readOnly = true)
    fun list(categoryId: Long?, status: EquipmentStatus?): List<Equipment> {
        // Validates that the category exists before filtering; throws ResourceNotFoundException if not.
        categoryId?.let { equipmentCategoryService.get(it) }

        return when {
            categoryId == null && status == null -> equipmentRepository.findAllWithCategory()
            categoryId == null -> equipmentRepository.findByStatusWithCategory(status!!)
            else -> equipmentRepository.findByCategoryAndStatus(categoryId, status)
        }
    }

    @Transactional(readOnly = true)
    fun get(id: Long): Equipment =
        equipmentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Equipment $id not found") }

    fun create(request: EquipmentRequest): Equipment {
        val category = equipmentCategoryService.get(request.categoryId)
        val equipment = Equipment(
            category = category,
            name = request.name,
            description = request.description
        )
        val saved = equipmentRepository.save(equipment)
        log.info("event=equipment.created | msg=Equipment created | equipmentId=${saved.id} categoryId=${category.id} name=\"${saved.name}\"")
        return saved
    }

    fun updateStatus(id: Long, request: EquipmentStatusRequest): Equipment {
        val equipment = get(id)
        val previousStatus = equipment.status
        equipment.status = request.status
        val saved = equipmentRepository.save(equipment)
        log.info("event=equipment.status_changed | msg=Equipment status changed | equipmentId=${saved.id} from=$previousStatus to=${saved.status}")
        return saved
    }

    fun delete(id: Long) {
        val equipment = get(id)
        equipmentRepository.delete(equipment)
        log.info("event=equipment.deleted | msg=Equipment deleted | equipmentId=${equipment.id}")
    }
}
