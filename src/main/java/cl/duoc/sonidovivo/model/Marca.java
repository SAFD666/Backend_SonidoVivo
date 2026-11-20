package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

// Tabla "marca" del DER: cada marca se escribe una sola vez y los productos la referencian.
@Entity
@Table(name = "marca", uniqueConstraints = @UniqueConstraint(name = "uk_marca_nombre", columnNames = "nombre"))
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    protected Marca() { }

    public Marca(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}