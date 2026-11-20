package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

// Tabla "comuna" : cada comuna pertenece a una región; no se repite dentro de la misma región.
@Entity
@Table(name = "comuna", uniqueConstraints = @UniqueConstraint(name = "uk_comuna_region", columnNames = {"nombre", "id_region"}))
public class Comuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comuna")
    private Integer id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_region", nullable = false, foreignKey = @ForeignKey(name = "fk_comuna_region"))
    private Region region;

    protected Comuna() { }

    public Comuna(String nombre, Region region) {
        this.nombre = nombre;
        this.region = region;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public Region getRegion() { return region; }
}