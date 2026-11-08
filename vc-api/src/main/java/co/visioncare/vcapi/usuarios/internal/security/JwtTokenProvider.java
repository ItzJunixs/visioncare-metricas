package co.visioncare.vcapi.usuarios.internal.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.visioncare.vcapi.usuarios.internal.entity.Role;
import co.visioncare.vcapi.usuarios.internal.entity.TokenProvider;
import co.visioncare.vcapi.usuarios.internal.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generateFor(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.username())
                .claim("role", user.role().name())
                .claim("name", user.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<Principal> verify(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new Principal(claims.getSubject(), Role.valueOf(claims.get("role", String.class))));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public long expirationMinutes() {
        return expirationMinutes;
    }
}
