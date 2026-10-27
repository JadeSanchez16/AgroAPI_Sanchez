package pe.upeu.agrosierra.web.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.TipoLabor
import java.time.Instant
import java.time.LocalDate

data class LaborRequest(
    @field:NotNull(message = "El lote es obligatorio")
    @field:Positive(message = "El lote debe ser válido")
    val loteId: Long?,
    @field:NotNull(message = "El tipo es obligatorio")
    val tipo: TipoLabor?,
    @field:NotNull(message = "La fecha es obligatoria")
    @field:PastOrPresent(message = "La fecha no puede ser futura")
    val fecha: LocalDate?,
    @field:NotNull(message = "La cantidad es obligatoria")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor que cero")
    val cantidad: Double?,
    @field:PositiveOrZero(message = "La cantidad de jornales no puede ser negativa")
    val cantidadJornales: Int = 0,
    @field:NotBlank(message = "La observación es obligatoria")
    @field:Size(max = 500, message = "La observación admite hasta 500 caracteres")
    val observacion: String?,
)

data class LaborResponse(
    val id: Long,
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: LocalDate,
    val responsable: String,
    val cantidad: Double,
    val cantidadJornales: Int,
    val observacion: String,
    val creadoEn: Instant,
)

fun Labor.toResponse() = LaborResponse(
    id = requireNotNull(id), loteId = requireNotNull(lote.id), tipo = tipo, fecha = fecha,
    responsable = responsable.username, cantidad = cantidad, cantidadJornales = cantidadJornales,
    observacion = observacion, creadoEn = creadoEn,
)
