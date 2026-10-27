package pe.upeu.agrosierra.servicio

open class ApiException(message: String) : RuntimeException(message)
class RecursoNoEncontradoException(message: String) : ApiException(message)
class ReglaNegocioException(message: String) : ApiException(message)
class SinPrivilegioException(message: String) : ApiException(message)
class ValidacionNegocioException(message: String, val errores: Map<String, String>) : ApiException(message)
