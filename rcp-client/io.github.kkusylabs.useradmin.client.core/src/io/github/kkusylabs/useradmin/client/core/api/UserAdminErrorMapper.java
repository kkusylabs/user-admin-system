package io.github.kkusylabs.useradmin.client.core.api;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link ErrorMapper} implementation for the User Admin API.
 *
 * <p>Parses API-specific error responses and maps them to the
 * appropriate {@link RestClientException} subtype. Validation
 * errors are mapped to {@link ValidationException} and include
 * field-level validation details when available.
 */
public class UserAdminErrorMapper implements ErrorMapper {

	private final ObjectMapper objectMapper;

	/**
	 * Creates a User Admin API error mapper.
	 *
	 * @param objectMapper object mapper used to deserialize API
	 *        error responses
	 */
	public UserAdminErrorMapper(ObjectMapper objectMapper) {
		this.objectMapper = Objects.requireNonNull(objectMapper);
	}

	@Override
	public RestClientException map(
			RestErrorResponse response,
			Throwable cause) {

		ApiError error = tryParseApiError(response.body());

		String message = getMessage(error, response);

		return switch (response.status()) {

			case 400 -> {
				if (error != null
						&& "VALIDATION_ERROR".equals(error.code())) {

					yield new ValidationException(
							message,
							error.errors());
				}

				yield new BadRequestException(message);
			}

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

	private ApiError tryParseApiError(String body) {

		if (body == null || body.isBlank()) {
			return null;
		}

		try {
			return objectMapper.readValue(
					body,
					ApiError.class);

		} catch (Exception e) {
			return null;
		}
	}

	private String getMessage(
			ApiError error,
			RestErrorResponse response) {

		if (error != null && hasText(error.detail())) {
			return error.detail();
		}

		if (error != null && hasText(error.title())) {
			return error.title();
		}

		if (hasText(response.body())) {
			return response.body();
		}

		return "HTTP " + response.status();
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	/**
	 * User Admin API error response.
	 *
	 * @param detail detailed error message
	 * @param instance request path associated with the error
	 * @param status HTTP status code
	 * @param title error summary
	 * @param timestamp error timestamp
	 * @param code application-specific error code
	 * @param errors field-level validation errors
	 */
	public record ApiError(
			String detail,
			String instance,
			Integer status,
			String title,
			String timestamp,
			String code,
			Map<String, String> errors) {
	}
}