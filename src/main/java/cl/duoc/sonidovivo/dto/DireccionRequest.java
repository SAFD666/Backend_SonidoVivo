package cl.duoc.sonidovivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// Dirección de despacho que envía el checkout (por partes, según el DER).
public record DireccionRequest(
        @Schema(example = "Av. Libertad")
        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 100, message = "La calle no puede superar los 100 caracteres")
        String calle,

        @Schema(example = "450")
        @NotBlank(message = "El número es obligatorio")
        @Size(max = 10, message = "El número no puede superar los 10 caracteres")
        String numero,

        @Schema(example = "", description = "Opcional")
        @Size(max = 20)
        String depto,

        @Schema(example = "8", description = "id de la comuna (ver GET /api/v1/regiones)")
        @NotNull(message = "La comuna es obligatoria")
        Integer idComuna
) { }