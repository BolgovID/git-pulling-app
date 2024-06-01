package org.programming.task.gitpullingapp.outgoing.dto;

public record RepositoryApiResponse(
        String name,
        OwnerApiResponse owner,
        boolean fork
) {
}
