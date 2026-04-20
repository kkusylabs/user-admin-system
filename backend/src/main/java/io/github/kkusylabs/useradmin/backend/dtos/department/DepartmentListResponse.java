package io.github.kkusylabs.useradmin.backend.dtos.department;

import java.util.List;

/**
 * Response model for listing departments.
 *
 * <p>Contains the available departments along with the current user's ability to create new ones.</p>
 *
 * <ul>
 *   <li><b>departments</b> – list of department entries (data + permissions)</li>
 *   <li><b>canCreate</b> – whether the user can create departments</li>
 * </ul>
 *
 * @author kkusy
 */
public record DepartmentListResponse (
        List<DepartmentListItemResponse> departments,
        boolean canCreate
) {
}
