package io.github.kkusylabs.useradmin.client.ui.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;

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
	public void execute(IWorkbench workbench) {
		workbench.close();
	}
}