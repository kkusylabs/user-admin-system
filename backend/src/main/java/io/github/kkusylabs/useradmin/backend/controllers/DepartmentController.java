package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.security.CurrentActorId;
import io.github.kkusylabs.useradmin.backend.services.department.DepartmentService;
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
 *
 * @author kkusy
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
    public ResponseEntity<DepartmentListItemResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @CurrentActorId Long actorId
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
    public ResponseEntity<DepartmentListResponse> getDepartments(
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(departmentService.getDepartments(actorId));
    }

    /**
     * Returns a single department by id.
     *
     * @param id      department id
     * @param actorId id of the current user
     * @return department details with permissions
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentListItemResponse> getDepartment(
            @PathVariable Long id,
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(departmentService.getDepartment(id, actorId));
    }

    /**
     * Updates an existing department.
     *
     * @param request request payload with updated data
     * @param id      department id
     * @param actorId id of the current user
     * @return updated department with permissions
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DepartmentListItemResponse> updateDepartment(
            @Valid @RequestBody UpdateDepartmentRequest request,
            @PathVariable Long id,
            @CurrentActorId Long actorId) {

        DepartmentListItemResponse updatedDepartment =
                departmentService.updateDepartment(id, request, actorId);

        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Deletes a department.
     *
     * @param id      department id
     * @param actorId id of the current user
     * @return empty response on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id,
            @CurrentActorId Long actorId
    ) {
        departmentService.deleteDepartment(id, actorId);
        return ResponseEntity.noContent().build();
    }
}
