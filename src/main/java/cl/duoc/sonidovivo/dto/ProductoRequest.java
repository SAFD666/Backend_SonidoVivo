package cl.duoc.sonidovivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// Datos del formulario de productos (validaciones: código 2 letras + 3 números, nombre >= 3 chars, precio > 0, stock >= 0).
public record ProductoRequest(
        @Schema(example = "GA006")
        @Pattern(regexp = "^[A-Za-z]{2}\\d{3}$", message = "Formato: 2 letras y 3 números (ej: GA006)")
        String codigo,

        @Schema(example = "Guitarra Acústica Folk")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombre,

        @Schema(example = "Yamaha")
        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 60)
        String marca,

        @Schema(example = "F310")
        @Size(max = 60)
        String modelo,

        @Schema(example = "Guitarras Acústicas", description = "Nombre de una categoría existente")
        @NotBlank(message = "La categoría es obligatoria")
        String categoria,

        @Schema(example = "129990")
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser un número entero mayor a 0")
        Integer precio,

        @Schema(example = "10")
        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock debe ser un número entero igual o mayor a 0")
        Integer stock,

        @Schema(example = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=600&q=80",
                description = "Si va vacía se usa la imagen de la categoría")
        @Size(max = 255)
        String imagen,

        @Size(max = 500)
        String descripcion,

        Boolean esOferta
) { }
