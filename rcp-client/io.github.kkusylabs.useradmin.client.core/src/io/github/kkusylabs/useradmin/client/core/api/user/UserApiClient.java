package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;

public class UserApiClient {

	private final RestClient restClient;

	public UserApiClient(RestClient restClient) {
		this.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
	}

	public UserListResponse getUsers(int page, int size) {
		return getUsers(page, size, null);
	}

	public UserListResponse getUsers(int page, int size, UserListFilter filter) {
		Map<String, Object> queryParams = new LinkedHashMap<>();
		queryParams.put("page", page);
		queryParams.put("size", size);

		if (filter != null) {
			if (filter.search() != null && !filter.search().isBlank()) {
				queryParams.put("search", filter.search());
			}
			if (filter.active() != null) {
				queryParams.put("active", filter.active());
			}
			if (filter.departmentId() != null) {
				queryParams.put("departmentId", filter.departmentId());
			}
			if (filter.role() != null) {
				queryParams.put("role", filter.role().name());
			}
			if (filter.sort() != null) {
				queryParams.put("sort", filter.sort().toQueryValue());
			}
		}

		return restClient.get("/users", queryParams, UserListResponse.class);
	}

	public UserDetailResponse getUserById(long userId) {
		return restClient.get("/users/" + userId, UserDetailResponse.class);
	}

	public CreateUserCapabilities getCreateCapabilities() {
		return restClient.get("/users/create-capabilities", CreateUserCapabilities.class);
	}

	public UserDetailResponse createUser(CreateUserRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return restClient.post("/users", request, UserDetailResponse.class);
	}

	public UserDetailResponse updateUser(long userId, Map<String, Object> patch) {
		Objects.requireNonNull(patch, "patch must not be null");
		return restClient.put("/users/" + userId, patch, UserDetailResponse.class);
	}

	public void deleteUser(long userId) {
		restClient.delete("/users/" + userId);
	}
	
	public EditUserResponse getUserEditData(long userId) {
		return restClient.get("/users/" + userId + "/edit", EditUserResponse.class);
	}
}