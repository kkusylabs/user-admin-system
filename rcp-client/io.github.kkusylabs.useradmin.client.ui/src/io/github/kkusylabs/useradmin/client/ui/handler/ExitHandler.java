package io.github.kkusylabs.useradmin.client.ui.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.ui.part.DepartmentPart;
import io.github.kkusylabs.useradmin.client.ui.part.UserPart;

/**
 * Eclipse command handler responsible for closing the application.
 */
public class ExitHandler {

	/**
	 * Closes the Eclipse workbench and exits the application.
	 *
	 * @param workbench Eclipse workbench instance
	 */
	@Execute
	public void execute(
			IWorkbench workbench,
			EPartService partService,
			Shell shell) {

		UserPart userPart =
				getPartObject(partService, UserPart.ID, UserPart.class);

		DepartmentPart departmentPart =
				getPartObject(partService, DepartmentPart.ID, DepartmentPart.class);

		boolean hasPendingChanges =
				(userPart != null && userPart.hasPendingChanges()) ||
				(departmentPart != null && departmentPart.hasPendingChanges());

		if (hasPendingChanges) {
			boolean discard = MessageDialog.openQuestion(
					shell,
					"Discard Changes?",
					"You have unsaved changes. Exit anyway?");

			if (!discard) {
				return;
			}
		}

		workbench.close();
	}

	private <T> T getPartObject(
			EPartService partService,
			String partId,
			Class<T> type) {

		MPart part = partService.findPart(partId);

		if (part == null || !type.isInstance(part.getObject())) {
			return null;
		}

		return type.cast(part.getObject());
	}
}