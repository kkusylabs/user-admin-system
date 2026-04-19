package io.github.kkusylabs.useradmin.backend.config;

import io.github.kkusylabs.useradmin.backend.security.CurrentActorIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentActorIdArgumentResolver currentActorIdArgumentResolver;

    public WebMvcConfig(CurrentActorIdArgumentResolver currentActorIdArgumentResolver) {
        this.currentActorIdArgumentResolver = currentActorIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentActorIdArgumentResolver);
    }
}