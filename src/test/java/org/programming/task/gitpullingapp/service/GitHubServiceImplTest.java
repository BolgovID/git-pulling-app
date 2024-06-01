package org.programming.task.gitpullingapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.exception.ApiRateLimitExceededException;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.mapper.GitHubMapperImpl;
import org.programming.task.gitpullingapp.outgoing.GitHubApiService;
import org.programming.task.gitpullingapp.outgoing.dto.BranchApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.CommitApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.OwnerApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import org.programming.task.gitpullingapp.service.impl.GitHubServiceImpl;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class GitHubServiceImplTest {

    @Mock
    private GitHubApiService gitHubApiService;

    @Mock
    private GitHubMapperImpl gitHubMapper;

    @InjectMocks
    private GitHubServiceImpl gitHubService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnOnlyNotForkedRepository() {
        var username = "username";
        var repositories = generateRepositoryApiResponseList();

        when(gitHubApiService.pullUserRepositories(username))
                .thenReturn(Flux.fromIterable(repositories));
        when(gitHubApiService.pullBranches(anyString(), anyString()))
                .thenReturn(Flux.empty());
        when(gitHubMapper.toBranchDto(any()))
                .thenReturn(new BranchDto("master", "sha1"));
        when(gitHubMapper.toUserRepositoryDto(any(), any()))
                .thenAnswer(invocation -> new UserRepositoryDto(
                        invocation.getArgument(0, RepositoryApiResponse.class).name(),
                        invocation.getArgument(0, RepositoryApiResponse.class).owner().login(),
                        invocation.getArgument(1)
                ));

        StepVerifier.create(gitHubService.getUserNotForkedRepositories(username))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldReturnBranchesAndCommitInfo_whenUserAndRepositoryExist() {
        var username = "username";
        var repository = "repository";
        var branchApiResponse = new BranchApiResponse("master", new CommitApiResponse("sha123"));
        var branchDto = new BranchDto("master", "sha123");

        when(gitHubApiService.pullBranches(username, repository))
                .thenReturn(Flux.just(branchApiResponse));
        when(gitHubMapper.toBranchDto(branchApiResponse))
                .thenReturn(branchDto);

        StepVerifier.create(gitHubService.getAllRepositoryBranches(username, repository))
                .expectNext(branchDto)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListOfBranches_whenUserHasNoRepository() {
        var username = "username";
        var repository = "repository";

        when(gitHubApiService.pullBranches(username, repository))
                .thenReturn(Flux.empty());

        StepVerifier.create(gitHubService.getAllRepositoryBranches(username, repository))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldThrowGitUserNotFoundException_whenUserNotExist() {
        var invalidUsername = "username";
        var invalidRepository = "repository";

        when(gitHubApiService.pullBranches(invalidUsername, invalidRepository))
                .thenReturn(Flux.error(new GitUserNotFoundException(invalidUsername)));

        StepVerifier.create(gitHubService.getAllRepositoryBranches(invalidUsername, invalidRepository))
                .expectError(GitUserNotFoundException.class)
                .verify();
    }

    @Test
    void shouldThrowApiRateLimitException_whenGitApiReturnApiRateLimitExceededMessage() {
        var username = "username";
        var repository = "repository";

        when(gitHubApiService.pullBranches(username, repository))
                .thenReturn(Flux.error(ApiRateLimitExceededException::new));

        StepVerifier.create(gitHubService.getAllRepositoryBranches(username, repository))
                .expectError(ApiRateLimitExceededException.class)
                .verify();
    }

    @Test
    void shouldReturnEmptyRepositoryList_whenAllRepositoriesAreFork() {
        var username = "username";
        var repositories = generateForkRepositoryApiResponseList();

        when(gitHubApiService.pullUserRepositories(username))
                .thenReturn(Flux.fromIterable(repositories));

        when(gitHubApiService.pullBranches(anyString(), anyString()))
                .thenReturn(Flux.empty());

        when(gitHubMapper.toBranchDto(any()))
                .thenReturn(new BranchDto("master", "sha1"));

        when(gitHubMapper.toUserRepositoryDto(any(), any()))
                .thenAnswer(invocation -> new UserRepositoryDto(
                        invocation.getArgument(0, RepositoryApiResponse.class).name(),
                        invocation.getArgument(0, RepositoryApiResponse.class).owner().login(),
                        invocation.getArgument(1)
                ));

        StepVerifier.create(gitHubService.getUserNotForkedRepositories(username))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyList_whenUserDontHasAnyRepository() {
        var username = "username";

        when(gitHubApiService.pullUserRepositories(username))
                .thenReturn(Flux.empty());

        StepVerifier.create(gitHubService.getUserNotForkedRepositories(username))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldThrowException_whenUserNotExist() {
        var username = "username";

        when(gitHubApiService.pullUserRepositories(username))
                .thenReturn(Flux.error(new GitUserNotFoundException(username)));

        StepVerifier.create(gitHubService.getUserNotForkedRepositories(username))
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