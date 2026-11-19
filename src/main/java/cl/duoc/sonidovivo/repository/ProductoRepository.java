package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrueOrderByCodigoAsc();

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    // Bloquea filas (SELECT ... FOR UPDATE) ordenadas por codigo al confirmar un pedido para evitar deadlocks en compras simultaneas.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.codigo in :codigos order by p.codigo")
    List<Producto> bloquearPorCodigos(@Param("codigos") Collection<String> codigos);

    // Restituye stock al cancelar un pedido bloqueando tambien la fila.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids order by p.id")
    List<Producto> bloquearPorIds(@Param("ids") Collection<Long> ids);
}
