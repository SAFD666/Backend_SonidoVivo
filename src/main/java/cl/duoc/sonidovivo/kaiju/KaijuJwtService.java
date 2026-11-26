package cl.duoc.sonidovivo.kaiju;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

// Tokens de la app Kaiju. Usa la misma clave secreta de Sonido Vivo, pero con un DESTINATARIO distinto
// ("kaiju-app"): JwtService de Sonido Vivo exige su propio destinatario, así que RECHAZA estos tokens
// y un usuario de Kaiju nunca puede usarse como usuario de Sonido Vivo.
@Service
public class KaijuJwtService {

    public static final String DESTINATARIO_KAIJU = "kaiju-app";

    private final SecretKey clave;
    private final long vigenciaSegundos;
    private final String emisor;

    public KaijuJwtService(@Value("${jwt.secret}") String secreto,
                           @Value("${jwt.expiration-minutes}") long vigenciaMinutos,
                           @Value("${jwt.issuer}") String emisor) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.vigenciaSegundos = vigenciaMinutos * 60;
        this.emisor = emisor;
    }

    public String generarToken(UsuarioKaiju usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .issuer(emisor)
                .audience().add(DESTINATARIO_KAIJU).and()
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(vigenciaSegundos)))
                .claim("app", "kaiju")
                .claim("rol", usuario.getRol().name())      // informativo: el rol real se lee de la base
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    // NUEVO: revisa firma, emisor, destinatario y vencimiento. Devuelve el id del usuario.
    // Si algo falla lanza JwtException (token inválido o vencido).
    public Long validar(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(emisor)
                .requireAudience(DESTINATARIO_KAIJU)       // un token de Sonido Vivo no sirve aquí
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    public long getVigenciaSegundos() {
        return vigenciaSegundos;
    }
}