package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

// Tabla "tipo_entrega" del DER: Retiro en Tienda, Despacho Starken, Despacho Chilexpress.
@Entity
@Table(name = "tipo_entrega", uniqueConstraints = @UniqueConstraint(name = "uk_tipo_entrega_nombre", columnNames = "nombre"))
public class TipoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_entrega")
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nombre;

    protected TipoEntrega() { }

    public TipoEntrega(String nombre) {
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}