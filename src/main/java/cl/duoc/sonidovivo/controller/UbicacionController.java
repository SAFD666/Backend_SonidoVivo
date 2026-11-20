package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.RegionResponse;
import cl.duoc.sonidovivo.service.UbicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regiones")
@Tag(name = "Ubicaciones", description = "Regiones y comunas de Chile para la dirección de despacho")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping
    @Operation(summary = "Listar regiones con sus comunas", description = "Público. Pantalla: Checkout (selectores Región y Comuna).")
    public List<RegionResponse> listar() {
        return ubicacionService.listar();
    }
}