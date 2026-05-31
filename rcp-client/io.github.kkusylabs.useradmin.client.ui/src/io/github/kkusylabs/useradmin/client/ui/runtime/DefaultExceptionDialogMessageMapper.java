package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.util.Map;

import io.github.kkusylabs.useradmin.client.core.api.RestClientException;
import io.github.kkusylabs.useradmin.client.core.api.ServerErrorException;
import io.github.kkusylabs.useradmin.client.core.api.ValidationException;

/**
 * Default implementation of {@link ExceptionDialogMessageMapper}.
 *
 * <p>Formats exceptions into user-facing dialog messages and provides
 * specialized handling for validation and server errors.
 */
public class DefaultExceptionDialogMessageMapper
		implements ExceptionDialogMessageMapper {

	@Override
	public String toDialogMessage(
			Throwable exception,
			String fallbackMessage) {

		return switch (exception) {

			case ValidationException e ->
					buildValidationMessage(e);

			case ServerErrorException e ->
					defaultMessage(
							e,
							"The server encountered an unexpected error.");

			case RestClientException e ->
					defaultMessage(
							e,
							fallbackMessage);

			default ->
					defaultMessage(
							exception,
							fallbackMessage);
		};
	}

	private String buildValidationMessage(
			ValidationException exception) {

		StringBuilder message =
				new StringBuilder(defaultMessage(
						exception,
						"Validation failed."));

		Map<String, String> errors = exception.getErrors();

		if (!errors.isEmpty()) {
			message.append(System.lineSeparator())
					.append(System.lineSeparator());

			errors.forEach((field, error) -> {
				message.append("• ")
						.append(field)
						.append(": ")
						.append(error)
						.append(System.lineSeparator());
			});
		}

		return message.toString().trim();
	}

	private String defaultMessage(
			Throwable exception,
			String fallback) {

		String message = exception.getMessage();

		return message == null || message.isBlank()
				? fallback
				: message;
	}
}
