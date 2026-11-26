package cl.duoc.sonidovivo.kaiju;

import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginResponse;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.RegistroRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.UsuarioResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KaijuAuthService {

    private static final String PREFIJO = "Bearer ";

    private final UsuarioKaijuRepository repository;
    private final PasswordEncoder passwordEncoder;      // el mismo BCrypt de Sonido Vivo
    private final KaijuJwtService jwtService;

    public KaijuAuthService(UsuarioKaijuRepository repository, PasswordEncoder passwordEncoder,
                            KaijuJwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ----- REGISTRO: todo usuario nuevo entra como BODEGA; solo el admin puede cambiarlo -----
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String rut = normalizarRut(request.rut());
        if (!rutValido(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido");
        }
        String email = request.email().trim().toLowerCase();

        if (repository.existsByRut(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un operador con ese RUT");
        }
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese email ya está registrado");
        }

        UsuarioKaiju usuario = new UsuarioKaiju(
                rut,
                request.nombreCompleto().trim(),
                email,
                passwordEncoder.encode(request.password()),   // se guarda el hash, nunca la contraseña
                RolKaiju.BODEGA                               // rol fijo al registrarse
        );
        return UsuarioResponse.desde(repository.save(usuario));
    }

    // ----- LOGIN -----
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        UsuarioKaiju usuario = repository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                // mismo mensaje si falla el email o la contraseña: no revela quién está registrado
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos"));

        // una cuenta desactivada por el admin no puede entrar
        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu cuenta está desactivada. Habla con un administrador");
        }

        return new LoginResponse(
                jwtService.generarToken(usuario),
                "Bearer",
                jwtService.getVigenciaSegundos(),
                UsuarioResponse.desde(usuario)
        );
    }

    // ----- NUEVO: ¿QUIÉN SOY? La app lo consulta para saber si la sesión sigue válida -----
    // Lee la base en cada llamada: refleja al instante si el admin desactivó la cuenta o cambió el rol.
    public UsuarioResponse yo(String authorization) {
        if (authorization == null || !authorization.startsWith(PREFIJO)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }
        Long id;
        try {
            id = jwtService.validar(authorization.substring(PREFIJO.length()).trim());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tu sesión venció. Inicia sesión otra vez");
        }
        UsuarioKaiju usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tu cuenta ya no existe"));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu cuenta fue desactivada por un administrador");
        }
        return UsuarioResponse.desde(usuario);
    }

    // ----- RUT (mismas reglas que la app: módulo 11) -----
    static String normalizarRut(String rut) {
        String limpio = rut.replace(".", "").replace("-", "").trim().toUpperCase();
        if (limpio.length() < 2) return limpio;
        return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
    }

    static boolean rutValido(String rutNormalizado) {
        String[] partes = rutNormalizado.split("-");
        if (partes.length != 2 || !partes[0].matches("\\d{7,8}") || partes[1].length() != 1) return false;

        int suma = 0, multiplicador = 2;
        for (int i = partes[0].length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(partes[0].charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resultado = 11 - (suma % 11);
        char esperado = resultado == 11 ? '0' : resultado == 10 ? 'K' : (char) ('0' + resultado);
        return partes[1].charAt(0) == esperado;
    }
}