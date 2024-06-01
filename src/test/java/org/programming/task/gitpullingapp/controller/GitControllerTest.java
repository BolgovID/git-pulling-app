package org.programming.task.gitpullingapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.service.impl.GitHubServiceImpl;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

class GitControllerTest {

    @Mock
    private GitHubServiceImpl gitHubService;

    @InjectMocks
    private GitController gitController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnListOfUser_whenUserExist() {
        var username = "user";
        var userRepository = new UserRepositoryDto("repo", "user", List.of());
        var repositories = List.of(userRepository);

        when(gitHubService.getUserNotForkedRepositories(username)).thenReturn(Flux.fromIterable((repositories)));

        StepVerifier.create(gitController.getUserRepositories(username))
                .expectNext(userRepository)
                .verifyComplete();
    }

    @Test
    void shouldThrowGitUserNotFoundException_whenUserNotExist() {
        var username = "nonExistentUser";
        when(gitHubService.getUserNotForkedRepositories(username))
                .thenReturn(Flux.error(new GitUserNotFoundException("nonExistentUser")));

        StepVerifier.create(gitController.getUserRepositories(username))
                .expectErrorMatches(GitUserNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void shouldReturnEmptyRepositoryList_whenUserDoesntHaveAnyRepositories() {
        var username = "user";
        when(gitHubService.getUserNotForkedRepositories(username))
                .thenReturn(Flux.empty());

        StepVerifier.create(gitController.getUserRepositories(username))
                .expectNextCount(0)
                .verifyComplete();
    }
}