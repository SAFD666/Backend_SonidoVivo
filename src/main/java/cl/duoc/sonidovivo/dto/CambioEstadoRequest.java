package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.EstadoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
        @Schema(example = "Enviado", description = "En preparación | Enviado | Entregado | Cancelado")
        @NotNull(message = "El estado es obligatorio")
        EstadoPedido estado
) { }
