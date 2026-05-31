package io.github.kkusylabs.useradmin.client.core.api;

public final class DefaultErrorMapper implements ErrorMapper {

	public static final DefaultErrorMapper INSTANCE =
			new DefaultErrorMapper();

	private DefaultErrorMapper() {
	}

	@Override
	public RestClientException map(
			RestErrorResponse response,
			Throwable cause) {

		String message = getMessage(response);

		return switch (response.status()) {

			case 400 ->
					new BadRequestException(message);

			case 401 ->
					new UnauthorizedException(message);

			case 403 ->
					new ForbiddenException(message);

			case 404 ->
					new NotFoundException(message);

			case 409 ->
					new ConflictException(message);

			default -> {

				if (response.status() >= 500) {
					yield new ServerErrorException(message);
				}

				yield new RestClientException(message, cause);
			}
		};
	}

	private String getMessage(
			RestErrorResponse response) {

		if (response.body() != null
				&& !response.body().isBlank()) {

			return response.body();
		}

		return "HTTP " + response.status();
	}
}