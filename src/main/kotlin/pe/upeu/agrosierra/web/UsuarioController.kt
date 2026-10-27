package pe.upeu.agrosierra.web

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pe.upeu.agrosierra.config.UsuarioAutenticado
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import pe.upeu.agrosierra.servicio.RecursoNoEncontradoException
import pe.upeu.agrosierra.web.dto.UsuarioActualResponse

@RestController
@RequestMapping("/api/v1/usuarios")
class UsuarioController(private val usuarios: UsuarioRepository) {
    @GetMapping("/me")
    fun actual(@AuthenticationPrincipal principal: UsuarioAutenticado): UsuarioActualResponse {
        val usuario = usuarios.findById(principal.id).orElseThrow { RecursoNoEncontradoException("Usuario no encontrado") }
        return UsuarioActualResponse(requireNotNull(usuario.id), usuario.username, usuario.nombre,
            usuario.rol.nombre, usuario.rol.privilegios.map { it.name }.toSortedSet())
    }
}
