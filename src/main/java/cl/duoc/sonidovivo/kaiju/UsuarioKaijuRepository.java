package cl.duoc.sonidovivo.kaiju;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Consultas a la tabla usuario_kaiju (Spring escribe el SQL a partir del nombre del método)
public interface UsuarioKaijuRepository extends JpaRepository<UsuarioKaiju, Long> {

    Optional<UsuarioKaiju> findByEmail(String email);    // para el login

    boolean existsByEmail(String email);                 // para no repetir email al registrar

    boolean existsByRut(String rut);                     // para no repetir RUT al registrar
}