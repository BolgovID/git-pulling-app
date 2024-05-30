package org.programming.task.gitpullingapp.outgoing.dto;

public record BranchApiResponse(
        String name,
        CommitApiResponse commit
) {
}
