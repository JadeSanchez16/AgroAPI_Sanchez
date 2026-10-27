package pe.upeu.agrosierra.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import pe.upeu.agrosierra.seguridad.Privilegio
import pe.upeu.agrosierra.seguridad.Rol
import pe.upeu.agrosierra.seguridad.Usuario
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class JwtServiceTest {
    private val secret = "clave-de-pruebas-agrosierra-con-mas-de-32-bytes"
    private val instant = Instant.parse("2026-10-27T15:00:00Z")

    @Test
    fun `emite y valida un token firmado`() {
        val service = JwtService(secret, "agrosierra-test", 3600, Clock.fixed(instant, ZoneOffset.UTC))
        val usuario = Usuario(id = 7, username = "tecnico1", nombre = "Técnico",
            rol = Rol(nombre = "TECNICO", privilegios = mutableSetOf(Privilegio.LOTE_LEER)))

        val identity = service.validar(service.emitir(usuario))

        assertEquals(7, identity?.usuarioId)
        assertEquals("tecnico1", identity?.username)
    }

    @Test
    fun `rechaza un token alterado`() {
        val service = JwtService(secret, "agrosierra-test", 3600, Clock.fixed(instant, ZoneOffset.UTC))
        val usuario = Usuario(id = 7, username = "tecnico1", nombre = "Técnico", rol = Rol(nombre = "TECNICO"))
        val partes = service.emitir(usuario).split('.').toMutableList()
        partes[2] = (if (partes[2].first() == 'a') 'b' else 'a') + partes[2].drop(1)
        val alterado = partes.joinToString(".")

        assertNull(service.validar(alterado))
    }

    @Test
    fun `rechaza un token vencido`() {
        val emisor = JwtService(secret, "agrosierra-test", 60, Clock.fixed(instant, ZoneOffset.UTC))
        val validador = JwtService(secret, "agrosierra-test", 60,
            Clock.fixed(instant.plusSeconds(61), ZoneOffset.UTC))
        val usuario = Usuario(id = 7, username = "tecnico1", nombre = "Técnico", rol = Rol(nombre = "TECNICO"))

        assertNull(validador.validar(emisor.emitir(usuario)))
    }
}
