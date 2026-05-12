package io.github.kkusylabs.useradmin.client.core.api.department;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kkusylabs.useradmin.client.core.api.ForbiddenException;
import io.github.kkusylabs.useradmin.client.core.test.support.AbstractRealBackendApiTest;

class DepartmentApiClientRealBackendTest extends AbstractRealBackendApiTest {

	private DepartmentApiClient departmentApiClient;

	@BeforeEach
	void setUpDepartmentClient() {
		departmentApiClient = new DepartmentApiClient(restClient);
	}

	@Test
	void getDepartments_returnsSeededDepartments_forAdmin() {
		loginAsAdmin();

		DepartmentListResponse response = departmentApiClient.getDepartments();

		assertNotNull(response);
		assertNotNull(response.departments());
		assertFalse(response.departments().isEmpty());

		assertTrueContainsDepartment(response, "Engineering");
		assertTrueContainsDepartment(response, "Finance");
		assertTrueContainsDepartment(response, "HR");
	}

	@Test
	void createDepartment_forNonAdmin_isForbidden() {
		loginAsUser();

		String uniqueName = "Dept Forbidden " + System.currentTimeMillis();

		org.junit.jupiter.api.Assertions.assertThrows(ForbiddenException.class, () -> departmentApiClient
				.createDepartment(new CreateDepartmentRequest(uniqueName, "Should not be created")));
	}

	private void assertTrueContainsDepartment(DepartmentListResponse response, String name) {
		boolean found = response.departments().stream().map(item -> item.department().name()).anyMatch(name::equals);

		if (!found) {
			throw new AssertionError("Expected department not found: " + name);
		}
	}

	private Long findDepartmentIdByName(String name) {
		DepartmentListResponse response = departmentApiClient.getDepartments();

		return response.departments().stream().map(DepartmentListItemResponse::department)
				.filter(department -> Objects.equals(department.name(), name)).map(DepartmentDetailsResponse::id)
				.findFirst().orElseThrow(() -> new AssertionError("Could not find department: " + name));
	}
}