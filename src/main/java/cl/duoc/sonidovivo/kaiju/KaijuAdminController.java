package cl.duoc.sonidovivo.kaiju;

import cl.duoc.sonidovivo.kaiju.KaijuDtos.ActualizarUsuarioRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Administración de usuarios de Kaiju. Cada petición trae el token del admin en el header Authorization.
// @SecurityRequirement: el botón "Authorize" de Swagger envía el token a estos endpoints.
@RestController
@RequestMapping("/api/v1/kaiju/admin/usuarios")
@Tag(name = "Kaiju - Administración", description = "Gestión de usuarios de Kaiju (solo rol ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class KaijuAdminController {

    private final KaijuAdminService adminService;

    public KaijuAdminController(KaijuAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios de Kaiju")
    public List<UsuarioResponse> listar(
            @Parameter(hidden = true)   // no se muestra como campo: lo pone el botón Authorize
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return adminService.listar(authorization);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar nombre, email, rol y estado de un usuario")
    public UsuarioResponse actualizar(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return adminService.actualizar(authorization, id, request);
    }
}