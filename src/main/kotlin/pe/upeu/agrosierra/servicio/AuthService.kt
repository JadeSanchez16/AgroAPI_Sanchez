package pe.upeu.agrosierra.servicio

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pe.upeu.agrosierra.config.JwtService
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import pe.upeu.agrosierra.web.dto.LoginRequest
import pe.upeu.agrosierra.web.dto.LoginResponse

@Service
class AuthService(
    private val usuarios: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun login(request: LoginRequest): LoginResponse {
        val usuario = usuarios.findByUsername(request.usuario)
            ?.takeIf { it.activo && passwordEncoder.matches(request.contrasena, it.passwordHash) }
            ?: throw CredencialesInvalidasException()
        return LoginResponse(
            token = jwtService.emitir(usuario), nombre = usuario.nombre, usuario = usuario.username,
            rol = usuario.rol.nombre, privilegios = usuario.rol.privilegios.map { it.name }.toSortedSet(),
        )
    }
}

class CredencialesInvalidasException : RuntimeException("Usuario o contraseña incorrectos")
