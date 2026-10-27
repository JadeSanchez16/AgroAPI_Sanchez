package pe.upeu.agrosierra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import pe.upeu.agrosierra.seguridad.Privilegio
import pe.upeu.agrosierra.seguridad.RolRepository
import pe.upeu.agrosierra.seguridad.UsuarioRepository

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class ScBIntegrationTest @Autowired constructor(
    private val roles: RolRepository,
    private val usuarios: UsuarioRepository,
) {
    @Test
    fun `SC-B crea supervisor con privilegios exactos y edicion global`() {
        val rol = requireNotNull(roles.findByNombre("SUPERVISOR_CALIDAD"))
        assertEquals(setOf(Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_EDITAR), rol.privilegios)
        assertTrue(rol.editaTodasLabores)
        assertFalse(rol.privilegios.contains(Privilegio.CAMPANIA_CERRAR))
        assertEquals("SUPERVISOR_CALIDAD", usuarios.findByUsername("supervisor1")?.rol?.nombre)
    }
}
