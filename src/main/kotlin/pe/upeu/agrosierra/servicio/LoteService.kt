package pe.upeu.agrosierra.servicio

import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborRepository
import pe.upeu.agrosierra.repositorio.LoteRepository
import pe.upeu.agrosierra.web.dto.LoteResponse
import pe.upeu.agrosierra.web.dto.PaginaResponse
import pe.upeu.agrosierra.web.dto.toResponse

@Service
class LoteService(
    private val lotes: LoteRepository,
    private val labores: LaborRepository,
) {
    @Transactional(readOnly = true)
    fun listar(pagina: Int, tamanio: Int, valle: String?, campaniaCerrada: Boolean?): PaginaResponse<LoteResponse> {
        validarPagina(pagina, tamanio)
        val specification = Specification<Lote> { root, _, cb ->
            val filtros = mutableListOf<Predicate>()
            valle?.takeIf { it.isNotBlank() }?.let {
                filtros += cb.equal(cb.lower(root.get("valle")), it.lowercase())
            }
            campaniaCerrada?.let { filtros += cb.equal(root.get<Boolean>("campaniaCerrada"), it) }
            cb.and(*filtros.toTypedArray())
        }
        val page = lotes.findAll(specification, PageRequest.of(pagina, tamanio, Sort.by("codigo")))
        return PaginaResponse(page.content.map { it.toResponse() }, pagina, tamanio,
            page.totalElements, page.totalPages, page.isLast)
    }

    @Transactional(readOnly = true)
    fun obtener(id: Long): LoteResponse = buscar(id).toResponse()

    @Transactional
    fun cerrarCampania(id: Long): LoteResponse {
        val lote = buscar(id)
        if (lote.campaniaCerrada) return lote.toResponse()
        if (!labores.existsByLoteIdAndTipo(id, TipoLabor.COSECHA)) {
            throw ReglaNegocioException("La campaña no puede cerrarse sin una labor de tipo COSECHA")
        }
        lote.campaniaCerrada = true
        return lotes.save(lote).toResponse()
    }

    fun buscar(id: Long): Lote = lotes.findById(id).orElseThrow {
        RecursoNoEncontradoException("No existe el lote con id $id")
    }
}

fun validarPagina(pagina: Int, tamanio: Int) {
    if (pagina < 0 || tamanio !in 1..100) {
        throw ValidacionNegocioException("Parámetros de paginación inválidos", mapOf(
            "pagina" to "Debe ser mayor o igual a cero",
            "tamanio" to "Debe estar entre 1 y 100",
        ))
    }
}
