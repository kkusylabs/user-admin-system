package io.github.kkusylabs.useradmin.client.core.api;

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
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kkusylabs.useradmin.client.core.auth.AuthTokenProvider;

public class RestClient {

	private final URI baseUri;
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final AuthTokenProvider tokenProvider;
	private final ErrorMapper errorMapper;
	private final Duration requestTimeout;

	public RestClient(
			URI baseUri,
			HttpClient httpClient,
			ObjectMapper objectMapper,
			AuthTokenProvider tokenProvider,
			ErrorMapper errorMapper,
			Duration requestTimeout) {

		this.baseUri = Objects.requireNonNull(baseUri);
		this.httpClient = Objects.requireNonNull(httpClient);
		this.objectMapper = Objects.requireNonNull(objectMapper);
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.errorMapper = Objects.requireNonNull(errorMapper);
		this.requestTimeout = Objects.requireNonNull(requestTimeout);
	}
	
	public RestClient(
			URI baseUri,
			HttpClient httpClient,
			ObjectMapper objectMapper,
			AuthTokenProvider tokenProvider,
			Duration requestTimeout) {

		this(
				baseUri,
				httpClient,
				objectMapper,
				tokenProvider,
				DefaultErrorMapper.INSTANCE,
				requestTimeout);
	}

	public <T> T get(
			String path,
			Class<T> responseType) {

		return get(path, Map.of(), responseType);
	}

	public <T> T get(
			String path,
			Map<String, ?> queryParameters,
			Class<T> responseType) {

		HttpRequest request = requestBuilder(path, queryParameters)
				.GET()
				.build();

		return send(request, responseType);
	}

	public <T> T post(
			String path,
			Object requestBody,
			Class<T> responseType) {

		return post(
				path,
				Map.of(),
				requestBody,
				responseType);
	}

	public <T> T post(
			String path,
			Map<String, ?> queryParameters,
			Object requestBody,
			Class<T> responseType) {

		HttpRequest request = requestBuilder(path, queryParameters)
				.header("Content-Type", "application/json")
				.POST(jsonBody(requestBody))
				.build();

		return send(request, responseType);
	}

	public <T> T put(
			String path,
			Object requestBody,
			Class<T> responseType) {

		return put(
				path,
				Map.of(),
				requestBody,
				responseType);
	}

	public <T> T put(
			String path,
			Map<String, ?> queryParameters,
			Object requestBody,
			Class<T> responseType) {

		HttpRequest request = requestBuilder(path, queryParameters)
				.header("Content-Type", "application/json")
				.PUT(jsonBody(requestBody))
				.build();

		return send(request, responseType);
	}
	
	public <T> T patch(
			String path,
			Object requestBody,
			Class<T> responseType) {

		return patch(
				path,
				Map.of(),
				requestBody,
				responseType);
	}
	
	protected <T> T patch(
			String path,
			Map<String, ?> queryParameters,
			Object requestBody,
			Class<T> responseType) {

		HttpRequest request = requestBuilder(path, queryParameters)
				.header("Content-Type", "application/json")
				.method("PATCH", jsonBody(requestBody))
				.build();

		return send(request, responseType);
	}

	public void delete(String path) {
		delete(path, Map.of());
	}

	public void delete(
			String path,
			Map<String, ?> queryParameters) {

		HttpRequest request = requestBuilder(path, queryParameters)
				.DELETE()
				.build();

		send(request, Void.class);
	}

	private HttpRequest.Builder requestBuilder(
			String path,
			Map<String, ?> queryParameters) {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(resolve(path, queryParameters))
				.timeout(requestTimeout)
				.header("Accept", "application/json");

		String token = tokenProvider.getToken();

		if (token != null && !token.isBlank()) {
			builder.header(
					"Authorization",
					"Bearer " + token);
		}

		return builder;
	}

	private <T> T send(
			HttpRequest request,
			Class<T> responseType) {

		try {

			HttpResponse<String> response =
					httpClient.send(
							request,
							HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() >= 400) {

				RestErrorResponse error =
						new RestErrorResponse(
								response.statusCode(),
								response.body(),
								response.headers().map());

				throw errorMapper.map(error, null);
			}

			return readResponseBody(
					response.body(),
					responseType);

		} catch (RestClientException e) {

			throw e;

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();

			throw new RestClientException(
					"Request was interrupted.",
					e);

		} catch (IOException e) {

			throw new RestClientException(
					"Request failed.",
					e);
		}
	}

	private HttpRequest.BodyPublisher jsonBody(
			Object requestBody) {

		try {

			return HttpRequest.BodyPublishers.ofString(
					objectMapper.writeValueAsString(
							requestBody));

		} catch (JsonProcessingException e) {

			throw new RestClientException(
					"Failed to serialize request body.",
					e);
		}
	}

	private <T> T readResponseBody(
			String body,
			Class<T> responseType) {

		if (responseType == Void.class) {
			return null;
		}

		if (responseType == String.class) {
			return responseType.cast(body);
		}

		if (body == null || body.isBlank()) {
			return null;
		}

		try {

			return objectMapper.readValue(
					body,
					responseType);

		} catch (JsonProcessingException e) {

			throw new RestClientException(
					"Failed to deserialize response body.",
					e);
		}
	}

	private URI resolve(
			String path,
			Map<String, ?> queryParameters) {

		String normalizedPath = path == null ? "" : path;

		while (normalizedPath.startsWith("/")) {
			normalizedPath = normalizedPath.substring(1);
		}

		URI uri = baseUri.toString().endsWith("/")
				? baseUri.resolve(normalizedPath)
				: URI.create(baseUri + "/").resolve(normalizedPath);

		if (queryParameters == null
				|| queryParameters.isEmpty()) {
			return uri;
		}

		String query = queryParameters.entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.map(entry ->
						encode(entry.getKey())
								+ "="
								+ encode(String.valueOf(
										entry.getValue())))
				.collect(Collectors.joining("&"));

		if (query.isBlank()) {
			return uri;
		}

		String separator =
				uri.getQuery() == null ? "?" : "&";

		return URI.create(uri + separator + query);
	}

	private String encode(String value) {
		return URLEncoder.encode(
				value,
				StandardCharsets.UTF_8);
	}
}
