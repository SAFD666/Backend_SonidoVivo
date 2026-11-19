package cl.duoc.sonidovivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// Reposición de stock (para la pantalla "Stock crítico").
public record StockRequest(
        @Schema(example = "15")
        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock debe ser un número entero igual o mayor a 0")
        Integer stock
) { }
