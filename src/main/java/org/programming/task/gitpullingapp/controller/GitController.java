package org.programming.task.gitpullingapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.exception.GitUserNotFoundException;
import org.programming.task.gitpullingapp.service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Slf4j
public class GitController {
    private final GitHubService gitHubService;

    @GetMapping(value = "/{username}/repositories")
    public Mono<ResponseEntity<List<UserRepositoryDto>>> getUserRepositories(
            @PathVariable String username
    ) {
        log.info("Retrieving repositories for {}", username);
        return gitHubService.getUserNotForkedRepositories(username)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.error(new GitUserNotFoundException(username)));
    }
}
