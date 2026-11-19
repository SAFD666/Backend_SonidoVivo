package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.DetallePedido;

// Item del pedido (idéntico al frontend): { codigo, nombre, precio, cantidad }.
public record ItemPedidoResponse(String codigo, String nombre, int precio, int cantidad) {
    public static ItemPedidoResponse desde(DetallePedido d) {
        return new ItemPedidoResponse(d.getCodigo(), d.getNombre(), d.getPrecioUnitario(), d.getCantidad());
    }
}
