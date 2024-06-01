package org.programming.task.gitpullingapp.outgoing;

import lombok.RequiredArgsConstructor;
import org.programming.task.gitpullingapp.exception.ApiRateLimitExceededException;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.outgoing.dto.BranchApiResponse;
import org.programming.task.gitpullingapp.outgoing.dto.RepositoryApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GitHubApiService {
    public static final Integer NOT_FOUND_ERROR_CODE = 404;
    public static final Integer API_RATE_LIMIT_EXCEEDED_ERROR_CODE = 403;

    private final WebClient webClient;

    public Flux<RepositoryApiResponse> pullUserRepositories(String username) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users/{username}/repos").build(username))
                .retrieve()
                .onStatus(status -> status.value() == NOT_FOUND_ERROR_CODE, response -> Mono.error(new GitUserNotFoundException(username)))
                .onStatus(status -> status.value() == API_RATE_LIMIT_EXCEEDED_ERROR_CODE, response -> Mono.error(ApiRateLimitExceededException::new))
                .bodyToFlux(RepositoryApiResponse.class);
    }

    public Flux<BranchApiResponse> pullBranches(String username, String repositoryName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/repos/{username}/{repositoryName}/branches").build(username, repositoryName))
                .retrieve()
                .onStatus(status -> status.value() == API_RATE_LIMIT_EXCEEDED_ERROR_CODE, response -> Mono.error(ApiRateLimitExceededException::new))
                .bodyToFlux(BranchApiResponse.class);
    }
}
