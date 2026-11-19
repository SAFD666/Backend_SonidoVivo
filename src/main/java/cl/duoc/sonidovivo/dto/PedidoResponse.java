package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.EstadoPedido;
import cl.duoc.sonidovivo.model.Pedido;

import java.time.LocalDate;
import java.util.List;

// Pedido con la estructura de pedidosIniciales del frontend, más usuarioId y metodoPago.
public record PedidoResponse(
        String id,
        LocalDate fecha,
        Long usuarioId,
        String clienteCorreo,
        String clienteNombre,
        String direccion,
        String telefono,
        String tipoEntrega,
        String metodoPago,
        EstadoPedido estado,
        int total,
        List<ItemPedidoResponse> items
) {
    public static PedidoResponse desde(Pedido p) {
        return new PedidoResponse(
                p.getCodigo(), p.getFecha(), p.getUsuario().getId(), p.getClienteCorreo(), p.getClienteNombre(),
                p.getDireccion(), p.getTelefono(), p.getTipoEntrega(), p.getMetodoPago(), p.getEstado(), p.getTotal(),
                p.getDetalles().stream().map(ItemPedidoResponse::desde).toList());
    }
}
