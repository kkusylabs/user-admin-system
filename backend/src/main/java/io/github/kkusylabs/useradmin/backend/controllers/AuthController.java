package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import io.github.kkusylabs.useradmin.backend.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles authentication-related API endpoints.
 *
 * <p>Provides endpoints for user registration and login. On successful
 * authentication, a JWT is returned to the client. The token is then used
 * to access protected API endpoints.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Creates a new controller.
     *
     * @param authenticationManager used to authenticate login requests
     * @param userRepository        used to retrieve users
     * @param jwtService            generates JWT tokens
     */
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates a user.
     *
     * <p>Validates credentials using Spring Security and returns a JWT
     * if authentication succeeds.</p>
     *
     * @param request login credentials
     * @return JWT token for the authenticated user
     */
    @PostMapping("/login")
    @Operation(
            summary = "Log in",
            description = "Authenticates a user and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content)
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalize(request.username()),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(normalize(request.username()))
                .orElseThrow();

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
    }

    /**
     * Returns information about the currently authenticated user.
     *
     * <p>Extracts user details directly from the JWT, avoiding a database lookup.
     * This endpoint is primarily useful for verifying authentication and retrieving
     * basic identity information.</p>
     *
     * @param jwt the authenticated JWT containing user claims
     * @return a map containing the user's ID and username
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Returns basic identity information for the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new MeResponse(
                ((Number) jwt.getClaim("userId")).longValue(),
                jwt.getSubject()
        );
    }

    /**
     * Normalizes a string by trimming whitespace and converting to lowercase.
     *
     * @param value the input value
     * @return normalized value, or {@code null} if input is null
     */
    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }


    /**
     * Request payload for user login.
     *
     * @param username the username
     * @param password the raw password
     */
    public record LoginRequest(
            @NotBlank
            String username,

            @NotBlank
            String password
    ) {
    }

    /**
     * Response payload containing a JWT.
     *
     * @param accessToken the generated JWT
     * @param tokenType   typically "Bearer"
     */
    public record AuthResponse(
            String accessToken,
            String tokenType
    ) {
    }

    public record MeResponse(
            Long userId,
            String username
    ) {
    }
}
