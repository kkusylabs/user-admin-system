package io.github.kkusylabs.useradmin.backend.dtos.common;

import java.util.List;

public record AuthenticatedActor(
        Long actorId,
        String username,
        List<String> roles) {
}
