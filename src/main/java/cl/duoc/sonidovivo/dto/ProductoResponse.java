package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Producto;

// Producto compatible con src/data/productos.js (el id equivale al código).
public record ProductoResponse(
        String id,
        String codigo,
        String nombre,
        String marca,
        String modelo,
        String categoria,
        int stock,
        int precio,
        String imagen,
        String descripcion,
        boolean esOferta
) {
    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getCodigo(), p.getCodigo(), p.getNombre(), p.getMarca(), p.getModelo(),
                p.getCategoria().getNombre(), p.getStock(), p.getPrecio(),
                p.getImagen(), p.getDescripcion(), p.isEsOferta());
    }
}
