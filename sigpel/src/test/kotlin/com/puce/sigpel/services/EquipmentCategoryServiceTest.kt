package com.puce.sigpel.services

import com.puce.sigpel.dto.EquipmentCategoryRequest
import com.puce.sigpel.entities.EquipmentCategory
import com.puce.sigpel.exceptions.DuplicateResourceException
import com.puce.sigpel.repositories.EquipmentCategoryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class EquipmentCategoryServiceTest {

    private val equipmentCategoryRepository = mockk<EquipmentCategoryRepository>()
    private lateinit var equipmentCategoryService: EquipmentCategoryService

    @BeforeEach
    fun setUp() {
        equipmentCategoryService = EquipmentCategoryService(equipmentCategoryRepository)
    }

    @Test
    fun `create saves the category when the name is not taken`() {
        every { equipmentCategoryRepository.existsByNameIgnoreCase("Microphones") } returns false
        val savedSlot = slot<EquipmentCategory>()
        every { equipmentCategoryRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        equipmentCategoryService.create(EquipmentCategoryRequest(name = "Microphones"))

        assertEquals("Microphones", savedSlot.captured.name)
        verify(exactly = 1) { equipmentCategoryRepository.save(any()) }
    }

    @Test
    fun `create throws DuplicateResourceException when a category with the same name already exists`() {
        every { equipmentCategoryRepository.existsByNameIgnoreCase("Microphones") } returns true

        assertThrows(DuplicateResourceException::class.java) {
            equipmentCategoryService.create(EquipmentCategoryRequest(name = "Microphones"))
        }
        verify(exactly = 0) { equipmentCategoryRepository.save(any()) }
    }

    @Test
    fun `create is case-insensitive when checking for duplicates`() {
        every { equipmentCategoryRepository.existsByNameIgnoreCase("microphones") } returns true

        assertThrows(DuplicateResourceException::class.java) {
            equipmentCategoryService.create(EquipmentCategoryRequest(name = "microphones"))
        }
    }

    @Test
    fun `update allows keeping the same name without triggering a duplicate error`() {
        val category = EquipmentCategory(id = 1L, name = "Microphones")
        every { equipmentCategoryRepository.findById(1L) } returns Optional.of(category)
        every { equipmentCategoryRepository.save(category) } returns category

        equipmentCategoryService.update(1L, EquipmentCategoryRequest(name = "Microphones"))

        verify(exactly = 0) { equipmentCategoryRepository.existsByNameIgnoreCase(any()) }
    }

    @Test
    fun `update throws DuplicateResourceException when renaming to another category's name`() {
        val category = EquipmentCategory(id = 1L, name = "Microphones")
        every { equipmentCategoryRepository.findById(1L) } returns Optional.of(category)
        every { equipmentCategoryRepository.existsByNameIgnoreCase("Cameras") } returns true

        assertThrows(DuplicateResourceException::class.java) {
            equipmentCategoryService.update(1L, EquipmentCategoryRequest(name = "Cameras"))
        }
        verify(exactly = 0) { equipmentCategoryRepository.save(any()) }
    }
}
