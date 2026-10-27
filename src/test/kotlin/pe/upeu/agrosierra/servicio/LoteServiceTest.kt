package pe.upeu.agrosierra.servicio

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborRepository
import pe.upeu.agrosierra.repositorio.LoteRepository
import java.util.Optional

class LoteServiceTest {
    private val lotes = mock<LoteRepository>()
    private val labores = mock<LaborRepository>()
    private val service = LoteService(lotes, labores)

    @Test
    fun `RN-03 impide cerrar lote sin cosecha`() {
        whenever(lotes.findById(1)).thenReturn(Optional.of(Lote(id = 1, campaniaCerrada = false)))
        whenever(labores.existsByLoteIdAndTipo(1, TipoLabor.COSECHA)).thenReturn(false)

        assertThrows(ReglaNegocioException::class.java) { service.cerrarCampania(1) }
    }
}
