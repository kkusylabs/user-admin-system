package io.github.kkusylabs.useradmin.client.ui.runtime;

@FunctionalInterface
public interface ExceptionDialogMessageMapper {

	String toDialogMessage(
			Throwable exception,
			String fallbackMessage);

}
