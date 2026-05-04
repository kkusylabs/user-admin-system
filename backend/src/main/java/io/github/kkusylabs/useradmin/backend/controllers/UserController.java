package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.dtos.user.*;
import io.github.kkusylabs.useradmin.backend.security.CurrentActorId;
import io.github.kkusylabs.useradmin.backend.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Create user",
            description = "Creates a new user account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<UserListItemResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @Parameter(hidden = true) @CurrentActorId Long actorId
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
    @Operation(
            summary = "Get users",
            description = "Retrieves a list of users. Supports optional filtering and pagination."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<UserListResponse> getUsers(
            Pageable pageable,
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getUsers(pageable, actorId));
    }

    /**
     * Returns a user by its identifier.
     *
     * @param userId the user identifier
     * @return the user
     */
    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user",
            description = "Retrieves a user by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = @Content)
    })
    public ResponseEntity<UserListItemResponse> getUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getUser(userId, actorId));
    }

    /**
     * Applies a partial update to a user.
     *
     * @param userId user identifier
     * @param request fields to update
     * @param actorId identifier of the authenticated actor
     * @return the updated user with actor-relative action flags
     */
    @PatchMapping("/{userId}")
    @Operation(
            summary = "Update user",
            description = "Partially updates a user. Omitted fields are unchanged. Fields set to null are cleared."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Partial user update. Omitted fields are unchanged. Fields set to null are cleared when allowed.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "fullName": "Jane Doe",
                          "email": "jane@example.com",
                          "phone": "+1 555-123-4567",
                          "jobTitle": "Senior Manager",
                          "active": true,
                          "departmentId": 1,
                          "role": "MANAGER"
                        }
                        """)
            )
    )
    public ResponseEntity<UserListItemResponse> updateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @CurrentActorId Long actorId) {
        UserListItemResponse updatedUser = userService.updateUser(userId, request, actorId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user by its identifier.
     *
     * @param userId the user identifier
     * @return an empty response with HTTP 204
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Delete user",
            description = "Deletes a user by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        userService.deleteUser(userId, actorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the data needed to render an edit-user form.
     *
     * @param userId user identifier
     * @param actorId identifier of the authenticated actor
     * @return current user data and update capabilities
     */
    @GetMapping("/{userId}/edit")
    @Operation(
            summary = "Get user edit data",
            description = "Retrieves the data required to render the edit-user form, including current user values and any update constraints."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Edit data retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<EditUserResponse> getUserEditData(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getUserEditData(userId, actorId));
    }

    /**
     * Returns the data needed to render a create-user form.
     *
     * @param actorId identifier of the authenticated actor
     * @return create permission, assignable roles, and assignable departments
     */
    @GetMapping("/create-capabilities")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Get create-user capabilities",
            description = "Retrieves the data required to render the create-user form, including whether the current actor can create users and which roles and departments are assignable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Create-user capabilities retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<CreateUserCapabilities> getCreateUserCapabilities(
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(userService.getCreateUserCapabilities(actorId));
    }
}