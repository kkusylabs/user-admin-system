package io.github.kkusylabs.useradmin.client.ui.runtime;

/**
 * Strategy interface for converting exceptions into user-facing
 * dialog messages.
 */
@FunctionalInterface
public interface ExceptionDialogMessageMapper {

	String toDialogMessage(
			Throwable exception,
			String fallbackMessage);

}
