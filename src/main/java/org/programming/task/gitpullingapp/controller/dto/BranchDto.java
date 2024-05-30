package org.programming.task.gitpullingapp.controller.dto;

public record BranchDto(
        String branchName,
        String lastCommit
) {
}
