package cl.duoc.sonidovivo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.Normalizer;
import java.util.Locale;

// Estados de pedido del frontend (EstadoPedido.jsx) serializados como texto ("En preparación", "Enviado").
public enum EstadoPedido {
    EN_PREPARACION("En preparación"),
    ENVIADO("Enviado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @JsonValue
    public String getEtiqueta() {
        return etiqueta;
    }

    // Valida estados de pedido aceptando formato libre o enum ("En preparación", "en preparacion", "EN_PREPARACION"); rechaza otros con 400.
    @JsonCreator
    public static EstadoPedido desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT).replace(' ', '_');
        for (EstadoPedido e : values()) {
            if (e.name().equals(limpio)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + texto);
    }
}
