package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

// Tabla "estado_pedido"  Enviado, Entregado, Cancelado.
@Entity
@Table(name = "estado_pedido", uniqueConstraints = @UniqueConstraint(name = "uk_estado_nombre", columnNames = "nombre"))
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer id;

    @Column(nullable = false, length = 30)
    private String nombre;

    protected Estado() { }

    public Estado(String nombre) {
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}