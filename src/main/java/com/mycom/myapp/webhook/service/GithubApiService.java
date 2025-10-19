package com.mycom.myapp.webhook.service;

import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.gitRepo.InvalidRepositoryNameException;
import com.mycom.myapp.common.exception.custom.github.GithubApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubApiService {
    private final WebClient webClient;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    public Mono<String> getCommitDiff(String repoFullName, String commitSha, String accessToken) {
        log.info("[getCommitDiff] 커밋 변경 내역을 조회를 시작합니다. repo: {}, commit: {}", repoFullName, commitSha);

        String[] parts = repoFullName.split("/", 2);
        if (parts.length != 2) throw new InvalidRepositoryNameException(ErrorCode.INVALID_REPOSITORY_NAME);

        String owner = parts[0];
        String repo  = parts[1];
        log.info("[getCommitDiff] owner: {}, repo: {}", owner, repo);

        return webClient.get()
                .uri(builder -> builder
                        .scheme("https")
                        .host(githubApiBaseUrl.replaceFirst("^https?://", ""))
                        .pathSegment("repos", owner, repo, "commits", commitSha)
                        .build())
                .header(HttpHeaders.USER_AGENT, "GitInsight/1.0")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.valueOf("application/vnd.github.v3.diff"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, rsp ->
                        rsp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new GithubApiException(ErrorCode.GITHUB_API_ERROR))))
                .bodyToMono(String.class);
    }
}
