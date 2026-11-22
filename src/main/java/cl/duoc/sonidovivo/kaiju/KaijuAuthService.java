package cl.duoc.sonidovivo.kaiju;

import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.LoginResponse;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.RegistroRequest;
import cl.duoc.sonidovivo.kaiju.KaijuDtos.UsuarioResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KaijuAuthService {

    private final UsuarioKaijuRepository repository;
    private final PasswordEncoder passwordEncoder;      // el mismo BCrypt de Sonido Vivo
    private final KaijuJwtService jwtService;

    public KaijuAuthService(UsuarioKaijuRepository repository, PasswordEncoder passwordEncoder,
                            KaijuJwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ----- REGISTRO -----
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
                request.rol()
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

        return new LoginResponse(
                jwtService.generarToken(usuario),
                "Bearer",
                jwtService.getVigenciaSegundos(),
                UsuarioResponse.desde(usuario)
        );
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