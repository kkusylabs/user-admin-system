package io.github.kkusylabs.useradmin.backend.dtos.department;

import java.util.List;

public record DepartmentListResponse (
        List<DepartmentListItemResponse> departments,
        boolean canCreate
) {
}
