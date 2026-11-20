package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

// Tabla "region" del DER: las 16 regiones de Chile.
@Entity
@Table(name = "region", uniqueConstraints = @UniqueConstraint(name = "uk_region_nombre", columnNames = "nombre"))
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region")
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nombre;

    protected Region() { }

    public Region(String nombre) {
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}