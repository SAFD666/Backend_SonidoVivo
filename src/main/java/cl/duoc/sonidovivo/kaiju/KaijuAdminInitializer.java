package cl.duoc.sonidovivo.kaiju;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Se ejecuta al arrancar el backend:
//  1) usuarios creados antes de existir los permisos -> reciben los de su rol (una sola vez)
//  2) garantiza que Kaiju tenga un administrador
@Component
public class KaijuAdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KaijuAdminInitializer.class);

    static final String EMAIL_ADMIN = "admin@kaiju.cl";
    static final String RUT_ADMIN = "11111111-1";          // RUT ficticio válido (módulo 11)

    private final UsuarioKaijuRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String claveAdmin;

    // La clave sale de la variable KAIJU_ADMIN_PASSWORD del .env; si no está, usa la de respaldo
    public KaijuAdminInitializer(UsuarioKaijuRepository repository, PasswordEncoder passwordEncoder,
                                 @Value("${KAIJU_ADMIN_PASSWORD:Kaiju2026!}") String claveAdmin) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.claveAdmin = claveAdmin;
    }

    @Override
    public void run(String... args) {
        completarPermisos();
        crearAdminSiFalta();
    }

    // 1) Permisos por defecto para los usuarios que aún no los tienen
    private void completarPermisos() {
        List<UsuarioKaiju> pendientes = repository.findAll().stream()
                .filter(u -> !u.isPermisosDefinidos())
                .toList();
        if (pendientes.isEmpty()) return;

        pendientes.forEach(UsuarioKaiju::aplicarPermisosDeRol);
        repository.saveAll(pendientes);
        log.info("Kaiju: permisos por defecto asignados a {} usuario(s)", pendientes.size());
    }

    // 2) Administrador por defecto (si no existe ninguno)
    private void crearAdminSiFalta() {
        if (repository.existsByRol(RolKaiju.ADMIN)) {
            return;                                         // ya hay administrador
        }
        // si ese email o RUT ya existían como otro rol, no se crea un duplicado
        if (repository.existsByEmail(EMAIL_ADMIN) || repository.existsByRut(RUT_ADMIN)) {
            log.warn("Kaiju: no hay administrador y {} o {} ya están usados. Asigna ADMIN a mano.", EMAIL_ADMIN, RUT_ADMIN);
            return;
        }
        repository.save(new UsuarioKaiju(
                RUT_ADMIN,
                "Administrador Kaiju",
                EMAIL_ADMIN,
                passwordEncoder.encode(claveAdmin),
                RolKaiju.ADMIN
        ));
        log.info("Kaiju: administrador por defecto creado ({})", EMAIL_ADMIN);
    }
}