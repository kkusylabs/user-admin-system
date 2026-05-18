package io.github.kkusylabs.useradmin.client.core.api.department;

import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;

/**
 * Client for department management API operations.
 *
 * <p>Provides methods for retrieving, creating, updating, and deleting
 * departments.
 */
public class DepartmentApiClient {

	private final RestClient restClient;

	/**
	 * Creates a new department API client.
	 *
	 * @param restClient underlying REST client used to execute HTTP requests
	 */
	public DepartmentApiClient(RestClient restClient) {
		this.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
	}

	/**
	 * Returns all departments.
	 *
	 * @return department list response
	 */
	public DepartmentListResponse getDepartments() {
		return restClient.get("/departments", DepartmentListResponse.class);
	}

	/**
	 * Returns a department by its identifier.
	 *
	 * @param departmentId unique department identifier
	 * @return matching department
	 */
	public DepartmentListItemResponse getDepartmentById(long departmentId) {
		return restClient.get("/departments/" + departmentId, DepartmentListItemResponse.class);
	}

	/**
	 * Creates a new department.
	 *
	 * @param request department creation request
	 * @return created department
	 * @throws NullPointerException if {@code request} is {@code null}
	 */
	public DepartmentListItemResponse createDepartment(CreateDepartmentRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return restClient.post("/departments", request, DepartmentListItemResponse.class);
	}

	/**
	 * Updates an existing department.
	 *
	 * @param departmentId unique department identifier
	 * @param request updated department values
	 * @return updated department
	 * @throws NullPointerException if {@code request} is {@code null}
	 */
	public DepartmentListItemResponse updateDepartment(long departmentId, UpdateDepartmentRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return restClient.put("/departments/" + departmentId, request, DepartmentListItemResponse.class);
	}

	/**
	 * Deletes a department.
	 *
	 * @param departmentId unique department identifier
	 */
	public void deleteDepartment(long departmentId) {
		restClient.delete("/departments/" + departmentId);
	}
}