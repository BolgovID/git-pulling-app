package org.programming.task.gitpullingapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programming.task.gitpullingapp.controller.dto.UserRepositoryDto;
import org.programming.task.gitpullingapp.service.GitHubService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Slf4j
public class GitController {
    private final GitHubService gitHubService;

    @GetMapping(value = "/{username}/repositories")
    public Flux<UserRepositoryDto> getUserRepositories(@PathVariable String username) {
        log.info("Retrieving repositories for {}", username);
        return gitHubService.getUserNotForkedRepositories(username);
    }
}
