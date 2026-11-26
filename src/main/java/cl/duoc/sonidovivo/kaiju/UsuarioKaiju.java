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
    private RolKaiju rol;                  // "BODEGA", "VENTAS" o "ADMIN"

    // si es false, no puede iniciar sesión. "default true" deja activos a los que ya existían
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

    // ----- NUEVO: permisos (los edita el administrador) -----
    @Column(name = "permiso_entradas", nullable = false, columnDefinition = "boolean default false")
    private boolean permisoEntradas;       // registrar entradas de stock

    @Column(name = "permiso_salidas", nullable = false, columnDefinition = "boolean default false")
    private boolean permisoSalidas;        // registrar salidas de stock

    @Column(name = "permiso_precios", nullable = false, columnDefinition = "boolean default false")
    private boolean permisoPrecios;        // editar precios

    @Column(name = "permiso_reportes", nullable = false, columnDefinition = "boolean default false")
    private boolean permisoReportes;       // ver reportes

    // false = usuario creado antes de existir los permisos: al arrancar se le asignan los de su rol
    @Column(name = "permisos_definidos", nullable = false, columnDefinition = "boolean default false")
    private boolean permisosDefinidos;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    protected UsuarioKaiju() { }           // JPA lo necesita

    public UsuarioKaiju(String rut, String nombreCompleto, String email, String passwordHash, RolKaiju rol) {
        this.rut = rut;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        aplicarPermisosDeRol();            // todo usuario nuevo parte con los permisos de su rol
    }

    // Permisos por defecto de cada rol (el admin puede cambiarlos después, usuario por usuario)
    public void aplicarPermisosDeRol() {
        switch (rol) {
            case ADMIN -> asignarPermisos(true, true, true, true);
            case BODEGA -> asignarPermisos(true, true, false, false);   // entradas y salidas
            case VENTAS -> asignarPermisos(false, true, false, true);   // salidas y reportes
        }
    }

    // El ADMIN siempre tiene todos los permisos: no se le pueden quitar
    public void asignarPermisos(boolean entradas, boolean salidas, boolean precios, boolean reportes) {
        if (rol == RolKaiju.ADMIN) {
            entradas = salidas = precios = reportes = true;
        }
        this.permisoEntradas = entradas;
        this.permisoSalidas = salidas;
        this.permisoPrecios = precios;
        this.permisoReportes = reportes;
        this.permisosDefinidos = true;
    }

    public Long getId() { return id; }
    public String getRut() { return rut; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public RolKaiju getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public boolean isPermisoEntradas() { return permisoEntradas; }
    public boolean isPermisoSalidas() { return permisoSalidas; }
    public boolean isPermisoPrecios() { return permisoPrecios; }
    public boolean isPermisoReportes() { return permisoReportes; }
    public boolean isPermisosDefinidos() { return permisosDefinidos; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    // lo que el administrador puede cambiar
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setEmail(String email) { this.email = email; }
    public void setRol(RolKaiju rol) { this.rol = rol; }
    public void setActivo(boolean activo) { this.activo = activo; }
}