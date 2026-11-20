package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    Optional<Estado> findByNombre(String nombre);
    List<Estado> findAllByOrderByIdAsc();
}