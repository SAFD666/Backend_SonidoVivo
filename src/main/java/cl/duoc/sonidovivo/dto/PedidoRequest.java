package cl.duoc.sonidovivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

// Datos del checkout del frontend (sin correo, precios, total ni fechas, que calcula el servidor).
// La dirección va por partes (DER) y es obligatoria salvo en "Retiro en Tienda".
public record PedidoRequest(
        @Schema(example = "Cliente Prueba", description = "Nombre completo (nombre + apellidos)")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Schema(example = "+56912345678")
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^\\+?(\\s*\\d){8,12}\\s*$", message = "Ingresa un teléfono válido (ej: +56912345678)")
        String telefono,

        @Schema(description = "Obligatoria para despacho; se omite en Retiro en Tienda")
        @Valid
        DireccionRequest direccion,

        @Schema(example = "Despacho Starken", description = "Retiro en Tienda (Viña del Mar) | Despacho Starken | Despacho Chilexpress")
        @NotBlank(message = "El tipo de entrega es obligatorio")
        String tipoEntrega,

        @Schema(example = "Tarjeta de crédito", description = "Tarjeta de crédito | Tarjeta de débito | Transferencia bancaria. Pago simulado: no se reciben datos de tarjeta.")
        String metodoPago,

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        @Size(max = 50, message = "Un pedido no puede tener más de 50 líneas")
        @Valid
        List<ItemPedidoRequest> items
) { }