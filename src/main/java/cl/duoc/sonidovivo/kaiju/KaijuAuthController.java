package cl.duoc.sonidovivo.kaiju;

import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginResponse;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.RegistroRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Login, registro y sesión de la app Kaiju (usuarios en la tabla usuario_kaiju)
@RestController
@RequestMapping("/api/v1/kaiju/auth")
@Tag(name = "Kaiju - Autenticación", description = "Registro e inicio de sesión de la app Android Kaiju")
public class KaijuAuthController {

    private final KaijuAuthService authService;

    public KaijuAuthController(KaijuAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un operador de Kaiju (queda con rol BODEGA)")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión en Kaiju (devuelve un token JWT)")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // NUEVO: la app lo llama para saber si la sesión sigue válida (cuenta activa, rol actual)
    @GetMapping("/yo")
    @Operation(summary = "Datos de mi sesión: 403 si la cuenta fue desactivada, 401 si el token venció")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse yo(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return authService.yo(authorization);
    }
}