package io.github.kkusylabs.useradmin.client.ui.model.user;

import io.github.kkusylabs.useradmin.client.core.api.user.Role;


//	EditUserResponse response = userApiClient.getUserEditData(userId);
//	
//	UserDetailResponse original = response.user();
//	EditUserModel model = EditUserModelMapper.fromResponse(response);
//	
//	// user edits the model in the dialog
//	
//	Map<String, Object> patch = UserPatchMapper.toPatch(original, model);
//	
//	if (!patch.isEmpty()) {
//	    UserDetailResponse updated = userApiClient.updateUser(model.getId(), patch);
//	}

public class EditUserModel {

	private Long id;
	private String username;
	private String fullName;
	private String email;
	private String phone;
	private String jobTitle;
	private boolean active;
	private Long departmentId;
	private Role role;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Usually read-only in edit mode, but kept here for display/binding.
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
