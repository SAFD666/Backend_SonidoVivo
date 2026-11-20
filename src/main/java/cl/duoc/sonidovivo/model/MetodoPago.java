package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

// Tabla "metodo_pago" : Tarjeta de crédito, Tarjeta de débito, Transferencia bancaria.
@Entity
@Table(name = "metodo_pago", uniqueConstraints = @UniqueConstraint(name = "uk_metodo_pago_nombre", columnNames = "nombre"))
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nombre;

    protected MetodoPago() { }

    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}