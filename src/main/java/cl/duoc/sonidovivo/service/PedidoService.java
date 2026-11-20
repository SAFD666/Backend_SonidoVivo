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
// Estado, tipo de entrega y método de pago se leen de sus tablas (CatalogoPedidoService).
// La dirección es obligatoria para despacho y se guarda en la tabla direccion (UbicacionService).
@Service
public class PedidoService {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoPedidoService catalogo;
    private final UbicacionService ubicacionService;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository, CatalogoPedidoService catalogo,
                         UbicacionService ubicacionService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogo = catalogo;
        this.ubicacionService = ubicacionService;
    }

    @Transactional(readOnly = true)
    public OpcionesPedidoResponse opciones() {
        return new OpcionesPedidoResponse(catalogo.tiposEntrega(), catalogo.metodosPago(), catalogo.estados());
    }

    // "Retiro en Tienda (Viña del Mar)" no necesita dirección de despacho
    public static boolean esRetiro(TipoEntrega tipo) {
        return tipo.getNombre().toLowerCase(Locale.ROOT).startsWith("retiro");
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest req, UsuarioAutenticado yo) {
        TipoEntrega tipoEntrega = catalogo.tipoEntrega(req.tipoEntrega());
        MetodoPago metodoPago = catalogo.metodoPago(req.metodoPago());
        Usuario usuario = usuarioRepository.findById(yo.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Direccion direccion = null;
        if (!esRetiro(tipoEntrega)) {
            if (req.direccion() == null) {
                throw new SolicitudInvalidaException("La dirección es obligatoria para " + tipoEntrega.getNombre());
            }
            direccion = ubicacionService.obtenerOCrearDireccion(usuario, req.direccion());
        }

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
        pedido.setDireccion(direccion);
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setMetodoPago(metodoPago);
        pedido.setFecha(LocalDate.now(ZONA));
        pedido.setEstado(catalogo.estado(EstadoPedido.EN_PREPARACION));
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
        p.setEstado(catalogo.estado(nuevo));
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
        p.setEstado(catalogo.estado(EstadoPedido.CANCELADO));
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

    public static String codigoDe(Long id) {
        return "PED-" + (1000 + id);
    }
}