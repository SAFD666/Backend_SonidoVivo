package cl.duoc.sonidovivo.kaiju;

import cl.duoc.sonidovivo.kaiju.KaijuDtos.ActualizarUsuarioRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.UsuarioResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Acciones que SOLO puede hacer un administrador de Kaiju
@Service
public class KaijuAdminService {

    private static final String PREFIJO = "Bearer ";

    private final UsuarioKaijuRepository repository;
    private final KaijuJwtService jwtService;

    public KaijuAdminService(UsuarioKaijuRepository repository, KaijuJwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    // ----- LISTAR todos los usuarios (sin contraseñas), ordenados por nombre -----
    public List<UsuarioResponse> listar(String authorization) {
        verificarAdmin(authorization);
        return repository.findAll(Sort.by("nombreCompleto")).stream()
                .map(UsuarioResponse::desde)
                .toList();
    }

    // ----- EDITAR nombre, email, rol, estado y permisos -----
    @Transactional
    public UsuarioResponse actualizar(String authorization, Long id, ActualizarUsuarioRequest request) {
        UsuarioKaiju admin = verificarAdmin(authorization);

        UsuarioKaiju usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String email = request.email().trim().toLowerCase();
        if (!email.equals(usuario.getEmail()) && repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese email ya está registrado");
        }

        // Protección: el admin no puede quitarse su propio rol ni desactivarse (Kaiju quedaría sin admin)
        boolean esElMismo = usuario.getId().equals(admin.getId());
        if (esElMismo && (request.rol() != RolKaiju.ADMIN || !request.activo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes quitarte el rol de administrador ni desactivar tu propia cuenta");
        }

        RolKaiju rolAnterior = usuario.getRol();

        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(email);
        usuario.setRol(request.rol());
        usuario.setActivo(request.activo());

        // Permisos: si vienen, se usan esos; si no vienen y cambió el rol, los del rol nuevo
        if (request.traePermisos()) {
            usuario.asignarPermisos(request.permisoEntradas(), request.permisoSalidas(),
                    request.permisoPrecios(), request.permisoReportes());
        } else if (rolAnterior != request.rol()) {
            usuario.aplicarPermisosDeRol();
        }

        return UsuarioResponse.desde(repository.save(usuario));
    }

    // Revisa el header "Authorization: Bearer <token>" y que quien llama sea un ADMIN activo.
    // El rol se lee de la BASE (no del token): si se lo quitan, pierde el acceso al instante.
    private UsuarioKaiju verificarAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith(PREFIJO)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }
        Long id;
        try {
            id = jwtService.validar(authorization.substring(PREFIJO.length()).trim());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o vencida. Inicia sesión otra vez");
        }
        UsuarioKaiju usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión"));

        if (!usuario.isActivo() || usuario.getRol() != RolKaiju.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede hacer esto");
        }
        return usuario;
    }
}