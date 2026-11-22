package cl.duoc.sonidovivo.kaiju;

import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginResponse;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.RegistroRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Login y registro de la app Kaiju (usuarios en la tabla usuario_kaiju)
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
    @Operation(summary = "Registrar un operador de Kaiju")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión en Kaiju (devuelve un token JWT)")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}