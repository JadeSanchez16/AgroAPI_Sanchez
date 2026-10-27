package pe.upeu.agrosierra.web.dto

import pe.upeu.agrosierra.dominio.Lote

data class LoteResponse(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val hectareas: Double,
    val cultivo: String,
    val valle: String,
    val campaniaCerrada: Boolean,
    val dosisMaximaPorHectarea: Double,
)

fun Lote.toResponse() = LoteResponse(
    id = requireNotNull(id), codigo = codigo, nombre = nombre, hectareas = hectareas,
    cultivo = cultivo, valle = valle, campaniaCerrada = campaniaCerrada,
    dosisMaximaPorHectarea = dosisMaximaPorHectarea,
)
