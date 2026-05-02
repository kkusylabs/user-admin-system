package io.github.kkusylabs.useradmin.backend.dtos.department;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateDepartmentRequestTest {

    @Test
    void trimsNameAndDescription() {
        CreateDepartmentRequest request =
                new CreateDepartmentRequest("  Sales  ", "  Handles sales  ");

        assertEquals("Sales", request.name());
        assertEquals("Handles sales", request.description());
    }

    @Test
    void convertsBlankDescriptionToNull() {
        CreateDepartmentRequest request =
                new CreateDepartmentRequest("Sales", "   ");

        assertNull(request.description());
    }

    @Test
    void nameIsTrimmedButNotConvertedToNull() {
        CreateDepartmentRequest request =
                new CreateDepartmentRequest("  Sales  ", null);

        assertEquals("Sales", request.name());
        assertNull(request.description());
    }
}
