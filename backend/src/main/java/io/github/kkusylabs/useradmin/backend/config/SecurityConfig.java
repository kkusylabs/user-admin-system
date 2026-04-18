package io.github.kkusylabs.useradmin.backend.config;

import io.github.kkusylabs.useradmin.backend.security.ActiveUserFilter;
import io.github.kkusylabs.useradmin.backend.security.CustomUserDetailsService;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Configures Spring Security for the backend application.
 * <p>
 * This configuration uses:
 * <ul>
 *     <li>username/password authentication for login</li>
 *     <li>JWT bearer tokens for protected API requests</li>
 *     <li>stateless session management</li>
 *     <li>method-level security for business authorization rules</li>
 * </ul>
 *
 * @author kkusy
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final String jwtSecret;
    private final ActiveUserFilter activeUserFilter;

    /**
     * Creates the security configuration.
     *
     * @param customUserDetailsService service used to load users from the database
     * @param jwtSecret shared secret used to validate HS256 JWTs
     */
    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            @Value("${app.jwt.secret}") String jwtSecret,
            ActiveUserFilter activeUserFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtSecret = jwtSecret;
        this.activeUserFilter = activeUserFilter;
    }

    /**
     * Configures the application's HTTP security.
     *
     * @param http the HTTP security builder
     * @return the configured security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.decoder(jwtDecoder())))
                .addFilterAfter(activeUserFilter,
                        org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates the authentication provider used for username/password login.
     *
     * @return DAO authentication provider backed by the application's users table
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the authentication manager used during login.
     *
     * @param configuration authentication configuration supplied by Spring
     * @return the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    /**
     * Creates the JWT decoder used by Spring Security Resource Server.
     *
     * @return a decoder configured for HS256 using the shared application secret
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Creates the password encoder used for hashing and verifying passwords.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}