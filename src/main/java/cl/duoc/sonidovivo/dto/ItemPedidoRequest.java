package cl.duoc.sonidovivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// Línea del carrito: solo envía código y cantidad (el servidor calcula nombre y precio).
public record ItemPedidoRequest(
        @Schema(example = "GA001")
        @NotBlank(message = "El código del producto es obligatorio")
        String codigo,

        @Schema(example = "2")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 99, message = "La cantidad máxima por producto es 99")
        Integer cantidad
) { }
