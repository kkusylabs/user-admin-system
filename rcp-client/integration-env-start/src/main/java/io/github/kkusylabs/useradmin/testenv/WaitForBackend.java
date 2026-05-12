package io.github.kkusylabs.useradmin.testenv;

import java.net.HttpURLConnection;
import java.net.URL;

public class WaitForBackend {

	private static final int MAX_RETRIES = 60;

	public static void main(String[] args) throws Exception {

		String url = "http://localhost:8081/actuator/health";

		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

			try {

				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

				connection.setConnectTimeout(2000);
				connection.setReadTimeout(2000);

				int responseCode = connection.getResponseCode();

				if (responseCode == 200) {
					System.out.println("Backend is ready: " + url);

					return;
				}

				System.out.println("Backend not ready yet. HTTP " + responseCode);

			} catch (Exception e) {

				System.out.println("Waiting for backend... attempt " + attempt);
			}

			Thread.sleep(2000);
		}

		throw new IllegalStateException("Backend failed to start within timeout");
	}
}