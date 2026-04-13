package io.github.kkusylabs.useradmin.backend.security;

import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads application users from the database for Spring Security authentication.
 * <p>
 * This service is used during username/password login. It converts the
 * application's {@link io.github.kkusylabs.useradmin.backend.models.User}
 * entity into Spring Security's {@link UserDetails} representation.
 *
 * @author kkusy
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Creates the service with the repository used to load users.
     *
     * @param userRepository repository for user lookup operations
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for authentication.
     *
     * @param username the username to look up
     * @return Spring Security user details for the matching user
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username.trim();

        User user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + normalizedUsername));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}