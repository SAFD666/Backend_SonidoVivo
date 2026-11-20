package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.MarcaResponse;
import cl.duoc.sonidovivo.service.MarcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marcas")
@Tag(name = "Marcas", description = "Marcas del catálogo (se crean solas al guardar un producto)")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping
    @Operation(summary = "Listar marcas", description = "Público. Pantalla: filtro por marca en Ofertas.")
    public List<MarcaResponse> listar() {
        return marcaService.listar();
    }
}