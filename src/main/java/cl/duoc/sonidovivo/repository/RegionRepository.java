package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Integer> {
    Optional<Region> findByNombre(String nombre);
    List<Region> findAllByOrderByIdAsc();
}