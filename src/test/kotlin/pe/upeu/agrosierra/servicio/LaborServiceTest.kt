package pe.upeu.agrosierra.servicio

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import pe.upeu.agrosierra.config.UsuarioAutenticado
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborRepository
import pe.upeu.agrosierra.repositorio.LoteRepository
import pe.upeu.agrosierra.seguridad.Rol
import pe.upeu.agrosierra.seguridad.Usuario
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import pe.upeu.agrosierra.web.dto.LaborRequest
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Optional

class LaborServiceTest {
    private val labores = mock<LaborRepository>()
    private val lotes = mock<LoteRepository>()
    private val usuarios = mock<UsuarioRepository>()
    private val ahora = Instant.parse("2026-10-27T15:00:00Z")
    private val service = LaborService(labores, lotes, usuarios, Clock.fixed(ahora, ZoneOffset.UTC))
    private val principal = UsuarioAutenticado(1, "tecnico1", "Técnico", "TECNICO", false)

    @Test
    fun `RN-01 impide registrar en campania cerrada`() {
        whenever(lotes.findById(1)).thenReturn(Optional.of(Lote(id = 1, campaniaCerrada = true)))

        assertThrows(ReglaNegocioException::class.java) { service.crear(request(), principal) }
        verifyNoInteractions(usuarios)
    }

    @Test
    fun `RN-02 impide al tecnico editar una labor ajena`() {
        val autor = Usuario(id = 2, username = "otro", rol = Rol(nombre = "TECNICO"))
        val lote = Lote(id = 1, hectareas = 2.0, dosisMaximaPorHectarea = 2.0)
        whenever(labores.findById(5)).thenReturn(Optional.of(Labor(id = 5, lote = lote,
            responsable = autor, creadoEn = ahora.minusSeconds(3600))))

        assertThrows(SinPrivilegioException::class.java) { service.actualizar(5, request(), principal) }
    }

    @Test
    fun `RN-02 impide editar despues de veinticuatro horas`() {
        val autor = Usuario(id = 1, username = "tecnico1", rol = Rol(nombre = "TECNICO"))
        val lote = Lote(id = 1, hectareas = 2.0, dosisMaximaPorHectarea = 2.0)
        whenever(labores.findById(5)).thenReturn(Optional.of(Labor(id = 5, lote = lote,
            responsable = autor, creadoEn = ahora.minusSeconds(24 * 3600 + 1))))

        assertThrows(SinPrivilegioException::class.java) { service.actualizar(5, request(), principal) }
    }

    @Test
    fun `RN-04 devuelve error de cantidad cuando excede la dosis`() {
        whenever(lotes.findById(1)).thenReturn(Optional.of(Lote(id = 1, hectareas = 2.0,
            dosisMaximaPorHectarea = 1.5)))
        val request = request(tipo = TipoLabor.FUMIGACION, cantidad = 3.1)

        val error = assertThrows(ValidacionNegocioException::class.java) { service.crear(request, principal) }
        assert(error.errores.containsKey("cantidad"))
    }

    private fun request(tipo: TipoLabor = TipoLabor.RIEGO, cantidad: Double = 1.0) = LaborRequest(
        loteId = 1, tipo = tipo, fecha = LocalDate.of(2026, 10, 27), cantidad = cantidad,
        observacion = "Labor de prueba",
    )
}
