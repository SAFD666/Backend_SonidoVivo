package cl.duoc.sonidovivo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Formulario de categorías: usa el logotipo si la imagen está vacía.
public record CategoriaRequest(
        @Schema(example = "Guitarras Acústicas")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
        String nombre,

        @Schema(example = "/img/acustica.png")
        @Size(max = 255)
        String imagen
) { }
