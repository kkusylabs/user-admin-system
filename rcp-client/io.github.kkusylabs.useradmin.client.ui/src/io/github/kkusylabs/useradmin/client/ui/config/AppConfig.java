package io.github.kkusylabs.useradmin.client.ui.config;

public class AppConfig {

    private final String baseUrl;

    public AppConfig() {
        this.baseUrl = System.getProperty(
            "useradmin.api.baseUrl",
            "http://localhost:8080"
        );
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
