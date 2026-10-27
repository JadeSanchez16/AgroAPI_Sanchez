package pe.upeu.agrosierra.repositorio

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import pe.upeu.agrosierra.dominio.Lote

interface LoteRepository : JpaRepository<Lote, Long>, JpaSpecificationExecutor<Lote> {
    fun findByCodigo(codigo: String): Lote?
}
