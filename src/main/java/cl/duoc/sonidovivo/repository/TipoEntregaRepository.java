package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.TipoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoEntregaRepository extends JpaRepository<TipoEntrega, Integer> {
    Optional<TipoEntrega> findByNombreIgnoreCase(String nombre);
    List<TipoEntrega> findAllByOrderByIdAsc();
}