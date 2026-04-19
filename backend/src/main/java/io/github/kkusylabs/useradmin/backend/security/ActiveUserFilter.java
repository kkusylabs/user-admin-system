package io.github.kkusylabs.useradmin.backend.security;

import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that ensures authenticated users are still active.
 * <p>
 * For requests authenticated with a JWT, this filter retrieves the user
 * from the database and rejects the request if the account is inactive.
 * </p>
 *
 * @author kkusy
 */
@Component
public class ActiveUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    /**
     * Creates a new filter with the required user repository.
     *
     * @param userRepository the repository used to load users
     */
    public ActiveUserFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verifies that the authenticated user is active before allowing the request to proceed.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            Long userId = jwt.getClaim("userId");

            if (userId != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new DisabledException("User account is not available"));

                if (!user.isActive()) {
                    throw new DisabledException("User account is inactive");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }
}