package org.programming.task.gitpullingapp.outgoing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RepositoryApiResponse(
        String name,
        OwnerApiResponse owner,
        @JsonProperty("fork")
        boolean isFork
) {
}
