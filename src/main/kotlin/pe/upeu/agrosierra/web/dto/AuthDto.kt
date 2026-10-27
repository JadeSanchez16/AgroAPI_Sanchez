package pe.upeu.agrosierra.web.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "El usuario es obligatorio") val usuario: String,
    @field:NotBlank(message = "La contraseña es obligatoria") val contrasena: String,
)

data class LoginResponse(
    val token: String,
    val nombre: String,
    val usuario: String,
    val rol: String,
    val privilegios: Set<String>,
)

data class UsuarioActualResponse(
    val id: Long,
    val usuario: String,
    val nombre: String,
    val rol: String,
    val privilegios: Set<String>,
)
