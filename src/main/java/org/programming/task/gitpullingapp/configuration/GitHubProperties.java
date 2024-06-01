package org.programming.task.gitpullingapp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.github")
public record GitHubProperties(
        String token,
        String url,
        String version
){}
