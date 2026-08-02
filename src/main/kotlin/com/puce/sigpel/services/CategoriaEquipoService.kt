package com.puce.sigpel.services

import com.puce.sigpel.dto.CategoriaEquipoRequest
import com.puce.sigpel.entities.CategoriaEquipo
import com.puce.sigpel.repositories.CategoriaEquipoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoriaEquipoService(
    private val categoriaEquipoRepository: CategoriaEquipoRepository
) {
    fun crear(request: CategoriaEquipoRequest): CategoriaEquipo =
        categoriaEquipoRepository.save(CategoriaEquipo(nombre = request.nombre))
}
