package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Marca;

// Marca que devuelve la API (para el filtro por marca de la página Ofertas).
public record MarcaResponse(Long id, String nombre) {
    public static MarcaResponse desde(Marca m) {
        return new MarcaResponse(m.getId(), m.getNombre());
    }
}