package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.CategoriaRequest;
import cl.duoc.sonidovivo.dto.CategoriaResponse;
import cl.duoc.sonidovivo.dto.ErrorResponse;
import cl.duoc.sonidovivo.service.CategoriaService;
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
@RequestMapping("/api/v1/categorias")
@Tag(name = "3. Categorías", description = "Categorías del catálogo. Lectura pública; escritura solo Administrador y Vendedor")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Público. Pantallas: Inicio, Categorías, Catálogo.")
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Ver categoría por slug", description = "Público. Pantalla: /categoria/:slug del frontend.")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CategoriaResponse obtenerPorSlug(@PathVariable String slug) {
        return categoriaService.obtenerPorSlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Crear categoría", description = "Administrador o Vendedor. El slug se genera solo. Pantalla: Admin → Categorías.")
    @ApiResponse(responseCode = "409", description = "Nombre o URL duplicados", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest req) {
        return categoriaService.crear(req);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Editar / renombrar categoría", description = "Los productos de la categoría quedan con el nuevo nombre y el slug se actualiza.")
    @ApiResponse(responseCode = "404", description = "No existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Nombre o URL duplicados", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.actualizar(id, req);
    }
}