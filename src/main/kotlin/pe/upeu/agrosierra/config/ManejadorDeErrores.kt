package pe.upeu.agrosierra.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pe.upeu.agrosierra.servicio.CredencialesInvalidasException
import pe.upeu.agrosierra.servicio.RecursoNoEncontradoException
import pe.upeu.agrosierra.servicio.ReglaNegocioException
import pe.upeu.agrosierra.servicio.SinPrivilegioException
import pe.upeu.agrosierra.servicio.ValidacionNegocioException
import pe.upeu.agrosierra.web.dto.ErrorResponse
import java.time.Instant

@RestControllerAdvice
class ManejadorDeErrores {
    @ExceptionHandler(CredencialesInvalidasException::class)
    fun credenciales(ex: CredencialesInvalidasException, request: HttpServletRequest) =
        respuesta(HttpStatus.UNAUTHORIZED, ex.message!!, request)

    @ExceptionHandler(RecursoNoEncontradoException::class)
    fun noEncontrado(ex: RecursoNoEncontradoException, request: HttpServletRequest) =
        respuesta(HttpStatus.NOT_FOUND, ex.message!!, request)

    @ExceptionHandler(ReglaNegocioException::class)
    fun conflicto(ex: ReglaNegocioException, request: HttpServletRequest) =
        respuesta(HttpStatus.CONFLICT, ex.message!!, request)

    @ExceptionHandler(SinPrivilegioException::class, AccessDeniedException::class)
    fun prohibido(ex: Exception, request: HttpServletRequest) =
        respuesta(HttpStatus.FORBIDDEN, ex.message ?: "No posee el privilegio requerido", request)

    @ExceptionHandler(ValidacionNegocioException::class)
    fun validacionNegocio(ex: ValidacionNegocioException, request: HttpServletRequest) =
        respuesta(HttpStatus.BAD_REQUEST, ex.message!!, request, ex.errores)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacion(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val errores = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Valor inválido") }
        return respuesta(HttpStatus.BAD_REQUEST, "Datos inválidos", request, errores)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class, ConstraintViolationException::class)
    fun solicitudInvalida(ex: Exception, request: HttpServletRequest) =
        respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es inválido", request)

    @ExceptionHandler(Exception::class)
    fun inesperado(ex: Exception, request: HttpServletRequest) =
        respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno", request)

    private fun respuesta(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
        errores: Map<String, String>? = null,
    ): ResponseEntity<ErrorResponse> = ResponseEntity.status(status).body(ErrorResponse(
        timestamp = Instant.now(), status = status.value(), error = status.reasonPhrase,
        message = message, path = request.requestURI, validationErrors = errores,
    ))
}
