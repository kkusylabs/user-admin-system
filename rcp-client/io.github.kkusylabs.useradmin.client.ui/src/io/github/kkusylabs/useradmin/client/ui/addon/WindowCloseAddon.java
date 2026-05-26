package io.github.kkusylabs.useradmin.client.ui.addon;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.ui.util.PartUtil;
import jakarta.inject.Inject;

public class WindowCloseAddon {

	@Inject
	private EPartService partService;

	@Inject
	@Optional
	public void onAppStartupComplete(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Object event,
			MApplication app) {

		if (app.getChildren().isEmpty()) {
			return;
		}

		MWindow window = app.getChildren().get(0);

		window.getContext().set(
				IWindowCloseHandler.class,
				(IWindowCloseHandler) this::handleClose);
	}

	private boolean handleClose(MWindow window) {
		if (!PartUtil.hasPendingChanges(partService)) {
			return true;
		}

		Shell shell = window.getWidget() instanceof Shell s ? s : null;

		return MessageDialog.openQuestion(
				shell,
				"Discard Changes?",
				"You have unsaved changes. Exit anyway?");
	}
}
