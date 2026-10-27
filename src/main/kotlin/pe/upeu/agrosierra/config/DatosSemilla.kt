package pe.upeu.agrosierra.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborRepository
import pe.upeu.agrosierra.repositorio.LoteRepository
import pe.upeu.agrosierra.seguridad.Privilegio
import pe.upeu.agrosierra.seguridad.Rol
import pe.upeu.agrosierra.seguridad.RolRepository
import pe.upeu.agrosierra.seguridad.Usuario
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import java.time.Instant
import java.time.LocalDate

@Component
class DatosSemilla(
    private val roles: RolRepository,
    private val usuarios: UsuarioRepository,
    private val lotes: LoteRepository,
    private val labores: LaborRepository,
    private val passwordEncoder: PasswordEncoder,
) : ApplicationRunner {
    @Transactional
    override fun run(args: ApplicationArguments) {
        val tecnico = rol("TECNICO", false, setOf(
            Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_CREAR, Privilegio.LABOR_EDITAR,
        ))
        val jefe = rol("JEFE_CAMPO", true, setOf(
            Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_CREAR, Privilegio.LABOR_EDITAR,
            Privilegio.LABOR_ELIMINAR, Privilegio.CAMPANIA_CERRAR,
        ))
        val admin = rol("ADMINISTRADOR", false, setOf(
            Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.USUARIO_GESTIONAR,
        ))
        val supervisorCalidad = rol("SUPERVISOR_CALIDAD", true, setOf(
            Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_EDITAR,
        ))
        val tecnicoUsuario = usuario("tecnico1", "Técnico de campo", tecnico)
        usuario("jefe1", "Jefe de campo", jefe)
        usuario("admin", "Administrador", admin)
        usuario("supervisor1", "Supervisor de calidad", supervisorCalidad)
        if (lotes.count() == 0L) crearLotes()
        if (labores.count() == 0L) crearLabores(tecnicoUsuario)
    }

    private fun rol(nombre: String, editaTodas: Boolean, privilegios: Set<Privilegio>): Rol {
        val existente = roles.findByNombre(nombre)
        if (existente != null) {
            existente.editaTodasLabores = editaTodas
            existente.privilegios.clear()
            existente.privilegios.addAll(privilegios)
            return roles.save(existente)
        }
        return roles.save(Rol(nombre = nombre, editaTodasLabores = editaTodas,
            privilegios = privilegios.toMutableSet()))
    }

    private fun usuario(username: String, nombre: String, rol: Rol): Usuario {
        val existente = usuarios.findByUsername(username)
        if (existente != null) return existente
        return usuarios.save(Usuario(username = username, nombre = nombre,
            passwordHash = requireNotNull(passwordEncoder.encode("Agro2026*")), rol = rol))
    }

    private fun crearLotes() {
        val datos = listOf(
            Lote(codigo = "LOT-001", nombre = "Andenes Norte", hectareas = 4.0, cultivo = "Papa", valle = "Mantaro", campaniaCerrada = true, dosisMaximaPorHectarea = 2.5),
            Lote(codigo = "LOT-002", nombre = "Pampa Sur", hectareas = 6.5, cultivo = "Maíz", valle = "Mantaro", campaniaCerrada = true, dosisMaximaPorHectarea = 2.0),
            Lote(codigo = "LOT-003", nombre = "Quebrada Alta", hectareas = 3.2, cultivo = "Quinua", valle = "Urubamba", dosisMaximaPorHectarea = 1.5),
            Lote(codigo = "LOT-004", nombre = "Terraza Uno", hectareas = 5.0, cultivo = "Café", valle = "Urubamba", dosisMaximaPorHectarea = 1.8),
            Lote(codigo = "LOT-005", nombre = "Ribera Este", hectareas = 2.8, cultivo = "Arroz", valle = "Majes", dosisMaximaPorHectarea = 2.2),
            Lote(codigo = "LOT-006", nombre = "Ladera Verde", hectareas = 4.7, cultivo = "Palta", valle = "Majes", dosisMaximaPorHectarea = 1.2),
            Lote(codigo = "LOT-007", nombre = "Fundo Central", hectareas = 7.1, cultivo = "Cebada", valle = "Mantaro", dosisMaximaPorHectarea = 2.0),
            Lote(codigo = "LOT-008", nombre = "Parcela Sol", hectareas = 3.9, cultivo = "Haba", valle = "Urubamba", dosisMaximaPorHectarea = 1.6),
        )
        lotes.saveAll(datos)
    }

    private fun crearLabores(tecnico: Usuario) {
        val todos = lotes.findAll().sortedBy { it.codigo }
        val laboresSemilla = mutableListOf<Labor>()
        todos.forEachIndexed { indice, lote ->
            laboresSemilla += Labor(lote = lote, tipo = TipoLabor.RIEGO,
                fecha = LocalDate.now().minusDays((indice + 2).toLong()), responsable = tecnico,
                cantidad = 10.0 + indice, cantidadJornales = 2 + (indice % 3),
                observacion = "Riego programado", creadoEn = Instant.now().minusSeconds(172800))
            laboresSemilla += Labor(lote = lote, tipo = TipoLabor.FUMIGACION,
                fecha = LocalDate.now().minusDays((indice + 1).toLong()), responsable = tecnico,
                cantidad = lote.hectareas * lote.dosisMaximaPorHectarea * 0.5,
                cantidadJornales = 1 + (indice % 2),
                observacion = "Fumigación preventiva", creadoEn = Instant.now().minusSeconds(86400))
        }
        listOf(0, 1, 2, 4).forEach { indice ->
            val lote = todos[indice]
            laboresSemilla += Labor(lote = lote, tipo = TipoLabor.COSECHA,
                fecha = LocalDate.now().minusDays(1), responsable = tecnico, cantidad = 100.0 + indice,
                cantidadJornales = 5 + indice, observacion = "Cosecha registrada",
                creadoEn = Instant.now().minusSeconds(3600))
        }
        labores.saveAll(laboresSemilla)
    }
}
