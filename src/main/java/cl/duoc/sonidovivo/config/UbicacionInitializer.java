package cl.duoc.sonidovivo.config;

import cl.duoc.sonidovivo.service.UbicacionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Carga regiones y comunas al arrancar, antes que DataInitializer (Order 0 = primero).
@Component
@Order(0)
public class UbicacionInitializer implements CommandLineRunner {

    private final UbicacionService ubicacionService;

    public UbicacionInitializer(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @Override
    public void run(String... args) {
        ubicacionService.cargarSiFalta();
    }
}