package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;

/**
 * Client for user management API operations.
 *
 * <p>Provides methods for retrieving, creating, updating, and deleting users,
 * along with user-related capability and edit metadata endpoints.
 */
public class UserApiClient {

	private final RestClient restClient;

	/**
	 * Creates a new user API client.
	 *
	 * @param restClient underlying REST client used to execute HTTP requests
	 */
	public UserApiClient(RestClient restClient) {
		this.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
	}

	/**
	 * Returns a paginated list of users.
	 *
	 * @param page zero-based page index
	 * @param size maximum number of users per page
	 * @return paginated user response
	 */
	public UserListResponse getUsers(int page, int size) {
		return getUsers(page, size, null);
	}

	/**
	 * Returns a paginated list of users using optional filtering and sorting
	 * criteria.
	 *
	 * @param page zero-based page index
	 * @param size maximum number of users per page
	 * @param filter optional user filtering and sorting options
	 * @return paginated user response
	 */
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

	/**
	 * Returns a user by identifier.
	 *
	 * @param userId unique user identifier
	 * @return matching user response
	 */
	public UserListItemResponse getUserById(long userId) {
		return restClient.get("/users/" + userId, UserListItemResponse.class);
	}

	/**
	 * Returns capability and reference data required to create users.
	 *
	 * @return user creation capability data
	 */
	public CreateUserCapabilities getCreateUserCapabilities() {
		return restClient.get("/users/create-capabilities", CreateUserCapabilities.class);
	}

	/**
	 * Creates a new user.
	 *
	 * @param request user creation request
	 * @return created user response
	 * @throws NullPointerException if {@code request} is {@code null}
	 */
	public UserListItemResponse createUser(CreateUserRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return restClient.post("/users", request, UserListItemResponse.class);
	}


	/**
	 * Applies a partial update to an existing user.
	 *
	 * @param userId unique user identifier
	 * @param patch partial update payload
	 * @return updated user response
	 * @throws NullPointerException if {@code patch} is {@code null}
	 */
	public UserListItemResponse updateUser(long userId, Map<String, Object> patch) {
		Objects.requireNonNull(patch, "patch must not be null");
		return restClient.patch("/users/" + userId, patch, UserListItemResponse.class);
	}

	/**
	 * Deletes a user.
	 *
	 * @param userId unique user identifier
	 */
	public void deleteUser(long userId) {
		restClient.delete("/users/" + userId);
	}
	

	/**
	 * Returns edit metadata and reference data for a user.
	 *
	 * @param userId unique user identifier
	 * @return user edit response
	 */
	public EditUserResponse getUserEditData(long userId) {
		return restClient.get("/users/" + userId + "/edit", EditUserResponse.class);
	}
}