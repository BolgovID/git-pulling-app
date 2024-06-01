package org.programming.task.gitpullingapp.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.exception.ApiRateLimitExceededException;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.outgoing.GitHubApiService;
import org.programming.task.gitpullingapp.outgoing.dto.BranchApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.CommitApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.OwnerApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import org.programming.task.gitpullingapp.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@WireMockTest(httpPort = 8181)
class GitHubServiceITest {

    @Autowired
    private GitHubService gitHubService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private GitHubApiService gitHubApiService;

    @Test
    void shouldReturnNotForkedRepositories() {
        var repositories = generateListOfRepositoryApiResponse();
        var repositoriesJsonApiResponse = objectMapper.valueToTree(repositories);

        var branches = generateListOfBranchApiResponse();
        var branchJsonApiResponse = objectMapper.valueToTree(branches);

        stubFor(get(urlPathMatching("/users/?[a-zA-Z0-9]*/repos"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(repositoriesJsonApiResponse)
                ));

        stubFor(get(urlPathMatching("/repos/?[a-zA-Z0-9]*/?[a-zA-Z0-9]*/branches"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(branchJsonApiResponse)
                ));

        var repositoryResponseFlux = gitHubService.getUserNotForkedRepositories("username");
        StepVerifier.create(repositoryResponseFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoRepositories() {
        stubFor(get(urlPathMatching("/users/?[a-zA-Z0-9]*/repos"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                ));

        var repositoryResponseFlux = gitHubApiService.pullUserRepositories("emptyuser");

        StepVerifier.create(repositoryResponseFlux)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldThrowAnException_whenUserNotExist() {
        stubFor(get(urlPathMatching("/users/?[a-zA-Z0-9]*/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        var repositoryResponseFlux = gitHubApiService.pullUserRepositories("nonexistentuser");

        StepVerifier.create(repositoryResponseFlux)
                .expectError(GitUserNotFoundException.class)
                .verify();
    }

    @Test
    void shouldThrowAnException_whenGitApiReturnApiRateLimitExceedMessage() {
        stubFor(get(urlPathMatching("/users/?[a-zA-Z0-9]*/repos"))
                .willReturn(aResponse()
                        .withStatus(403)
                ));

        var repositoryResponseFlux = gitHubApiService.pullUserRepositories("rateLimitedUser");

        StepVerifier.create(repositoryResponseFlux)
                .expectError(ApiRateLimitExceededException.class)
                .verify();
    }

    @Test
    void shouldReturnListOfBranches() {
        var branchesGitResponse = generateListOfBranchApiResponse();
        var branchResponseAsJson = objectMapper.valueToTree(branchesGitResponse);
        var expectedBranchesDto = generateListOfBranchDto();

        stubFor(get(urlPathMatching("/repos/?[a-zA-Z0-9]*/?[a-zA-Z0-9]*/branches"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(branchResponseAsJson)
                ));

        var repositoryResponseFlux = gitHubService.getAllRepositoryBranches("username", "repo1");

        StepVerifier.create(repositoryResponseFlux)
                .expectNextSequence(expectedBranchesDto)
                .verifyComplete();
    }

    private List<BranchApiResponse> generateListOfBranchApiResponse() {
        var branchResp1 = new BranchApiResponse("master", new CommitApiResponse("sha1"));
        var branchResp2 = new BranchApiResponse("develop", new CommitApiResponse("sha2"));

        return List.of(branchResp1, branchResp2);
    }

    private List<BranchDto> generateListOfBranchDto() {
        var branchDto1 = new BranchDto("master", "sha1");
        var branchDto2 = new BranchDto("develop", "sha2");
        return List.of(branchDto1, branchDto2);
    }

    private List<RepositoryApiResponse> generateListOfRepositoryApiResponse() {
        var owner = new OwnerApiResponse("username");
        var repository1 = new RepositoryApiResponse("repo1", owner, false);
        var repository2 = new RepositoryApiResponse("fork1", owner, true);
        var repository3 = new RepositoryApiResponse("repo2", owner, false);
        var repository4 = new RepositoryApiResponse("fork2", owner, true);

        return List.of(repository1, repository2, repository3, repository4);
    }
}
