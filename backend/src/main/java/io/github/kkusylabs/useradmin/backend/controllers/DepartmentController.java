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

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DepartmentListItemResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @CurrentActorId Long actorId
    ) {
        DepartmentListItemResponse createdDepartment = departmentService.createDepartment(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    @GetMapping
    public ResponseEntity<DepartmentListResponse> getDepartments(
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(departmentService.getDepartments(actorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentListItemResponse> getDepartment(
            @PathVariable Long id,
            @CurrentActorId Long actorId
    ) {
        return ResponseEntity.ok(departmentService.getDepartment(id, actorId));
    }

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
