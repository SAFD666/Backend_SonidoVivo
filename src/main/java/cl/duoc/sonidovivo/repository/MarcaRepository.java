package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    Optional<Marca> findByNombreIgnoreCase(String nombre);
    List<Marca> findAllByOrderByNombreAsc();
}