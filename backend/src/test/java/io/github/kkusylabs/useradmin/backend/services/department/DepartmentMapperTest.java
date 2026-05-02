package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentDetailsResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.models.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentMapperTest {

    private DepartmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DepartmentMapper();
    }

    // ===== fromCreateRequest =====

    @Test
    void createsDepartmentFromCreateRequest() {
        CreateDepartmentRequest request =
                new CreateDepartmentRequest("Sales", "Handles sales");

        Department department = mapper.fromCreateRequest(request);

        assertNotNull(department);
        assertEquals("Sales", department.getName());
        assertEquals("Handles sales", department.getDescription());
        // active is not set here → depends on default, don’t assert it
    }

    // ===== toDetailsResponse =====

    @Test
    void mapsDepartmentToDetailsResponse() {
        Department department = department(1L, "Sales", "Handles sales", true);

        DepartmentDetailsResponse response = mapper.toDetailsResponse(department);

        assertEquals(1L, response.id());
        assertEquals("Sales", response.name());
        assertEquals("Handles sales", response.description());
        assertTrue(response.active());
    }

    // ===== toListItemResponse =====

    @Test
    void mapsDepartmentToListItemResponse() {
        Department department = department(1L, "Sales", "Handles sales", true);

        DepartmentListItemResponse response =
                mapper.toListItemResponse(department, true, false);

        assertNotNull(response);
        assertNotNull(response.department());

        assertEquals(1L, response.department().id());
        assertEquals("Sales", response.department().name());
        assertTrue(response.department().active());

        assertTrue(response.canUpdate());
        assertFalse(response.canDelete());
    }

    // ===== updateDepartment =====

    @Test
    void updatesDepartmentFromRequest() {
        Department department = department(1L, "Old Name", "Old desc", true);

        UpdateDepartmentRequest request =
                new UpdateDepartmentRequest("New Name", "New desc", false);

        mapper.updateDepartment(department, request);

        assertEquals("New Name", department.getName());
        assertEquals("New desc", department.getDescription());
        assertFalse(department.isActive());
    }

    // ===== Helpers =====

    private static Department department(Long id, String name, String description, boolean active) {
        Department department = new Department();
        setId(department, id);
        department.setName(name);
        department.setDescription(description);
        department.setActive(active);
        return department;
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to set id for test entity", e);
        }
    }
}