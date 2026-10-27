package pe.upeu.agrosierra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.repositorio.LaborRepository

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class ScCIntegrationTest @Autowired constructor(
    private val labores: LaborRepository,
) {
    @Test
    @Transactional
    fun `SC-C persiste cantidad de jornales en datos semilla`() {
        val labor = labores.findAll().first()
        assert(labor.cantidadJornales >= 0)
    }
}
