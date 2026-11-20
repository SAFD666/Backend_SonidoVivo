package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.exception.SolicitudInvalidaException;
import cl.duoc.sonidovivo.model.*;
import cl.duoc.sonidovivo.repository.EstadoRepository;
import cl.duoc.sonidovivo.repository.MetodoPagoRepository;
import cl.duoc.sonidovivo.repository.TipoEntregaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Tablas de catálogo del pedido : estado_pedido, tipo_entrega y metodo_pago.
// Las llena al arrancar con los mismos valores del frontend y valida lo que llega del checkout.
@Service
public class CatalogoPedidoService {

    private final EstadoRepository estadoRepository;
    private final TipoEntregaRepository tipoEntregaRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    public CatalogoPedidoService(EstadoRepository estadoRepository, TipoEntregaRepository tipoEntregaRepository,
                                 MetodoPagoRepository metodoPagoRepository) {
        this.estadoRepository = estadoRepository;
        this.tipoEntregaRepository = tipoEntregaRepository;
        this.metodoPagoRepository = metodoPagoRepository;
    }

    // Datos iniciales: solo agrega los que faltan (no duplica al reiniciar).
    @Transactional
    public void cargarSiFalta() {
        for (EstadoPedido e : EstadoPedido.values()) {
            if (estadoRepository.findByNombre(e.getEtiqueta()).isEmpty()) {
                estadoRepository.save(new Estado(e.getEtiqueta()));
            }
        }
        for (String nombre : OpcionesPedido.TIPOS_ENTREGA) {
            if (tipoEntregaRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
                tipoEntregaRepository.save(new TipoEntrega(nombre));
            }
        }
        for (String nombre : OpcionesPedido.METODOS_PAGO) {
            if (metodoPagoRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
                metodoPagoRepository.save(new MetodoPago(nombre));
            }
        }
    }

    public Estado estado(EstadoPedido e) {
        return estadoRepository.findByNombre(e.getEtiqueta())
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + e.getEtiqueta() + " en la base"));
    }

    // Obligatorio; si no existe responde 400 con las opciones válidas.
    public TipoEntrega tipoEntrega(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new SolicitudInvalidaException("El tipo de entrega es obligatorio");
        }
        return tipoEntregaRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "El tipo de entrega no es válido. Opciones: " + String.join(", ", tiposEntrega())));
    }

    // Opcional: si viene vacío se usa el primero (igual que el checkout del frontend).
    public MetodoPago metodoPago(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return metodoPagoRepository.findAllByOrderByIdAsc().get(0);
        }
        return metodoPagoRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "El método de pago no es válido. Opciones: " + String.join(", ", metodosPago())));
    }

    public List<String> estados() {
        return estadoRepository.findAllByOrderByIdAsc().stream().map(Estado::getNombre).toList();
    }

    public List<String> tiposEntrega() {
        return tipoEntregaRepository.findAllByOrderByIdAsc().stream().map(TipoEntrega::getNombre).toList();
    }

    public List<String> metodosPago() {
        return metodoPagoRepository.findAllByOrderByIdAsc().stream().map(MetodoPago::getNombre).toList();
    }
}