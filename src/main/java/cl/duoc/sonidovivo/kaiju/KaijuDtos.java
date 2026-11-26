package cl.duoc.sonidovivo.kaiju;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// Todos los objetos que entran y salen de la API de Kaiju, agrupados en un solo archivo
public final class KaijuDtos {

    private KaijuDtos() { }

    // Lo que envía la app al registrarse (SIN rol: el backend siempre asigna BODEGA)
    public record RegistroRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80, message = "Máximo 80 caracteres")
            String nombreCompleto,

            @NotBlank(message = "El RUT es obligatorio")
            String rut,

            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Formato de email inválido")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 6, message = "Debe tener al menos 6 caracteres")
            String password
    ) { }

    // Lo que envía la app al iniciar sesión
    public record LoginRequest(
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Formato de email inválido")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            String password
    ) { }

    // Lo que envía el administrador al editar un usuario.
    // Permisos OPCIONALES: si no vienen y cambió el rol, se aplican los del rol nuevo.
    public record ActualizarUsuarioRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80, message = "Máximo 80 caracteres")
            String nombreCompleto,

            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Formato de email inválido")
            String email,

            @NotNull(message = "El rol es obligatorio")
            RolKaiju rol,

            @NotNull(message = "Indica si la cuenta está activa")
            Boolean activo,

            Boolean permisoEntradas,
            Boolean permisoSalidas,
            Boolean permisoPrecios,
            Boolean permisoReportes
    ) {
        public boolean traePermisos() {
            return permisoEntradas != null && permisoSalidas != null
                    && permisoPrecios != null && permisoReportes != null;
        }
    }

    // Datos del usuario que devuelve la API (nunca la contraseña)
    public record UsuarioResponse(Long id, String rut, String nombreCompleto, String email,
                                  RolKaiju rol, boolean activo,
                                  boolean permisoEntradas, boolean permisoSalidas,
                                  boolean permisoPrecios, boolean permisoReportes,
                                  LocalDateTime fechaRegistro) {
        public static UsuarioResponse desde(UsuarioKaiju u) {
            return new UsuarioResponse(u.getId(), u.getRut(), u.getNombreCompleto(), u.getEmail(),
                    u.getRol(), u.isActivo(),
                    u.isPermisoEntradas(), u.isPermisoSalidas(), u.isPermisoPrecios(), u.isPermisoReportes(),
                    u.getFechaRegistro());
        }
    }

    // Respuesta del login: el token y los datos del usuario
    public record LoginResponse(String accessToken, String tokenType, long expiresIn, UsuarioResponse usuario) { }
}