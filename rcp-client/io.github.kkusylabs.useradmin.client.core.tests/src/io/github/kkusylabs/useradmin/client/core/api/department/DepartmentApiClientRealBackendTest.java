package io.github.kkusylabs.useradmin.client.core.api.department;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kkusylabs.useradmin.client.core.api.ForbiddenException;
import io.github.kkusylabs.useradmin.client.core.api.NotFoundException;
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
	void getDepartments_returnsCanCreate_forAdmin() {
		loginAs("admin1", "admin12345");

		DepartmentListResponse response = departmentApiClient.getDepartments();

		assertNotNull(response);
		assertTrue(response.canCreate());
	}
	
	@Test
	void getDepartments_returnsCanCreateFalse_forNonAdmin() {
		loginAs("user1", "user12345");

		DepartmentListResponse response = departmentApiClient.getDepartments();

		assertNotNull(response);
		assertFalse(response.canCreate());
	}
	
	@Test
	void createDepartment_forNonAdmin_isForbidden() {
		loginAsUser();

		String uniqueName = "Dept Forbidden " + System.currentTimeMillis();

		org.junit.jupiter.api.Assertions.assertThrows(ForbiddenException.class, () -> departmentApiClient
				.createDepartment(new CreateDepartmentRequest(uniqueName, "Should not be created")));
	}
	
	@Test
	void getDepartmentById_returnsFinanceDepartment() {
		loginAs("admin1", "admin12345");

		Long financeId = findDepartmentIdByName("Finance");

		DepartmentListItemResponse response = departmentApiClient.getDepartmentById(financeId);

		assertNotNull(response);
		assertNotNull(response.department());
		assertEquals(financeId, response.department().id());
		assertEquals("Finance", response.department().name());
	}
	
	@Test
	void getDepartmentById_forUnknownDepartment_throwsNotFound() {
		loginAs("admin1", "admin12345");

		assertThrows(NotFoundException.class, () -> {
			departmentApiClient.getDepartmentById(999999L);
		});
	}
	
	@Test
	void createDepartment_succeeds_forAdmin_andCanBeDeleted() {
		loginAs("admin1", "admin12345");

		String uniqueName = "Dept IT " + System.currentTimeMillis();

		DepartmentListItemResponse created = departmentApiClient
				.createDepartment(new CreateDepartmentRequest(uniqueName, "Integration test department"));

		assertNotNull(created);
		assertNotNull(created.department());
		assertNotNull(created.department().id());
		assertEquals(uniqueName, created.department().name());

		Long createdId = created.department().id();

		DepartmentListItemResponse reloaded = departmentApiClient.getDepartmentById(createdId);
		assertEquals(uniqueName, reloaded.department().name());

		departmentApiClient.deleteDepartment(createdId);

		assertThrows(NotFoundException.class, () -> departmentApiClient.getDepartmentById(createdId));
	}
	
	@Test
	void updateDepartment_succeeds_forAdmin_andRestoresOriginalValue() {
		loginAs("admin1", "admin12345");

		Long financeId = findDepartmentIdByName("Finance");

		DepartmentListItemResponse original = departmentApiClient.getDepartmentById(financeId);
		DepartmentDetailsResponse originalDept = original.department();

		String originalDescription = originalDept.description();
		boolean originalActive = originalDept.active();

		String temporaryDescription = "Updated by integration test";
		boolean temporaryActive = !originalActive;

		try {
			DepartmentListItemResponse updated = departmentApiClient.updateDepartment(financeId,
					new UpdateDepartmentRequest(originalDept.name(), temporaryDescription, temporaryActive));

			assertNotNull(updated);

			DepartmentListItemResponse reloaded = departmentApiClient.getDepartmentById(financeId);
			assertEquals(temporaryDescription, reloaded.department().description());
			assertEquals(temporaryActive, reloaded.department().active());
		} finally {
			departmentApiClient.updateDepartment(financeId,
					new UpdateDepartmentRequest(originalDept.name(), originalDescription, originalActive));
		}

		DepartmentListItemResponse restored = departmentApiClient.getDepartmentById(financeId);
		assertEquals(originalDescription, restored.department().description());
		assertEquals(originalActive, restored.department().active());
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