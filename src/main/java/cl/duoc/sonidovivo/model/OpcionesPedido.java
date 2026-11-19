package cl.duoc.sonidovivo.model;

import java.util.List;

// Opciones fijas del checkout del frontend (FormularioCheckout.jsx); el servidor solo acepta estos valores.
public final class OpcionesPedido {

    public static final List<String> TIPOS_ENTREGA =
            List.of("Retiro en Tienda (Viña del Mar)", "Despacho Starken", "Despacho Chilexpress");

    public static final List<String> METODOS_PAGO =
            List.of("Tarjeta de crédito", "Tarjeta de débito", "Transferencia bancaria");

    private OpcionesPedido() { }
}
