package io.github.kkusylabs.useradmin.backend.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves the current actor's identifier from the authenticated request.
 *
 * <p>Supports controller method parameters annotated with {@link CurrentActorId}
 * of type {@link Long} or {@code long}. The value is extracted from the
 * {@code userId} claim in the authenticated JWT.</p>
 *
 * <p>This resolver assumes that authentication is performed via
 * {@link JwtAuthenticationToken} and that the JWT contains a numeric
 * {@code userId} claim.</p>
 *
 * @throws IllegalStateException if the authentication is not a JWT or the
 * {@code userId} claim is missing or not numeric
 */
@Component
public class CurrentActorIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> type = parameter.getParameterType();
        return parameter.hasParameterAnnotation(CurrentActorId.class)
                && (type.equals(Long.class) || type.equals(long.class));
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Object principal = webRequest.getUserPrincipal();

        if (principal instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            Object claim = jwt.getClaim("userId");

            if (claim instanceof Number number) {
                return number.longValue();
            }

            throw new IllegalStateException("JWT claim 'userId' is missing or not numeric");
        }

        throw new IllegalStateException("Could not resolve current actor id from authentication");
    }
}
