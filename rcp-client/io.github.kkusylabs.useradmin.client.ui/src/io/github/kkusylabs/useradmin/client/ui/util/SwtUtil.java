package io.github.kkusylabs.useradmin.client.ui.util;

import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
/**
 * Utility methods for creating and configuring SWT controls.
 */
public class SwtUtil {
	
	/**
	 * Creates a standard SWT push button using Eclipse dialog sizing
	 * conventions.
	 *
	 * <p>
	 * The button width is automatically adjusted to ensure a consistent
	 * minimum size across the application.
	 * </p>
	 *
	 * @param parent parent composite
	 * @param text button label text
	 * @return configured push button
	 */
	public static Button createPushButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);

		PixelConverter converter = new PixelConverter(button);

		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		data.widthHint = Math.max(converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);

		button.setLayoutData(data);

		return button;
	}
}
