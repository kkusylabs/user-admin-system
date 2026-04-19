package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.dtos.common.AuthenticatedActor;
import io.github.kkusylabs.useradmin.backend.dtos.user.*;
import io.github.kkusylabs.useradmin.backend.security.AuthenticatedActorResolver;
import io.github.kkusylabs.useradmin.backend.services.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users.
 *
 * <p>Provides endpoints for creating, retrieving, and deleting users.</p>
 *
 * @author kkusy
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticatedActorResolver authenticatedActorResolver;

    /**
     * Creates a new {@code UserController}.
     *
     * @param userService                the service used to manage users
     * @param authenticatedActorResolver resolves the authenticated actor from a JWT
     */
    public UserController(UserService userService,
                          AuthenticatedActorResolver authenticatedActorResolver) {
        this.userService = userService;
        this.authenticatedActorResolver = authenticatedActorResolver;
    }

    /**
     * Creates a new user.
     *
     * @param request the user creation request
     * @return the created user
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserListItemResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedActor actor = authenticatedActorResolver.fromJwt(jwt);
        UserListItemResponse createdUser = userService.createUser(request, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Returns a paginated list of users.
     *
     * @param pageable pagination and sorting information
     * @return a paged response of users
     */
    @GetMapping
    public ResponseEntity<UserListResponse> findAll(
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedActor actor = authenticatedActorResolver.fromJwt(jwt);
        return ResponseEntity.ok(userService.getUsers(pageable, actor));
    }

    /**
     * Returns a user by its identifier.
     *
     * @param id the user identifier
     * @return the user
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserListItemResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedActor actor = authenticatedActorResolver.fromJwt(jwt);
        return ResponseEntity.ok(userService.getUserById(id, actor));
    }

    /**
     * Deletes a user by its identifier.
     *
     * @param id the user identifier
     * @return an empty response with HTTP 204
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedActor actor = authenticatedActorResolver.fromJwt(jwt);
        userService.deleteUserById(id, actor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<EditUserResponse> getUserForEdit(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedActor actor = authenticatedActorResolver.fromJwt(jwt);
        return ResponseEntity.ok(userService.getUserForEdit(id, actor));
    }

    @GetMapping("/create-capabilities")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CreateUserCapabilities> getCreateCapabilities(
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedActor actor = authenticatedActorResolver.fromJwt(jwt);
        return ResponseEntity.ok(userService.getCreateCapabilities(actor));
    }
}