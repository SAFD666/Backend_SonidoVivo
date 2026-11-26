package cl.duoc.sonidovivo.kaiju;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

// Errores de los endpoints de Kaiju con el mensaje EXACTO que escribimos en los services
// (ej: "Tu cuenta está desactivada..."), en vez del texto genérico del manejador de Sonido Vivo.
// assignableTypes: solo aplica a los controllers de Kaiju. HIGHEST_PRECEDENCE: se usa antes que el global.
@RestControllerAdvice(assignableTypes = {KaijuAuthController.class, KaijuAdminController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class KaijuErrorHandler {

    // Errores que lanzamos nosotros: 400, 401, 403, 404, 409...
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> errorConEstado(ResponseStatusException e) {
        String mensaje = e.getReason() != null ? e.getReason() : "No se pudo completar la solicitud";
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", e.getStatusCode().value(), "message", mensaje));
    }

    // Validaciones de los DTO (@NotBlank, @Email...): devuelve el primer mensaje, ej "Formato de email inválido"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> errorDeValidacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Datos inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", 400, "message", mensaje));
    }
}