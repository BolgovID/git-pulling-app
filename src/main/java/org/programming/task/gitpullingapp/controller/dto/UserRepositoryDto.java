package org.programming.task.gitpullingapp.controller.dto;

import java.util.List;

public record UserRepositoryDto(
        String repositoryName,
        String ownerLogin,
        List<BranchDto> branches
) {
}
