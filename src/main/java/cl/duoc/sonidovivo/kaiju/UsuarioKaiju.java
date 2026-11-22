package cl.duoc.sonidovivo.kaiju;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Usuarios de la app Kaiju: tabla propia, separada de "usuario" de Sonido Vivo
@Entity
@Table(name = "usuario_kaiju")
public class UsuarioKaiju {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_kaiju")
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;                    // "12345678-5" (normalizado)

    @Column(name = "nombre_completo", nullable = false, length = 80)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 120)
    private String email;                  // siempre en minúsculas

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;           // BCrypt, nunca la contraseña real

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RolKaiju rol;                  // se guarda como texto: "BODEGA" (solo informativo)

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    protected UsuarioKaiju() { }           // JPA lo necesita

    public UsuarioKaiju(String rut, String nombreCompleto, String email, String passwordHash, RolKaiju rol) {
        this.rut = rut;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getRut() { return rut; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public RolKaiju getRol() { return rol; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}