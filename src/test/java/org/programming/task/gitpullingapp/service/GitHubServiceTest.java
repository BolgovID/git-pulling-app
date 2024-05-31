package org.programming.task.gitpullingapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.mapper.GitHubMapperImpl;
import org.programming.task.gitpullingapp.outgoing.GitApiCallService;
import org.programming.task.gitpullingapp.outgoing.dto.OwnerApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class GitHubServiceTest {

    @Mock
    private GitApiCallService gitApiCallService;

    @Mock
    private GitHubMapperImpl gitHubMapper;

    @InjectMocks
    private GitHubService gitHubService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnOnlyNotForkedRepository() {
        var repositories = generateRepositoryApiResponseList();
        when(gitApiCallService.pullUserRepositories("username"))
                .thenReturn(Flux.fromIterable(repositories));

        when(gitApiCallService.pullBranches(anyString(), anyString()))
                .thenReturn(Flux.empty());

        when(gitHubMapper.toBranchDto(any()))
                .thenReturn(new BranchDto("master", "sha1"));

        when(gitHubMapper.toUserRepositoryDto(any(), any()))
                .thenAnswer(invocation -> new UserRepositoryDto(
                        invocation.getArgument(0, RepositoryApiResponse.class).name(),
                        invocation.getArgument(0, RepositoryApiResponse.class).owner().login(),
                        invocation.getArgument(1)
                ));

        StepVerifier.create(gitHubService.getUserNotForkedRepositories("username"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyRepositoryList_whenAllRepositoriesAreFork() {
        var repositories = generateForkRepositoryApiResponseList();
        when(gitApiCallService.pullUserRepositories("username"))
                .thenReturn(Flux.fromIterable(repositories));

        when(gitApiCallService.pullBranches(anyString(), anyString()))
                .thenReturn(Flux.empty());

        when(gitHubMapper.toBranchDto(any()))
                .thenReturn(new BranchDto("master", "sha1"));

        when(gitHubMapper.toUserRepositoryDto(any(), any()))
                .thenAnswer(invocation -> new UserRepositoryDto(
                        invocation.getArgument(0, RepositoryApiResponse.class).name(),
                        invocation.getArgument(0, RepositoryApiResponse.class).owner().login(),
                        invocation.getArgument(1)
                ));

        StepVerifier.create(gitHubService.getUserNotForkedRepositories("username"))
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    void shouldReturnEmptyList_whenUserDontHasAnyRepository() {
        when(gitApiCallService.pullUserRepositories("emptyUser"))
                .thenReturn(Flux.empty());

        StepVerifier.create(gitHubService.getUserNotForkedRepositories("emptyUser"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldThrowException_whenUserNotExist() {
        when(gitApiCallService.pullUserRepositories("nonExistentUser"))
                .thenReturn(Flux.error(new GitUserNotFoundException("nonExistentUser")));

        StepVerifier.create(gitHubService.getUserNotForkedRepositories("nonExistentUser"))
                .expectErrorMatches(GitUserNotFoundException.class::isInstance)
                .verify();
    }

    private List<RepositoryApiResponse> generateRepositoryApiResponseList() {
        return List.of(
                new RepositoryApiResponse("fork1", new OwnerApiResponse("owner1"), true),
                new RepositoryApiResponse("repo1", new OwnerApiResponse("owner1"), false),
                new RepositoryApiResponse("repo2", new OwnerApiResponse("owner1"), false),
                new RepositoryApiResponse("fork2", new OwnerApiResponse("owner1"), true)
        );
    }

    private List<RepositoryApiResponse> generateForkRepositoryApiResponseList() {
        return List.of(
                new RepositoryApiResponse("fork1", new OwnerApiResponse("owner1"), true),
                new RepositoryApiResponse("fork2", new OwnerApiResponse("owner1"), true),
                new RepositoryApiResponse("fork3", new OwnerApiResponse("owner1"), true),
                new RepositoryApiResponse("fork4", new OwnerApiResponse("owner1"), true)
        );
    }
}