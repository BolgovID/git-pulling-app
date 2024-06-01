package org.programming.task.gitpullingapp.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(GitHubProperties.class)
@RequiredArgsConstructor
public class WebClientConfiguration {
    private final GitHubProperties gitHubProperties;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(gitHubProperties.url())
                .defaultHeader("Authorization", "Bearer " + gitHubProperties.token())
                .defaultHeader("X-GitHub-Api-Version", gitHubProperties.version())
                .build();
    }
}
