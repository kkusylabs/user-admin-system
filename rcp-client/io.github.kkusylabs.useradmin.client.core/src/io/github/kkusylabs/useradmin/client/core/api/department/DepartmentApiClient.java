package io.github.kkusylabs.useradmin.client.core.api.department;

import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;

public class DepartmentApiClient {

	private final RestClient restClient;

	public DepartmentApiClient(RestClient restClient) {
		this.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
	}

	public DepartmentListResponse getDepartments() {
		return restClient.get("/departments", DepartmentListResponse.class);
	}

	public DepartmentListItemResponse getDepartmentById(long departmentId) {
		return restClient.get("/departments/" + departmentId, DepartmentListItemResponse.class);
	}

	public DepartmentListItemResponse createDepartment(CreateDepartmentRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return restClient.post("/departments", request, DepartmentListItemResponse.class);
	}

	public DepartmentListItemResponse updateDepartment(long departmentId, UpdateDepartmentRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return restClient.put("/departments/" + departmentId, request, DepartmentListItemResponse.class);
	}

	public void deleteDepartment(long departmentId) {
		restClient.delete("/departments/" + departmentId);
	}
}