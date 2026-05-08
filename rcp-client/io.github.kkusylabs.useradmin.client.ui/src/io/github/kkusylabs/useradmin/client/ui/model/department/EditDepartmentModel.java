package io.github.kkusylabs.useradmin.client.ui.model.department;

//	DepartmentListItemResponse response = departmentApiClient.getDepartmentById(departmentId);
//	
//	EditDepartmentModel model = EditDepartmentModelMapper.fromResponse(response);
//	
//	// user edits fields in dialog
//	
//	UpdateDepartmentRequest request = DepartmentUpdateRequestMapper.toRequest(model);
//	DepartmentListItemResponse updated =
//	    departmentApiClient.updateDepartment(model.getId(), request);

public class EditDepartmentModel {

	private Long id;
	private String name;
	private String description;
	private boolean active;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}