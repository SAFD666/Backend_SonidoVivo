package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, Integer> {
    Optional<Comuna> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreAndRegionId(String nombre, Integer regionId);
    List<Comuna> findAllByOrderByNombreAsc();
}