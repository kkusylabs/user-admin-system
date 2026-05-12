package io.github.kkusylabs.useradmin.client.core.api.user;

import io.github.kkusylabs.useradmin.client.core.test.support.AbstractRealBackendApiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}