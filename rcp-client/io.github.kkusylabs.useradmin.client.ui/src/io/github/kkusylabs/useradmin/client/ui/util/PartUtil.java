package io.github.kkusylabs.useradmin.client.ui.util;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import io.github.kkusylabs.useradmin.client.ui.part.DepartmentPart;
import io.github.kkusylabs.useradmin.client.ui.part.UserPart;

/**
 * Utility methods for working with Eclipse E4 parts and shared
 * part-level workflows.
 */
public final class PartUtil {

	private PartUtil() {
	}

	/**
	 * Returns the instantiated part object for the specified part identifier.
	 *
	 * @param <T> expected part type
	 * @param partService Eclipse part service
	 * @param partId Eclipse part identifier
	 * @param type expected part object type
	 * @return instantiated part object or {@code null}
	 */
	public static <T> T getPartObject(
			EPartService partService,
			String partId,
			Class<T> type) {

		MPart part = partService.findPart(partId);

		if (part == null || !type.isInstance(part.getObject())) {
			return null;
		}

		return type.cast(part.getObject());
	}

	/**
	 * Returns whether any known administration part currently has
	 * unsaved changes.
	 *
	 * @param partService Eclipse part service
	 * @return {@code true} if unsaved changes exist
	 */
	public static boolean hasPendingChanges(EPartService partService) {
		UserPart userPart =
				getPartObject(
						partService,
						UserPart.ID,
						UserPart.class);

		DepartmentPart departmentPart =
				getPartObject(
						partService,
						DepartmentPart.ID,
						DepartmentPart.class);

		return (userPart != null && userPart.hasPendingChanges())
				|| (departmentPart != null && departmentPart.hasPendingChanges());
	}
}
