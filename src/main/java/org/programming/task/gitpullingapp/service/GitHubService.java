package org.programming.task.gitpullingapp.service;

import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import reactor.core.publisher.Flux;

public interface GitHubService {
    Flux<UserRepositoryDto> getUserNotForkedRepositories(String username);

    Flux<BranchDto> getAllRepositoryBranches(String username, String repository);
}
