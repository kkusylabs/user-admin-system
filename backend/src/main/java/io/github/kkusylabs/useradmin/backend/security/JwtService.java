package io.github.kkusylabs.useradmin.backend.security;

import io.github.kkusylabs.useradmin.backend.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Service responsible for creating JWT access tokens.
 * <p>
 * Tokens produced by this service are later validated by Spring Security's
 * Resource Server support through the configured {@code JwtDecoder}.
 *
 * @author kkusy
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    /**
     * Creates the JWT service using application configuration.
     *
     * @param secret           shared secret used to sign HS256 tokens
     * @param expirationMillis token lifetime in milliseconds
     */
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-millis}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /**
     * Generates a signed JWT for the authenticated user.
     * <p>
     * The username is stored as the JWT subject so it naturally maps to the
     * authenticated principal name in Spring Security.
     *
     * @param user the authenticated application user
     * @return a compact signed JWT string
     */
    public String generateToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("departmentId", user.getDepartment().getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}