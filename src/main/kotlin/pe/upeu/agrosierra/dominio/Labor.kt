package pe.upeu.agrosierra.dominio

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import pe.upeu.agrosierra.seguridad.Usuario
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "labor")
class Labor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    var lote: Lote = Lote(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var tipo: TipoLabor = TipoLabor.RIEGO,
    @Column(nullable = false)
    var fecha: LocalDate = LocalDate.now(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    var responsable: Usuario = Usuario(),
    @Column(nullable = false)
    var cantidad: Double = 0.0,
    @Column(nullable = false, length = 500)
    var observacion: String = "",
    @Column(name = "creado_en", nullable = false, updatable = false)
    var creadoEn: Instant = Instant.now(),
)
