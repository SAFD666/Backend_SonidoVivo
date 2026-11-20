package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    // Para reutilizar una dirección que el usuario ya había usado
    Optional<Direccion> findFirstByUsuarioIdAndCalleIgnoreCaseAndNumeroAndComunaId(
            Long usuarioId, String calle, String numero, Integer comunaId);
}