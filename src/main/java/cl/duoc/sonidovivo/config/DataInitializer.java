package cl.duoc.sonidovivo.config;

import cl.duoc.sonidovivo.model.*;
import cl.duoc.sonidovivo.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.sonidovivo.service.MarcaService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga los datos iniciales del frontend SOLO si faltan (no duplica nada al reiniciar):
 *  1. Los tres roles.
 *  2. Las 9 categorías y los 50 productos de src/data (archivos en resources/seed).
 *  3. Cuentas de demostración (las mismas del frontend) si se definió DEMO_PASSWORD.
 *  4. Los 2 pedidos de ejemplo del cliente de prueba, si todavía no hay pedidos.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    record SeedCategoria(String nombre, String imagen) { }

    record SeedProducto(String codigo, String nombre, String marca, String modelo, String categoria,
                                int stock, int precio, String imagen, String descripcion, boolean esOferta) { }

    record SeedItem(String codigo, int cantidad) { }

    record SeedPedido(String id, LocalDate fecha, String clienteCorreo, String clienteNombre,
                              String direccion, String telefono, String tipoEntrega, String estado,
                              List<SeedItem> items) { }

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final String demoPassword;
    private final MarcaService marcaService;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                           PedidoRepository pedidoRepository, PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper,
                            @Value("${app.seed.demo-password:}") String demoPassword,
                           MarcaService marcaService) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.demoPassword = demoPassword;
        this.marcaService = marcaService;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        crearRolSiFalta(NombreRol.ADMINISTRADOR, "Gestiona usuarios, roles, catálogo y pedidos");
        crearRolSiFalta(NombreRol.VENDEDOR, "Gestiona productos, categorías, stock y estados de pedidos");
        crearRolSiFalta(NombreRol.CLIENTE, "Compra productos y ve solo sus pedidos");

        cargarCategorias();
        cargarProductos();

        if (demoPassword == null || demoPassword.isBlank()) {
            log.warn("DEMO_PASSWORD no está definido: no se crean cuentas de demostración.");
            return;
        }
        // Mismas cuentas que src/data/usuarios.js del frontend (la contraseña la define DEMO_PASSWORD).
        crearUsuarioSiFalta("Carlos Dueño (Admin)", "admin@sonidovivo.cl", NombreRol.ADMINISTRADOR);
        crearUsuarioSiFalta("Matías Vendedor", "vendedor@sonidovivo.cl", NombreRol.VENDEDOR);
        crearUsuarioSiFalta("Cliente Prueba", "cliente@correo.cl", NombreRol.CLIENTE);

        cargarPedidosDeEjemplo();
    }

    private void crearRolSiFalta(NombreRol nombre, String descripcion) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            rolRepository.save(new Rol(nombre, descripcion));
        }
    }

    private void crearUsuarioSiFalta(String nombre, String correo, NombreRol nombreRol) {
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            return;
        }
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPasswordHash(passwordEncoder.encode(demoPassword));
        u.setRol(rolRepository.findByNombre(nombreRol).orElseThrow());
        usuarioRepository.save(u);
        log.info("Usuario de demostración creado: {} ({})", correo, nombreRol);
    }

    private void cargarCategorias() throws IOException {
        for (SeedCategoria c : leer("seed/categorias.json", new TypeReference<List<SeedCategoria>>() { })) {
            if (!categoriaRepository.existsByNombreIgnoreCase(c.nombre())) {
                categoriaRepository.save(new Categoria(c.nombre(), c.imagen()));
            }
        }
    }

    private void cargarProductos() throws IOException {
        int nuevos = 0;
        for (SeedProducto s : leer("seed/productos.json", new TypeReference<List<SeedProducto>>() { })) {
            if (productoRepository.existsByCodigo(s.codigo())) {
                continue;
            }
            Producto p = new Producto();
            p.setCodigo(s.codigo());
            p.setNombre(s.nombre());
            p.setMarca(marcaService.obtenerOCrear(s.marca()));
            p.setModelo(s.modelo());
            p.setDescripcion(s.descripcion());
            p.setPrecio(s.precio());
            p.setStock(s.stock());
            p.setImagen(s.imagen());
            p.setEsOferta(s.esOferta());
            p.setCategoria(categoriaRepository.findByNombreIgnoreCase(s.categoria())
                    .orElseThrow(() -> new IllegalStateException("Categoría inexistente en seed: " + s.categoria())));
            productoRepository.save(p);
            nuevos++;
        }
        if (nuevos > 0) {
            log.info("Productos iniciales cargados: {}", nuevos);
        }
    }

    private void cargarPedidosDeEjemplo() throws IOException {
        if (pedidoRepository.count() > 0) {
            return;
        }
        for (SeedPedido s : leer("seed/pedidos.json", new TypeReference<List<SeedPedido>>() { })) {
            Usuario cliente = usuarioRepository.findByCorreoIgnoreCase(s.clienteCorreo()).orElse(null);
            if (cliente == null) {
                continue;
            }
            Map<String, Producto> cache = new HashMap<>();
            Pedido pedido = new Pedido();
            int total = 0;
            for (SeedItem item : s.items()) {
                Producto prod = cache.computeIfAbsent(item.codigo(), c -> productoRepository.findByCodigo(c).orElse(null));
                if (prod == null) {
                    continue;
                }
                DetallePedido d = new DetallePedido(prod, item.cantidad());
                pedido.agregarDetalle(d);
                total += d.getSubtotal();
            }
            pedido.setCodigo(s.id());
            pedido.setFecha(s.fecha());
            pedido.setUsuario(cliente);
            pedido.setClienteCorreo(s.clienteCorreo());
            pedido.setClienteNombre(s.clienteNombre());
            pedido.setDireccion(s.direccion());
            pedido.setTelefono(s.telefono());
            pedido.setTipoEntrega(s.tipoEntrega());
            pedido.setMetodoPago(OpcionesPedido.METODOS_PAGO.get(0));
            pedido.setEstado(EstadoPedido.desdeTexto(s.estado()));
            pedido.setTotal(total);
            pedidoRepository.save(pedido);
        }
        log.info("Pedidos de ejemplo cargados");
    }

    private <T> T leer(String ruta, TypeReference<T> tipo) throws IOException {
        try (InputStream in = new ClassPathResource(ruta).getInputStream()) {
            return objectMapper.readValue(in, tipo);
        }
    }
}
