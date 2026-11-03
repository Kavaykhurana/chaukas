package in.chaukas.config;

import in.chaukas.domain.AppUser;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private final SecretKey key;
    private final Duration ttl;

    public JwtService(AppProps props) {
        byte[] secret = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("chaukas.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.ttl = Duration.ofHours(props.jwt().ttlHours());
    }

    public String issue(AppUser user) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .claim("name", user.getName())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + ttl.toMillis()))
            .signWith(key)
            .compact();
    }

    /** Returns the user id inside a valid, unexpired token. */
    public Optional<Long> parse(String token) {
        try {
            String sub = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(Long.parseLong(sub));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
