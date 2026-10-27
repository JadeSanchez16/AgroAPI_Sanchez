package pe.upeu.agrosierra.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pe.upeu.agrosierra.seguridad.Usuario
import pe.upeu.agrosierra.seguridad.UsuarioRepository

data class UsuarioAutenticado(
    val id: Long,
    val username: String,
    val nombre: String,
    val rol: String,
    val editaTodasLabores: Boolean,
)

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header?.startsWith("Bearer ") == true && SecurityContextHolder.getContext().authentication == null) {
            val identity = jwtService.validar(header.removePrefix("Bearer ").trim())
            val usuario = identity?.let { usuarioRepository.findById(it.usuarioId).orElse(null) }
            if (usuario != null && usuario.activo && usuario.username == identity.username) {
                autenticar(usuario)
            }
        }
        chain.doFilter(request, response)
    }

    private fun autenticar(usuario: Usuario) {
        val principal = UsuarioAutenticado(
            requireNotNull(usuario.id), usuario.username, usuario.nombre, usuario.rol.nombre,
            usuario.rol.editaTodasLabores,
        )
        val authorities = usuario.rol.privilegios.map { SimpleGrantedAuthority(it.name) }
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(principal, null, authorities)
    }
}
