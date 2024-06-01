package org.programming.task.gitpullingapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.mapper.GitHubMapper;
import org.programming.task.gitpullingapp.outgoing.GitHubApiService;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import org.programming.task.gitpullingapp.service.GitHubService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubService {
    private final GitHubApiService gitHubApiService;
    private final GitHubMapper gitHubMapper;

    @Override
    public Flux<UserRepositoryDto> getUserNotForkedRepositories(String username) {
        return gitHubApiService
                .pullUserRepositories(username)
                .filter(repository -> !repository.fork())
                .flatMap(this::getBranchesAndMapToDto);
    }

    @Override
    public Flux<BranchDto> getAllRepositoryBranches(String username, String repository) {
        return gitHubApiService.pullBranches(username, repository)
                .map(gitHubMapper::toBranchDto);
    }

    private Mono<UserRepositoryDto> getBranchesAndMapToDto(RepositoryApiResponse repository) {
        return getAllRepositoryBranches(repository.owner().login(), repository.name())
                .collectList()
                .map(branches -> gitHubMapper.toUserRepositoryDto(repository, branches));
    }
}
