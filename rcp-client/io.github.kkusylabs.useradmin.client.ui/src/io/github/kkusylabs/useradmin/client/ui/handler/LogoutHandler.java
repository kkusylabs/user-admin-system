package io.github.kkusylabs.useradmin.client.ui.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;
import io.github.kkusylabs.useradmin.client.ui.util.PartUtil;

public class LogoutHandler {
	@Execute
	public void execute(
			IEventBroker eventBroker,
			EPartService partService,
			SessionTokenStore tokenStore,
			Shell shell) {

		if (PartUtil.hasPendingChanges(partService)) {
			boolean discard = MessageDialog.openQuestion(
					shell,
					"Discard Changes?",
					"You have unsaved changes. Log out anyway?");

			if (!discard) {
				return;
			}
		}

		tokenStore.clear();
		eventBroker.post(AppTopics.LOGOUT, null);
	}
}
