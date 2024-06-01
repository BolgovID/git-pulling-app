package org.programming.task.gitpullingapp.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.programming.task.gitpullingapp.GitPullingAppApplication;
import org.programming.task.gitpullingapp.controller.dto.BranchDto;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.outgoing.dto.BranchApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.CommitApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.OwnerApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@WireMockTest(httpPort = 8181)
@ContextConfiguration(classes = GitPullingAppApplication.class)
@AutoConfigureWebTestClient
class GitControllerITest {

    @Autowired
    private WebTestClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnOneUserRepository_whenUserExist() {
        var repositories = generateListOfRepositoryApiResponse();
        var repositoriesJsonApiResponse = objectMapper.valueToTree(repositories);

        var branches = generateListOfBranchApiResponse();
        var branchJsonApiResponse = objectMapper.valueToTree(branches);

        var expectedResult = new UserRepositoryDto("repo1", "username", generateListOfBranchDto());

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

        webClient.get().uri(uriBuilder -> uriBuilder.path("/api/github/{username}/repositories").build("username"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserRepositoryDto.class)
                .contains(expectedResult);
    }

    @Test
    void shouldThrowGitUserNotFoundException_whenUserNotExist() {
        stubFor(get(urlPathMatching("/users/?[a-zA-Z0-9]*/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        webClient.get()
                .uri("/github/username/repositories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();
    }

    @Test
    void shouldThrowUnsupportedAcceptHeaderException_whenAcceptHeaderIsDifferentFromApplicationJson() {
        webClient.get()
                .uri("/github/username/repositories")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(406);
    }

    private List<BranchDto> generateListOfBranchDto() {
        var branch1 = new BranchDto("master", "sha1");
        var branch2 = new BranchDto("develop", "sha2");

        return List.of(branch1, branch2);
    }

    private List<BranchApiResponse> generateListOfBranchApiResponse() {
        var branchResp1 = new BranchApiResponse("master", new CommitApiResponse("sha1"));
        var branchResp2 = new BranchApiResponse("develop", new CommitApiResponse("sha2"));

        return List.of(branchResp1, branchResp2);
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