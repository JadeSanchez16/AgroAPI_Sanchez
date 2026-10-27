package pe.upeu.agrosierra.servicio

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.config.UsuarioAutenticado
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborRepository
import pe.upeu.agrosierra.repositorio.LoteRepository
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import pe.upeu.agrosierra.web.dto.LaborRequest
import pe.upeu.agrosierra.web.dto.LaborResponse
import pe.upeu.agrosierra.web.dto.PaginaResponse
import pe.upeu.agrosierra.web.dto.toResponse
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class LaborService(
    private val labores: LaborRepository,
    private val lotes: LoteRepository,
    private val usuarios: UsuarioRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    @Transactional(readOnly = true)
    fun listar(loteId: Long, pagina: Int, tamanio: Int): PaginaResponse<LaborResponse> {
        validarPagina(pagina, tamanio)
        if (!lotes.existsById(loteId)) throw RecursoNoEncontradoException("No existe el lote con id $loteId")
        val page = labores.findByLoteId(loteId, PageRequest.of(pagina, tamanio,
            Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("creadoEn"))))
        return PaginaResponse(page.content.map { it.toResponse() }, pagina, tamanio,
            page.totalElements, page.totalPages, page.isLast)
    }

    @Transactional
    fun crear(request: LaborRequest, principal: UsuarioAutenticado): LaborResponse {
        val lote = lotes.findById(requireNotNull(request.loteId)).orElseThrow {
            RecursoNoEncontradoException("No existe el lote con id ${request.loteId}")
        }
        validarLoteAbierto(lote.campaniaCerrada)
        validarFumigacion(request.tipo, requireNotNull(request.cantidad), lote.hectareas, lote.dosisMaximaPorHectarea)
        val usuario = usuarios.findById(principal.id).orElseThrow { RecursoNoEncontradoException("Usuario no encontrado") }
        return labores.save(Labor(
            lote = lote, tipo = requireNotNull(request.tipo), fecha = requireNotNull(request.fecha),
            responsable = usuario, cantidad = request.cantidad, observacion = requireNotNull(request.observacion).trim(),
            creadoEn = Instant.now(clock),
        )).toResponse()
    }

    @Transactional
    fun actualizar(id: Long, request: LaborRequest, principal: UsuarioAutenticado): LaborResponse {
        val labor = buscar(id)
        validarLoteAbierto(labor.lote.campaniaCerrada)
        validarPropiedadYPlazo(labor, principal)
        val loteDestino = lotes.findById(requireNotNull(request.loteId)).orElseThrow {
            RecursoNoEncontradoException("No existe el lote con id ${request.loteId}")
        }
        validarLoteAbierto(loteDestino.campaniaCerrada)
        validarFumigacion(request.tipo, requireNotNull(request.cantidad), loteDestino.hectareas, loteDestino.dosisMaximaPorHectarea)
        labor.lote = loteDestino
        labor.tipo = requireNotNull(request.tipo)
        labor.fecha = requireNotNull(request.fecha)
        labor.cantidad = request.cantidad
        labor.observacion = requireNotNull(request.observacion).trim()
        return labores.save(labor).toResponse()
    }

    @Transactional
    fun eliminar(id: Long) {
        val labor = buscar(id)
        labores.delete(labor)
    }

    private fun buscar(id: Long) = labores.findById(id).orElseThrow {
        RecursoNoEncontradoException("No existe la labor con id $id")
    }

    private fun validarLoteAbierto(cerrada: Boolean) {
        if (cerrada) throw ReglaNegocioException("No se puede registrar ni corregir una labor en una campaña cerrada")
    }

    private fun validarPropiedadYPlazo(labor: Labor, principal: UsuarioAutenticado) {
        if (!principal.editaTodasLabores) {
            val propia = labor.responsable.id == principal.id
            val dentroDePlazo = Duration.between(labor.creadoEn, Instant.now(clock)) <= Duration.ofHours(24)
            if (!propia || !dentroDePlazo) {
                throw SinPrivilegioException("Un técnico solo puede corregir labores propias durante las primeras 24 horas")
            }
        }
    }

    private fun validarFumigacion(tipo: TipoLabor?, cantidad: Double, hectareas: Double, dosisMaxima: Double) {
        if (tipo == TipoLabor.FUMIGACION && cantidad > dosisMaxima * hectareas) {
            throw ValidacionNegocioException("Cantidad de fumigación inválida", mapOf(
                "cantidad" to "No puede superar la dosis máxima por hectárea multiplicada por las hectáreas del lote",
            ))
        }
    }
}
