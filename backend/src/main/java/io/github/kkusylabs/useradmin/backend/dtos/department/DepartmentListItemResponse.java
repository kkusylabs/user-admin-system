package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Response model for a single department entry in a list.
 *
 * <p>Wraps department details together with the current user's permissions.</p>
 *
 * <ul>
 *   <li><b>department</b> – department data</li>
 *   <li><b>canUpdate</b> – whether the user can update this department</li>
 *   <li><b>canDelete</b> – whether the user can delete this department</li>
 * </ul>
 *
 * @author kkusy
 */
public record DepartmentListItemResponse(
        DepartmentDetailsResponse department,
        boolean canUpdate,
        boolean canDelete
) {
}
