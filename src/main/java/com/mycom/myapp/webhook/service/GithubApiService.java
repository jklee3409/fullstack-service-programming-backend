package com.mycom.myapp.webhook.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubApiService {
    private final WebClient webClient;

    @Value("${github.api.base-url}")
    private String githubApiBaseUrl;

    public Mono<String> getCommitDiff(String repoFullName, String commitSha, String accessToken) {
        log.info("Fetching commit diff for repo: {}, commit: {}", repoFullName, commitSha);
        return webClient.get()
                .uri(githubApiBaseUrl + "/repos/{repoFullName}/commits/{commitSha}", repoFullName, commitSha)
                .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                .header(HttpHeaders.ACCEPT, "application/vnd.github.v3.diff")
                .retrieve()
                .bodyToMono(String.class);
    }
}
