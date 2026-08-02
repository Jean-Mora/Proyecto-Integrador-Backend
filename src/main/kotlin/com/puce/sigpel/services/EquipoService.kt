package com.puce.sigpel.services

import com.puce.sigpel.dto.EquipoRequest
import com.puce.sigpel.entities.Equipo
import com.puce.sigpel.repositories.EquipoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EquipoService(
    private val equipoRepository: EquipoRepository,
    private val categoriaEquipoService: CategoriaEquipoService
) {
    fun crear(request: EquipoRequest): Equipo {
        val categoria = categoriaEquipoService.obtener(request.categoriaId)
        val equipo = Equipo(
            categoria = categoria,
            nombre = request.nombre,
            descripcion = request.descripcion
        )
        return equipoRepository.save(equipo)
    }
}
