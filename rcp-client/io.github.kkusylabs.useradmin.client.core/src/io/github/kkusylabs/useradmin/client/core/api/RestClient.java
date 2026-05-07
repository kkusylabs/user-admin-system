package io.github.kkusylabs.useradmin.client.core.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kkusylabs.useradmin.client.core.auth.AuthTokenProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class RestClient {

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final Duration requestTimeout;
	private final AuthTokenProvider authTokenProvider;

	public RestClient(HttpClient httpClient, ObjectMapper objectMapper, String baseUrl, Duration requestTimeout,
			AuthTokenProvider authTokenProvider) {
		this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
		this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
		this.baseUrl = normalizeBaseUrl(baseUrl);
		this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
		this.authTokenProvider = authTokenProvider;
	}

	public <T> T get(String path, Class<T> responseType) {
		HttpRequest request = requestBuilder(path).GET().build();

		return send(request, responseType);
	}

	public <T> T get(String path, Map<String, ?> queryParams, Class<T> responseType) {
		String fullPath = appendQueryParams(path, queryParams);

		HttpRequest request = requestBuilder(fullPath).GET().build();

		return send(request, responseType);
	}

	public <T> T post(String path, Object requestBody, Class<T> responseType) {
		String json = serialize(requestBody);

		HttpRequest request = requestBuilder(path).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json)).build();

		return send(request, responseType);
	}

	public <T> T put(String path, Object requestBody, Class<T> responseType) {
		String json = serialize(requestBody);

		HttpRequest request = requestBuilder(path).header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

		return send(request, responseType);
	}

	public void delete(String path) {
		HttpRequest request = requestBuilder(path).DELETE().build();

		sendWithoutBody(request);
	}

	public <T> T delete(String path, Class<T> responseType) {
		HttpRequest request = requestBuilder(path).DELETE().build();

		return send(request, responseType);
	}

	public <T> T patch(String path, Object requestBody, Class<T> responseType) {
		String json = serialize(requestBody);

		HttpRequest request = requestBuilder(path).header("Content-Type", "application/json")
				.method("PATCH", HttpRequest.BodyPublishers.ofString(json)).build();

		return send(request, responseType);
	}

	private HttpRequest.Builder requestBuilder(String path) {
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl + ensureLeadingSlash(path)))
				.timeout(requestTimeout).header("Accept", "application/json");

		String token = getAuthToken();
		if (token != null && !token.isBlank()) {
			builder.header("Authorization", "Bearer " + token);
		}

		return builder;
	}

	private <T> T send(HttpRequest request, Class<T> responseType) {
		HttpResponse<String> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			throw new RestClientException("I/O error during HTTP call", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RestClientException("HTTP call interrupted", e);
		}

		int status = response.statusCode();
		String body = response.body();

		if (status >= 200 && status < 300) {
			if (responseType == Void.class) {
				return null;
			}

			if (body == null || body.isBlank()) {
				return null;
			}

			try {
				return objectMapper.readValue(body, responseType);
			} catch (IOException e) {
				throw new RestClientException("Failed to deserialize response. Status=" + status + ", body=" + body, e);
			}
		}

		throw mapError(status, body);
	}

	private void sendWithoutBody(HttpRequest request) {
		HttpResponse<String> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			throw new RestClientException("I/O error during HTTP call", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RestClientException("HTTP call interrupted", e);
		}

		int status = response.statusCode();
		if (status < 200 || status >= 300) {
			throw mapError(status, response.body());
		}
	}

	private String serialize(Object requestBody) {
		try {
			return objectMapper.writeValueAsString(requestBody);
		} catch (IOException e) {
			throw new RestClientException("Failed to serialize request body", e);
		}
	}

	private RuntimeException mapError(int status, String body) {
		String message = "HTTP " + status + " returned from server. Body=" + body;

		return switch (status) {
		case 400 -> new ValidationException(message);
		case 401 -> new UnauthorizedException(message);
		case 403 -> new ForbiddenException(message);
		case 404 -> new NotFoundException(message);
		case 409 -> new ConflictException(message);
		default -> {
			if (status >= 500) {
				yield new ServerErrorException(message);
			}
			yield new RestClientException(message);
		}
		};
	}

	private String getAuthToken() {
		return authTokenProvider == null ? null : authTokenProvider.getToken();
	}

	private static String normalizeBaseUrl(String value) {
		Objects.requireNonNull(value, "baseUrl must not be null");
		if (value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}
		return value;
	}

	private static String ensureLeadingSlash(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return value.startsWith("/") ? value : "/" + value;
	}

	private static String appendQueryParams(String path, Map<String, ?> queryParams) {
		if (queryParams == null || queryParams.isEmpty()) {
			return path;
		}

		StringJoiner joiner = new StringJoiner("&");
		for (Map.Entry<String, ?> entry : queryParams.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}

			String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
			String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
			joiner.add(key + "=" + value);
		}

		String query = joiner.toString();
		if (query.isEmpty()) {
			return path;
		}

		return path + (path.contains("?") ? "&" : "?") + query;
	}
}
