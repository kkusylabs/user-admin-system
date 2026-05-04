package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.security.CurrentActorId;
import io.github.kkusylabs.useradmin.backend.services.department.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing departments.
 *
 * <p>Provides CRUD endpoints for departments. Write operations are restricted
 * to administrators, while read operations are available to authenticated users.</p>
 *
 * <p>The controller delegates all business logic to {@code DepartmentService}
 * and relies on Spring Security and service-layer validation for authorization.</p>
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li><b>POST /api/departments</b> – create department (admin only)</li>
 *   <li><b>GET /api/departments</b> – list departments</li>
 *   <li><b>GET /api/departments/{id}</b> – get department by id</li>
 *   <li><b>PUT /api/departments/{id}</b> – update department (admin only)</li>
 *   <li><b>DELETE /api/departments/{id}</b> – delete department (admin only)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * Creates a new department.
     *
     * @param request request payload containing department data
     * @param actorId id of the current user
     * @return the created department with permissions
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            summary = "Create department",
            description = "Creates a new department. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<DepartmentListItemResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        DepartmentListItemResponse createdDepartment = departmentService.createDepartment(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    /**
     * Returns all departments.
     *
     * @param actorId id of the current user
     * @return list of departments with permissions and create capability flag
     */
    @GetMapping
    @Operation(
            summary = "Get departments",
            description = "Retrieves all departments, including permissions and create capability for the current user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departments retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<DepartmentListResponse> getDepartments(
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(departmentService.getDepartments(actorId));
    }

    /**
     * Returns a single department by id.
     *
     * @param departmentId      department id
     * @param actorId id of the current user
     * @return department details with permissions
     */
    @GetMapping("/{departmentId}")
    @Operation(
            summary = "Get department",
            description = "Retrieves a department by ID, including permissions for the current user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    public ResponseEntity<DepartmentListItemResponse> getDepartment(
            @Parameter(description = "Department ID", required = true)
            @PathVariable Long departmentId,
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(departmentService.getDepartment(departmentId, actorId));
    }

    /**
     * Updates an existing department.
     *
     * @param request request payload with updated data
     * @param departmentId      department id
     * @param actorId id of the current user
     * @return updated department with permissions
     */
    @PutMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            summary = "Update department",
            description = "Updates an existing department. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    public ResponseEntity<DepartmentListItemResponse> updateDepartment(
            @Valid @RequestBody UpdateDepartmentRequest request,
            @Parameter(description = "Department ID", required = true)
            @PathVariable Long departmentId,
            @Parameter(hidden = true) @CurrentActorId Long actorId) {

        DepartmentListItemResponse updatedDepartment =
                departmentService.updateDepartment(departmentId, request, actorId);

        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Deletes a department.
     *
     * @param departmentId      department id
     * @param actorId id of the current user
     * @return empty response on success
     */
    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            summary = "Delete department",
            description = "Deletes a department by ID. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Department deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long departmentId,
            @Parameter(hidden = true) @CurrentActorId Long actorId
    ) {
        departmentService.deleteDepartment(departmentId, actorId);
        return ResponseEntity.noContent().build();
    }
}
