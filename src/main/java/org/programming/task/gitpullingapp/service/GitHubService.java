package org.programming.task.gitpullingapp.service;

import lombok.RequiredArgsConstructor;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.mapper.GitHubMapper;
import org.programming.task.gitpullingapp.outgoing.GitApiCallService;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubService {
    private final GitApiCallService gitApiCallService;
    private final GitHubMapper gitHubMapper;

    public Mono<List<UserRepositoryDto>> getUserNotForkedRepositories(String username) {
        return gitApiCallService
                .pullUserRepositories(username)
                .filter(repository -> !repository.isFork())
                .flatMap(this::getBranchesAndMapToDto)
                .collectList();
    }

    private Mono<UserRepositoryDto> getBranchesAndMapToDto(RepositoryApiResponse repository) {
        return gitApiCallService.pullBranches(repository.owner().login(), repository.name())
                .map(gitHubMapper::toBranchDto)
                .collectList()
                .map(branches -> gitHubMapper.toUserRepositoryDto(repository, branches))                ;
    }
}
