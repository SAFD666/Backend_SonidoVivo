package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.OpcionesPedidoResponse;
import cl.duoc.sonidovivo.dto.PedidoRequest;
import cl.duoc.sonidovivo.dto.PedidoResponse;
import cl.duoc.sonidovivo.exception.ConflictoException;
import cl.duoc.sonidovivo.exception.RecursoNoEncontradoException;
import cl.duoc.sonidovivo.exception.SolicitudInvalidaException;
import cl.duoc.sonidovivo.model.*;
import cl.duoc.sonidovivo.repository.PedidoRepository;
import cl.duoc.sonidovivo.repository.ProductoRepository;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import cl.duoc.sonidovivo.security.UsuarioAutenticado;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

// Reglas de pedidos (equivalen a crearPedido / cambiarEstadoPedido en TiendaContext, pero en servidor).
// El pedido pertenece al usuario del token: cliente ve solo los suyos; Administrador y Vendedor ven todos.
// Precios y total los calcula el servidor con los precios actuales de la base.
// Regla de stock: no supera lo disponible (409) y bloquea filas (SELECT ... FOR UPDATE) contra compras simultaneas.
// Al confirmar se descuenta el stock y al cancelar se devuelve.
// Un pedido cancelado no puede cambiar a otro estado (409) porque su stock ya fue devuelto.
@Service
public class PedidoService {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public OpcionesPedidoResponse opciones() {
        return new OpcionesPedidoResponse(
                OpcionesPedido.TIPOS_ENTREGA,
                OpcionesPedido.METODOS_PAGO,
                Arrays.stream(EstadoPedido.values()).map(EstadoPedido::getEtiqueta).toList());
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest req, UsuarioAutenticado yo) {
        String tipoEntrega = elegir(OpcionesPedido.TIPOS_ENTREGA, req.tipoEntrega(), "tipo de entrega", true);
        String metodoPago = elegir(OpcionesPedido.METODOS_PAGO, req.metodoPago(), "método de pago", false);
        Usuario usuario = usuarioRepository.findById(yo.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Une líneas repetidas del mismo producto y las ordena por código.
        Map<String, Integer> cantidades = new TreeMap<>();
        req.items().forEach(i -> cantidades.merge(i.codigo().trim().toUpperCase(Locale.ROOT), i.cantidad(), Integer::sum));

        Map<String, Producto> productos = productoRepository.bloquearPorCodigos(cantidades.keySet()).stream()
                .collect(Collectors.toMap(Producto::getCodigo, p -> p));

        Pedido pedido = new Pedido();
        int total = 0;
        for (Map.Entry<String, Integer> linea : cantidades.entrySet()) {
            Producto p = productos.get(linea.getKey());
            if (p == null || !p.isActivo()) {
                throw new RecursoNoEncontradoException("Producto " + linea.getKey() + " no encontrado");
            }
            int pedida = linea.getValue();
            if (pedida > p.getStock()) {
                throw new ConflictoException("Stock insuficiente para " + p.getNombre()
                        + ": solicitaste " + pedida + " y hay " + p.getStock() + " disponible(s)");
            }
            p.setStock(p.getStock() - pedida);
            DetallePedido detalle = new DetallePedido(p, pedida);
            pedido.agregarDetalle(detalle);
            total += detalle.getSubtotal();
        }

        pedido.setUsuario(usuario);
        pedido.setClienteCorreo(usuario.getCorreo());
        pedido.setClienteNombre(req.nombre().trim());
        pedido.setTelefono(req.telefono().replaceAll("\\s", ""));
        pedido.setDireccion(req.direccion().trim());
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setMetodoPago(metodoPago);
        pedido.setFecha(LocalDate.now(ZONA));
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        pedido.setTotal(total);

        pedidoRepository.saveAndFlush(pedido);
        pedido.setCodigo(codigoDe(pedido.getId()));
        return PedidoResponse.desde(pedido);
    }

    // Cliente: ve solo sus pedidos; Administrador/Vendedor: ven todos (con filtro opcional por usuario).
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioAutenticado yo, Long usuarioId, EstadoPedido estado) {
        List<Pedido> pedidos;
        if (yo.esPersonal()) {
            pedidos = usuarioId != null
                    ? pedidoRepository.findByUsuarioIdOrderByIdDesc(usuarioId)
                    : pedidoRepository.findAllByOrderByIdDesc();
        } else {
            if (usuarioId != null && !usuarioId.equals(yo.id())) {
                throw new AccessDeniedException("Solo puedes ver tus propios pedidos");
            }
            pedidos = pedidoRepository.findByUsuarioIdOrderByIdDesc(yo.id());
        }
        return pedidos.stream()
                .filter(p -> estado == null || p.getEstado() == estado)
                .map(PedidoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(String codigo, UsuarioAutenticado yo) {
        Pedido p = buscar(codigo);
        verificarAcceso(p, yo);
        return PedidoResponse.desde(p);
    }

    // Administrador / Vendedor cambian el estado; cancelar devuelve el stock.
    @Transactional
    public PedidoResponse cambiarEstado(String codigo, EstadoPedido nuevo) {
        Pedido p = buscar(codigo);
        EstadoPedido actual = p.getEstado();
        if (actual == nuevo) {
            return PedidoResponse.desde(p);
        }
        if (actual == EstadoPedido.CANCELADO) {
            throw new ConflictoException("Un pedido cancelado no puede cambiar de estado");
        }
        if (nuevo == EstadoPedido.CANCELADO) {
            devolverStock(p);
        }
        p.setEstado(nuevo);
        return PedidoResponse.desde(p);
    }

    // Cliente puede cancelar su pedido solo mientras esta "En preparacion".
    @Transactional
    public PedidoResponse cancelar(String codigo, UsuarioAutenticado yo) {
        Pedido p = buscar(codigo);
        if (!yo.esPersonal() && !p.getUsuario().getId().equals(yo.id())) {
            throw new AccessDeniedException("Solo puedes cancelar tus propios pedidos");
        }
        if (p.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new ConflictoException("Solo se puede cancelar un pedido que está En preparación");
        }
        devolverStock(p);
        p.setEstado(EstadoPedido.CANCELADO);
        return PedidoResponse.desde(p);
    }

    private void devolverStock(Pedido p) {
        Map<Long, Integer> porProducto = new HashMap<>();
        p.getDetalles().forEach(d -> porProducto.merge(d.getProducto().getId(), d.getCantidad(), Integer::sum));
        productoRepository.bloquearPorIds(porProducto.keySet())
                .forEach(prod -> prod.setStock(prod.getStock() + porProducto.get(prod.getId())));
    }

    private void verificarAcceso(Pedido p, UsuarioAutenticado yo) {
        if (!yo.esPersonal() && !p.getUsuario().getId().equals(yo.id())) {
            throw new AccessDeniedException("Solo puedes ver tus propios pedidos");
        }
    }

    private Pedido buscar(String codigo) {
        return pedidoRepository.findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido " + codigo + " no encontrado"));
    }

    private String elegir(List<String> validos, String valor, String campo, boolean obligatorio) {
        if (valor == null || valor.isBlank()) {
            if (obligatorio) {
                throw new SolicitudInvalidaException("El " + campo + " es obligatorio");
            }
            return validos.get(0);
        }
        return validos.stream().filter(v -> v.equalsIgnoreCase(valor.trim())).findFirst()
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "El " + campo + " no es válido. Opciones: " + String.join(", ", validos)));
    }

    public static String codigoDe(Long id) {
        return "PED-" + (1000 + id);
    }
}
