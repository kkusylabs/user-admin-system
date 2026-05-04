package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.dtos.user.*;
import io.github.kkusylabs.useradmin.backend.security.CurrentActorId;
import io.github.kkusylabs.useradmin.backend.services.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing users.
 *
 * <p>Provides endpoints for creating, retrieving, and deleting users.</p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates a new {@code UserController}.
     *
     * @param userService                the service used to manage users
     */
    public UserController(UserService userService) {
        this.userService = userService;
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
            @CurrentActorId Long actorId
    ) {
        UserListItemResponse createdUser = userService.createUser(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Returns a paginated list of users.
     *
     * @param pageable pagination and sorting information
     * @return a paged response of users
     */
    @GetMapping
    public ResponseEntity<UserListResponse> getUsers(
            Pageable pageable,
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getUsers(pageable, actorId));
    }

    /**
     * Returns a user by its identifier.
     *
     * @param id the user identifier
     * @return the user
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserListItemResponse> getUser(
            @PathVariable Long id,
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getUser(id, actorId));
    }

    /**
     * Applies a partial update to a user.
     *
     * @param id user identifier
     * @param request fields to update
     * @param actorId identifier of the authenticated actor
     * @return the updated user with actor-relative action flags
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserListItemResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            @CurrentActorId Long actorId) {
        UserListItemResponse updatedUser = userService.updateUser(id, request, actorId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user by its identifier.
     *
     * @param id the user identifier
     * @return an empty response with HTTP 204
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @CurrentActorId Long actorId
    ) {
        userService.deleteUser(id, actorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the data needed to render an edit-user form.
     *
     * @param id user identifier
     * @param actorId identifier of the authenticated actor
     * @return current user data and update capabilities
     */
    @GetMapping("/{id}/edit")
    public ResponseEntity<EditUserResponse> getUserEditData(
            @PathVariable Long id,
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getUserEditData(id, actorId));
    }

    /**
     * Returns the data needed to render a create-user form.
     *
     * @param actorId identifier of the authenticated actor
     * @return create permission, assignable roles, and assignable departments
     */
    @GetMapping("/create-capabilities")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CreateUserCapabilities> getCreateUserCapabilities(
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getCreateUserCapabilities(actorId));
    }
}