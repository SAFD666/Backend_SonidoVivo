package cl.duoc.sonidovivo.dto;

import java.util.List;

// Estados válidos para el checkout y el panel de pedidos.
public record OpcionesPedidoResponse(List<String> tiposEntrega, List<String> metodosPago, List<String> estados) { }
