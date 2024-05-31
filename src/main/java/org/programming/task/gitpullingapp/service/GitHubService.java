package org.programming.task.gitpullingapp.service;

import lombok.RequiredArgsConstructor;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.mapper.GitHubMapper;
import org.programming.task.gitpullingapp.outgoing.GitApiCallService;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GitHubService {
    private final GitApiCallService gitApiCallService;
    private final GitHubMapper gitHubMapper;

    public Flux<UserRepositoryDto> getUserNotForkedRepositories(String username) {
        return gitApiCallService
                .pullUserRepositories(username)
                .filter(repository -> !repository.isFork())
                .flatMap(this::getBranchesAndMapToDto);
    }

    public Flux<BranchDto> getAllRepositoryBranches(String username, String repository) {
        return gitApiCallService.pullBranches(username, repository)
                .map(gitHubMapper::toBranchDto);
    }

    private Mono<UserRepositoryDto> getBranchesAndMapToDto(RepositoryApiResponse repository) {
        return getAllRepositoryBranches(repository.owner().login(), repository.name())
                .collectList()
                .map(branches -> gitHubMapper.toUserRepositoryDto(repository, branches));
    }
}
