package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Tabla "usuario" del DER: correo unico y contrasena solo como hash BCrypt; eliminar desactiva (activo = false)
// para preservar pedidos asociados. Las direcciones van en la tabla "direccion".
@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol", nullable = false, foreignKey = @ForeignKey(name = "fk_usuario_rol"))
    private Rol rol;

    @Column(name = "fecha_ingreso", nullable = false, updatable = false)
    private LocalDateTime fechaIngreso;

    @PrePersist
    void alCrear() {
        this.fechaIngreso = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
}