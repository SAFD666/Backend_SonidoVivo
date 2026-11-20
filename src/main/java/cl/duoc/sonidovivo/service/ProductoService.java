package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.ProductoRequest;
import cl.duoc.sonidovivo.dto.ProductoResponse;
import cl.duoc.sonidovivo.exception.ConflictoException;
import cl.duoc.sonidovivo.exception.RecursoNoEncontradoException;
import cl.duoc.sonidovivo.exception.SolicitudInvalidaException;
import cl.duoc.sonidovivo.model.Categoria;
import cl.duoc.sonidovivo.model.Producto;
import cl.duoc.sonidovivo.repository.CategoriaRepository;
import cl.duoc.sonidovivo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

// CRUD de productos (equivale a ProductosService.js del frontend, pero en PostgreSQL).
// Productos identificados por su codigo (GA001...); eliminar oculta el registro (activo = false).
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaService marcaService;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                           MarcaService marcaService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaService = marcaService;
    }

    // Lista publica con filtros opcionales: categoria (por nombre o slug), solo ofertas y busqueda por texto.
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String categoria, Boolean soloOfertas, String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return productoRepository.findByActivoTrueOrderByCodigoAsc().stream()
                .filter(p -> categoria == null || categoria.isBlank()
                        || p.getCategoria().getNombre().equalsIgnoreCase(categoria.trim())
                        || p.getCategoria().getSlug().equals(categoria.trim()))
                .filter(p -> soloOfertas == null || !soloOfertas || p.isEsOferta())
                .filter(p -> q.isEmpty()
                        || p.getNombre().toLowerCase(Locale.ROOT).contains(q)
                        || p.getMarca().getNombre().toLowerCase(Locale.ROOT).contains(q)
                        || (p.getModelo() != null && p.getModelo().toLowerCase(Locale.ROOT).contains(q))
                        || p.getCodigo().toLowerCase(Locale.ROOT).contains(q))
                .map(ProductoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(String codigo) {
        return ProductoResponse.desde(buscarActivo(codigo));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        if (req.codigo() == null || req.codigo().isBlank()) {
            throw new SolicitudInvalidaException("El código es obligatorio");
        }
        String codigo = req.codigo().trim().toUpperCase(Locale.ROOT);
        Categoria categoria = buscarCategoria(req.categoria());

        Producto p = productoRepository.findByCodigo(codigo).orElse(null);
        if (p != null && p.isActivo()) {
            throw new ConflictoException("Ya existe un producto con ese código");
        }
        if (p == null) {
            p = new Producto();
            p.setCodigo(codigo);
        }
        // Si el código pertenecía a un producto eliminado (oculto), se reutiliza.
        p.setActivo(true);
        copiarDatos(p, req, categoria);
        return ProductoResponse.desde(productoRepository.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(String codigo, ProductoRequest req) {
        Producto p = buscarActivo(codigo);
        copiarDatos(p, req, buscarCategoria(req.categoria()));
        return ProductoResponse.desde(p);
    }

    @Transactional
    public ProductoResponse actualizarStock(String codigo, int stock) {
        Producto p = buscarActivo(codigo);
        p.setStock(stock);
        return ProductoResponse.desde(p);
    }

    @Transactional
    public void eliminar(String codigo) {
        buscarActivo(codigo).setActivo(false);
    }

    private void copiarDatos(Producto p, ProductoRequest req, Categoria categoria) {
        p.setNombre(req.nombre().trim());
        // La marca llega como texto; si no existe en la tabla marca, se crea (etapa 1 del DER).
        p.setMarca(marcaService.obtenerOCrear(req.marca()));
        p.setModelo(req.modelo() == null ? null : req.modelo().trim());
        p.setDescripcion(req.descripcion() == null ? null : req.descripcion().trim());
        p.setPrecio(req.precio());
        p.setStock(req.stock());
        p.setEsOferta(Boolean.TRUE.equals(req.esOferta()));
        p.setCategoria(categoria);
        // Sin imagen propia se usa la de la categoría (igual que el formulario del frontend).
        p.setImagen(req.imagen() == null || req.imagen().isBlank() ? categoria.getImagen() : req.imagen().trim());
    }

    private Categoria buscarCategoria(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new SolicitudInvalidaException("La categoría \"" + nombre + "\" no existe"));
    }

    private Producto buscarActivo(String codigo) {
        return productoRepository.findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + codigo + " no encontrado"));
    }
}