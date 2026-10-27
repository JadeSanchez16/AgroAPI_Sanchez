package pe.upeu.agrosierra.seguridad

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "rol")
class Rol(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 50)
    var nombre: String = "",

    @Column(name = "edita_todas_labores", nullable = false)
    var editaTodasLabores: Boolean = false,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rol_privilegio", joinColumns = [JoinColumn(name = "rol_id")])
    @Column(name = "privilegio", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var privilegios: MutableSet<Privilegio> = mutableSetOf(),
)
