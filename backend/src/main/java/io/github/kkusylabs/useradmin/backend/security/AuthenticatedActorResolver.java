package io.github.kkusylabs.useradmin.backend.security;

import io.github.kkusylabs.useradmin.backend.dtos.common.AuthenticatedActor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedActorResolver {

    public AuthenticatedActor fromJwt(Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        String username = jwt.getSubject();

        return new AuthenticatedActor(actorId, username);
    }
}