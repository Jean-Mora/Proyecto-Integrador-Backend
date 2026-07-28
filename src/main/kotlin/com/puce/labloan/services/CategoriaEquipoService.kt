package com.puce.labloan.services

import com.puce.labloan.dto.CategoriaEquipoRequest
import com.puce.labloan.entities.CategoriaEquipo
import com.puce.labloan.exceptions.ResourceNotFoundException
import com.puce.labloan.repositories.CategoriaEquipoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoriaEquipoService(
    private val categoriaEquipoRepository: CategoriaEquipoRepository
) {
    @Transactional(readOnly = true)
    fun listar(): List<CategoriaEquipo> = categoriaEquipoRepository.findAll()

    @Transactional(readOnly = true)
    fun obtener(id: Long): CategoriaEquipo =
        categoriaEquipoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Categoria $id no encontrada") }

    fun crear(request: CategoriaEquipoRequest): CategoriaEquipo =
        categoriaEquipoRepository.save(CategoriaEquipo(nombre = request.nombre))

    fun editar(id: Long, request: CategoriaEquipoRequest): CategoriaEquipo {
        val categoria = obtener(id)
        categoria.nombre = request.nombre
        return categoriaEquipoRepository.save(categoria)
    }

    fun eliminar(id: Long) {
        categoriaEquipoRepository.delete(obtener(id))
    }
}
