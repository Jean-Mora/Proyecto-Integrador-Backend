package com.puce.sigpel.repositories

import com.puce.sigpel.entities.PrestamoAuditoria
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PrestamoAuditoriaRepository : JpaRepository<PrestamoAuditoria, Long> {
    fun findByPrestamoId(prestamoId: Long): List<PrestamoAuditoria>
}