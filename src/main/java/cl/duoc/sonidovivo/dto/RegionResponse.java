package cl.duoc.sonidovivo.dto;

import java.util.List;

// Región con sus comunas, para los selectores "Región" y "Comuna" del checkout.
public record RegionResponse(Integer id, String nombre, List<ComunaResponse> comunas) {

    public record ComunaResponse(Integer id, String nombre) { }
}