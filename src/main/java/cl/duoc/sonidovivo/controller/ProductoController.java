package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.ErrorResponse;
import cl.duoc.sonidovivo.dto.ProductoRequest;
import cl.duoc.sonidovivo.dto.ProductoResponse;
import cl.duoc.sonidovivo.dto.StockRequest;
import cl.duoc.sonidovivo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "4. Productos", description = "Catálogo de instrumentos. Lectura pública; escritura solo Administrador y Vendedor. Se identifican por código (GA001)")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Público. Filtros opcionales: ?categoria=Bajos &oferta=true &q=yamaha. Pantallas: Catálogo, Ofertas, Categorías, Admin → Productos / Stock crítico.")
    public List<ProductoResponse> listar(@RequestParam(required = false) String categoria,
                                         @RequestParam(required = false) Boolean oferta,
                                         @RequestParam(required = false) String q) {
        return productoService.listar(categoria, oferta, q);
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Ver producto por código", description = "Público. Pantalla: Detalle de producto.")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProductoResponse obtener(@PathVariable String codigo) {
        return productoService.obtener(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Crear producto", description = "Administrador o Vendedor. Pantalla: Admin → Productos.")
    @ApiResponse(responseCode = "409", description = "Código duplicado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest req) {
        return productoService.crear(req);
    }

    @PutMapping("/{codigo}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Editar producto", description = "El código no se puede modificar (se toma de la URL).")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ProductoResponse actualizar(@PathVariable String codigo, @Valid @RequestBody ProductoRequest req) {
        return productoService.actualizar(codigo, req);
    }

    @PatchMapping("/{codigo}/stock")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Actualizar stock", description = "Reposición rápida. Pantalla: Admin → Stock crítico.")
    public ProductoResponse actualizarStock(@PathVariable String codigo, @Valid @RequestBody StockRequest req) {
        return productoService.actualizarStock(codigo, req.stock());
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Eliminar producto", description = "Lo oculta del catálogo (activo=false); los pedidos antiguos lo conservan.")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public void eliminar(@PathVariable String codigo) {
        productoService.eliminar(codigo);
    }
}
