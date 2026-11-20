package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Categoria;

// Mismo que { nombre, imagen } del frontend, más el id y el slug para la URL.
public record CategoriaResponse(Long id, String nombre, String slug, String imagen) {
    public static CategoriaResponse desde(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getSlug(), c.getImagen());
    }
}