package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;

// Tabla "direccion" del DER: dirección de despacho de un usuario, ubicada en una comuna.
@Entity
@Table(name = "direccion")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long id;

    @Column(nullable = false, length = 100)
    private String calle;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(length = 20)
    private String depto;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_comuna", nullable = false, foreignKey = @ForeignKey(name = "fk_direccion_comuna"))
    private Comuna comuna;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "fk_direccion_usuario"))
    private Usuario usuario;

    protected Direccion() { }

    public Direccion(String calle, String numero, String depto, Comuna comuna, Usuario usuario) {
        this.calle = calle;
        this.numero = numero;
        this.depto = depto;
        this.comuna = comuna;
        this.usuario = usuario;
    }

    // Texto para mostrar, igual al formato del frontend: "Av. Libertad 450, Viña del Mar"
    public String texto() {
        String conDepto = depto == null || depto.isBlank() ? "" : ", depto " + depto;
        return calle + " " + numero + conDepto + ", " + comuna.getNombre();
    }

    public Long getId() { return id; }
    public String getCalle() { return calle; }
    public String getNumero() { return numero; }
    public String getDepto() { return depto; }
    public String getCodigoPostal() { return codigoPostal; }
    public Comuna getComuna() { return comuna; }
    public Usuario getUsuario() { return usuario; }
}