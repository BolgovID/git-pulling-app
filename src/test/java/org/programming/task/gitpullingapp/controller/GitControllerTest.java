package org.programming.task.gitpullingapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.service.GitHubService;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

class GitControllerTest {

    @Mock
    private GitHubService gitHubService;

    @InjectMocks
    private GitController gitController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnListOfUser_whenUserExist() {
        var username = "user";
        var repositories = List.of(new UserRepositoryDto("repo", "user", List.of()));

        when(gitHubService.getUserNotForkedRepositories(username)).thenReturn(Mono.just(repositories));

        StepVerifier.create(gitController.getUserRepositories(username))
                .expectNext(ResponseEntity.ok(repositories))
                .verifyComplete();
    }

    @Test
    void shouldThrowGitUserNotFoundException_whenUserNotExist() {
        var username = "nonExistentUser";
        when(gitHubService.getUserNotForkedRepositories(username))
                .thenReturn(Mono.error(new GitUserNotFoundException("nonExistentUser")));

        StepVerifier.create(gitController.getUserRepositories(username))
                .expectErrorMatches(GitUserNotFoundException.class::isInstance)
                .verify();
    }

    @Test
    void shouldReturnEmptyRepositoryList_whenUserDoesntHaveAnyRepositories() {
        var username = "user";
        when(gitHubService.getUserNotForkedRepositories(username))
                .thenReturn(Mono.just(List.of()));

        StepVerifier.create(gitController.getUserRepositories(username))
                .expectNext(ResponseEntity.ok(List.of()))
                .verifyComplete();
    }
}