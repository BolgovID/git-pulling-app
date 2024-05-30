package org.programming.task.gitpullingapp.controller;

import org.junit.jupiter.api.Test;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.when;


@WebFluxTest(GitController.class)
class GithubControllerITest {

    @Autowired
    private WebTestClient testClient;

    @MockBean
    private GitHubService githubService;

    @Test
    void shouldReturnOneUserRepository_whenUserExist() {
        var branchesDto = List.of(new BranchDto("master", "master-sha1"));
        var userRepositoryDto = new UserRepositoryDto("repo1", "owner", branchesDto);
        var repositoryDto = List.of(userRepositoryDto);

        when(githubService.getUserNotForkedRepositories("username"))
                .thenReturn(Mono.just(repositoryDto));

        testClient.get().uri("/api/github/username/repositories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserRepositoryDto.class).contains(userRepositoryDto);
    }

    @Test
    void shouldThrowGitUserNotFoundException_whenUserNotExist() {
        when(githubService.getUserNotForkedRepositories("unknownUser"))
                .thenReturn(Mono.error(new GitUserNotFoundException("unknownUser")));

        testClient.get()
                .uri("/github/unknownUser/repositories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();
    }

    @Test
    void shouldThrowUnsupportedAcceptHeaderException_whenAcceptHeaderIsDifferentFromApplicationJson() {
        testClient.get()
                .uri("/github/username/repositories")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(406);
    }
}


