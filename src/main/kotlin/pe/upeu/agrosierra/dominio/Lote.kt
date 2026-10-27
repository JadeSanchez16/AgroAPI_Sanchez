package pe.upeu.agrosierra.dominio

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "lote")
class Lote(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true, length = 40)
    var codigo: String = "",
    @Column(nullable = false, length = 120)
    var nombre: String = "",
    @Column(nullable = false)
    var hectareas: Double = 0.0,
    @Column(nullable = false, length = 100)
    var cultivo: String = "",
    @Column(nullable = false, length = 100)
    var valle: String = "",
    @Column(name = "campania_cerrada", nullable = false)
    var campaniaCerrada: Boolean = false,
    @Column(name = "dosis_maxima_por_hectarea", nullable = false)
    var dosisMaximaPorHectarea: Double = 0.0,
)
