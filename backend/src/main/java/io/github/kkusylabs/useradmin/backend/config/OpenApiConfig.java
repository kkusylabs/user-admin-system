package io.github.kkusylabs.useradmin.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI (Swagger) documentation for the application.
 * <p>
 * This configuration:
 * <ul>
 *     <li>Defines a JWT Bearer authentication scheme</li>
 *     <li>Enables the "Authorize" button in Swagger UI</li>
 *     <li>Applies the security scheme globally to all endpoints</li>
 * </ul>
 * <p>
 * This does not enforce security at runtime. Actual request authorization
 * is handled by Spring Security configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(schemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}