package io.github.kkusylabs.useradmin.client.ui.util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SwtUtil {
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
