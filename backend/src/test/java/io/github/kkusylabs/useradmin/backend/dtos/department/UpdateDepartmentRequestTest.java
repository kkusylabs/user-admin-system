package io.github.kkusylabs.useradmin.backend.dtos.department;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateDepartmentRequestTest {

    @Test
    void trimsNameAndDescription() {
        UpdateDepartmentRequest request =
                new UpdateDepartmentRequest("  Sales  ", "  Handles sales  ", true);

        assertEquals("Sales", request.name());
        assertEquals("Handles sales", request.description());
        assertTrue(request.active());
    }

    @Test
    void convertsBlankDescriptionToNull() {
        UpdateDepartmentRequest request =
                new UpdateDepartmentRequest("Sales", "   ", true);

        assertNull(request.description());
    }

    @Test
    void preservesActiveFlag() {
        UpdateDepartmentRequest activeRequest =
                new UpdateDepartmentRequest("Sales", null, true);

        UpdateDepartmentRequest inactiveRequest =
                new UpdateDepartmentRequest("Sales", null, false);

        assertTrue(activeRequest.active());
        assertFalse(inactiveRequest.active());
    }

    @Test
    void handlesNullDescription() {
        UpdateDepartmentRequest request =
                new UpdateDepartmentRequest("Sales", null, true);

        assertNull(request.description());
    }
}