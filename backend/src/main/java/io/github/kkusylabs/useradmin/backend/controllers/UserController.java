package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.dtos.CreateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.UserResponse;
import io.github.kkusylabs.useradmin.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users.
 * <p>
 * Provides endpoints for creating users and retrieving user data.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates a new controller instance.
     *
     * @param userService the service used to manage users
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user.
     *
     * @param request the user creation request
     * @param jwt     the authenticated user's JWT
     * @return the created user
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        UserResponse createdUser = userService.createUser(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Returns a paginated list of users.
     *
     * @param pageable pagination and sorting information
     * @return a page of user response DTOs
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    /**
     * Returns a user by its identifier.
     *
     * @param id the identifier of the user
     * @return the user response DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Updates an existing user.
     *
     * @param id      the identifier of the user to update
     * @param request the requested changes
     * @param jwt     the authenticated user's JWT
     * @return the updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateById(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        UserResponse updatedUser = userService.updateById(id, request, actorId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user by its identifier.
     *
     * @param id  the identifier of the user to delete
     * @param jwt the authenticated user's JWT
     * @return an empty response with HTTP 204
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        userService.deleteById(id, actorId);
        return ResponseEntity.noContent().build();
    }
}