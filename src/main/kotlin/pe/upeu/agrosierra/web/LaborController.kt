package pe.upeu.agrosierra.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pe.upeu.agrosierra.config.UsuarioAutenticado
import pe.upeu.agrosierra.servicio.LaborService
import pe.upeu.agrosierra.web.dto.LaborRequest
import pe.upeu.agrosierra.web.dto.LaborResponse
import pe.upeu.agrosierra.web.dto.PaginaResponse

@RestController
@RequestMapping("/api/v1")
class LaborController(private val service: LaborService) {
    @GetMapping("/lotes/{loteId}/labores")
    @PreAuthorize("hasAuthority('LABOR_LEER')")
    fun listar(@PathVariable loteId: Long, @RequestParam(defaultValue = "0") pagina: Int,
               @RequestParam(defaultValue = "20") tamanio: Int): PaginaResponse<LaborResponse> =
        service.listar(loteId, pagina, tamanio)

    @PostMapping("/labores")
    @PreAuthorize("hasAuthority('LABOR_CREAR')")
    fun crear(@Valid @RequestBody request: LaborRequest,
              @AuthenticationPrincipal principal: UsuarioAutenticado): ResponseEntity<LaborResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request, principal))

    @PutMapping("/labores/{id}")
    @PreAuthorize("hasAuthority('LABOR_EDITAR')")
    fun actualizar(@PathVariable id: Long, @Valid @RequestBody request: LaborRequest,
                   @AuthenticationPrincipal principal: UsuarioAutenticado): LaborResponse =
        service.actualizar(id, request, principal)

    @DeleteMapping("/labores/{id}")
    @PreAuthorize("hasAuthority('LABOR_ELIMINAR')")
    fun eliminar(@PathVariable id: Long): ResponseEntity<Void> {
        service.eliminar(id)
        return ResponseEntity.noContent().build()
    }
}
