package io.github.kkusylabs.useradmin.client.core.api.department;

import java.util.List;

public record DepartmentListResponse(
    List<DepartmentListItemResponse> departments,
    boolean canCreate
) {
}