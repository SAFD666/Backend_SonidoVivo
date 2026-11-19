package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = "detalles")
    Optional<Pedido> findByCodigo(String codigo);
}
