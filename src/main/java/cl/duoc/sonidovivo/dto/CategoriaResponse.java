package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Categoria;

// Mismo que { nombre, imagen } del frontend, más el id de la base de datos.
public record CategoriaResponse(Long id, String nombre, String imagen) {
    public static CategoriaResponse desde(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getImagen());
    }
}
