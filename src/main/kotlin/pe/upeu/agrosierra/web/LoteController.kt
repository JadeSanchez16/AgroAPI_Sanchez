package pe.upeu.agrosierra.web

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pe.upeu.agrosierra.servicio.LoteService
import pe.upeu.agrosierra.web.dto.LoteResponse
import pe.upeu.agrosierra.web.dto.PaginaResponse

@RestController
@RequestMapping("/api/v1/lotes")
class LoteController(private val service: LoteService) {
    @GetMapping
    @PreAuthorize("hasAuthority('LOTE_LEER')")
    fun listar(
        @RequestParam(defaultValue = "0") pagina: Int,
        @RequestParam(defaultValue = "20") tamanio: Int,
        @RequestParam(required = false) valle: String?,
        @RequestParam(required = false) campaniaCerrada: Boolean?,
    ): PaginaResponse<LoteResponse> = service.listar(pagina, tamanio, valle, campaniaCerrada)

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOTE_LEER')")
    fun obtener(@PathVariable id: Long): LoteResponse = service.obtener(id)

    @PostMapping("/{id}/cerrar-campania")
    @PreAuthorize("hasAuthority('CAMPANIA_CERRAR')")
    fun cerrar(@PathVariable id: Long): LoteResponse = service.cerrarCampania(id)
}
