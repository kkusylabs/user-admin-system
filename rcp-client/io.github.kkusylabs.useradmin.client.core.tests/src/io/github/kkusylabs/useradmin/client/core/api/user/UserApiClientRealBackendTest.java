package io.github.kkusylabs.useradmin.client.core.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kkusylabs.useradmin.client.core.test.support.AbstractRealBackendApiTest;

class UserApiClientRealBackendTest extends AbstractRealBackendApiTest {

	private UserApiClient userApiClient;

	@BeforeEach
	void setUpUserClient() {
		userApiClient = new UserApiClient(restClient);
	}

	@Test
	void getUsers_returnsSeededUsers_forAdmin() {
		loginAsAdmin();

		UserListResponse response = userApiClient.getUsers(0, 50);

		assertNotNull(response);
		assertNotNull(response.users());
		assertNotNull(response.users().content());
		assertFalse(response.users().content().isEmpty());

		assertTrueContainsUsername(response, "admin1");
		assertTrueContainsUsername(response, "manager1");
		assertTrueContainsUsername(response, "user1");
	}
	
	@Test
	void getUsers_withActiveFilter_returnsOnlyActiveUsers() {
		loginAs("admin1", "admin12345");

		UserListFilter filter = new UserListFilter(null, true, null, null, null);

		UserListResponse response = userApiClient.getUsers(0, 50, filter);

		assertNotNull(response);
		assertNotNull(response.users());
		assertNotNull(response.users().content());
		assertFalse(response.users().content().isEmpty());

		assertTrue(response.users().content().stream().allMatch(item -> item.user().active()));
	}
	
	@Test
	void getUsers_withRoleFilter_returnsOnlyUsersWithThatRole() {
		loginAs("admin1", "admin12345");

		UserListFilter filter = new UserListFilter(null, null, null, Role.ADMIN, null);

		UserListResponse response = userApiClient.getUsers(0, 50, filter);

		assertNotNull(response);
		assertNotNull(response.users());
		assertNotNull(response.users().content());
		assertFalse(response.users().content().isEmpty());

		assertTrue(response.users().content().stream().allMatch(item -> item.user().role() == Role.ADMIN));
	}
	
	@Test
	void getUserById_returnsAdminUserDetails() {
		loginAs("admin1", "admin12345");

		Long adminUserId = currentUserId();

		UserDetailResponse user = userApiClient.getUserById(adminUserId).user();

		assertNotNull(user);
		assertEquals(adminUserId, user.id());
		assertEquals("admin1", user.username());
		assertNotNull(user.email());
		assertNotNull(user.role());
	}
	
	@Test
	void getUserEditData_returnsUserAndCapabilities() {
		loginAs("admin1", "admin12345");

		Long userId = findUserIdByUsername("user1");

		EditUserResponse response = userApiClient.getUserEditData(userId);

		assertNotNull(response);
		assertNotNull(response.user());
		assertNotNull(response.updateCapabilities());

		assertEquals("user1", response.user().username());
	}
	
	@Test
	void updateUser_changesDepartment_andRestoresOriginalValue() {
		loginAs("admin1", "admin12345");

		Long userId = findUserIdByUsername("user1");

		UserDetailResponse original = userApiClient.getUserById(userId).user();
		Long originalDepartmentId = original.department() == null ? null : original.department().id();

		UserListResponse users = userApiClient.getUsers(0, 50);
		Long adminId = users.users().content().stream().map(UserListItemResponse::user)
				.filter(u -> Objects.equals(u.username(), "admin1")).map(UserDetailResponse::id).findFirst()
				.orElseThrow(() -> new AssertionError("Could not find admin1"));

		UserDetailResponse adminUser = userApiClient.getUserById(adminId).user();
		Long targetDepartmentId = adminUser.department() == null ? null : adminUser.department().id();

		assumeTrue(targetDepartmentId != null, "admin1 must have a department");
		assumeTrue(!Objects.equals(originalDepartmentId, targetDepartmentId),
				"Seed data should place user1 in a different department from admin1");

		try {
			userApiClient.updateUser(userId, Map.of("departmentId", targetDepartmentId));

			UserDetailResponse reloaded = userApiClient.getUserById(userId).user();
			assertNotNull(reloaded.department());
			assertEquals(targetDepartmentId, reloaded.department().id());
		} finally {
			userApiClient.updateUser(userId, Map.of("departmentId", originalDepartmentId));
		}

		UserDetailResponse restored = userApiClient.getUserById(userId).user();
		if (originalDepartmentId == null) {
			assertNull(restored.department());
		} else {
			assertNotNull(restored.department());
			assertEquals(originalDepartmentId, restored.department().id());
		}
	}

	@Test
	void updateUser_changesJobTitle_andRestoresOriginalValue() {
		loginAsAdmin();

		Long userId = findUserIdByUsername("user1");

		UserDetailResponse original = userApiClient.getUserById(userId).user();
		String originalJobTitle = original.jobTitle();

		String temporaryJobTitle = "Temporary Integration Test Title";

		try {
			userApiClient.updateUser(userId, Map.of("jobTitle", temporaryJobTitle));

			UserDetailResponse reloaded = userApiClient.getUserById(userId).user();
			assertEquals(temporaryJobTitle, reloaded.jobTitle());
		} finally {
			userApiClient.updateUser(userId, Map.of("jobTitle", originalJobTitle));
		}
	}
	
	@Test
	void getCreateUserCapabilities_returnsOptions_forAdmin() {
		loginAs("admin1", "admin12345");

		CreateUserCapabilities capabilities = userApiClient.getCreateUserCapabilities();

		assertNotNull(capabilities);
		assertTrue(capabilities.canCreate());
		assertNull(capabilities.reason());

		assertNotNull(capabilities.roleOptions());
		assertFalse(capabilities.roleOptions().isEmpty());
		assertTrue(capabilities.roleOptions().contains(Role.USER));

		assertNotNull(capabilities.departmentOptions());
		assertFalse(capabilities.departmentOptions().isEmpty());
		assertTrue(capabilities.departmentOptions().stream()
				.anyMatch(department -> "Engineering".equals(department.name())));
	}

	private Long findUserIdByUsername(String username) {
		UserListResponse response = userApiClient.getUsers(0, 100, 
				new UserListFilter(username, null, null, null, null));

		return response.users().content().stream().map(UserListItemResponse::user)
				.filter(user -> username.equals(user.username())).map(UserDetailResponse::id).findFirst()
				.orElseThrow(() -> new AssertionError("Could not find user: " + username));
	}

	private void assertTrueContainsUsername(UserListResponse response, String username) {
		boolean found = response.users().content().stream().map(item -> item.user().username())
				.anyMatch(username::equals);

		if (!found) {
			throw new AssertionError("Expected user not found: " + username);
		}
	}
	
	private Long currentUserId() {
		return authApiClient.me().userId();
	}
}