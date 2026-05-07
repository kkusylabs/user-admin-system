package io.github.kkusylabs.useradmin.client.ui.error;

import io.github.kkusylabs.useradmin.client.core.api.BadRequestException;
import io.github.kkusylabs.useradmin.client.core.api.ConflictException;
import io.github.kkusylabs.useradmin.client.core.api.ForbiddenException;
import io.github.kkusylabs.useradmin.client.core.api.NotFoundException;
import io.github.kkusylabs.useradmin.client.core.api.ServerErrorException;
import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;
import io.github.kkusylabs.useradmin.client.core.api.ValidationException;

public final class ApiErrorHandler {

	private ApiErrorHandler() {
	}

	public static void handle(Exception e, Runnable reLoginAction, Runnable refreshAction) {

		if (e instanceof UnauthorizedException) {
			reLoginAction.run();
			return;
		}
		if (e instanceof NotFoundException) {
			showError("The item no longer exists.");
			refreshAction.run();
			return;
		}
		if (e instanceof ForbiddenException) {
			showError("You do not have permission to do that.");
			return;
		}
		if (e instanceof ConflictException) {
			showError("The operation conflicted with existing data.");
			return;
		}
		if (e instanceof ValidationException || e instanceof BadRequestException) {
			showError("Please correct the entered values.");
			return;
		}
		if (e instanceof ServerErrorException) {
			showError("The server failed to process the request.");
			return;
		}

		showError("An unexpected error occurred.");
	}

	private static void showError(String message) {
		// open dialog / set error message in UI
	}
}
