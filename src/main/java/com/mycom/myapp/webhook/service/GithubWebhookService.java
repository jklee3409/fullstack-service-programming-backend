package com.mycom.myapp.webhook.service;

import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.gitRepo.InvalidRepositoryNameException;
import com.mycom.myapp.common.exception.custom.github.GithubApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubWebhookService {

    @Value("${github.api.base-url}")
    private String githubApiBaseUrl;

    @Value("${webhook.callback-url}")
    private String callbackUrl;

    @Value("${webhook.secret}")
    private String webhookSecret;

    private final WebClient webClient;

    @Getter
    @AllArgsConstructor
    public static class WebhookResult {
        private String webhookId;
        private String repoUrl;
    }

    public Mono<WebhookResult> createWebhook(String repoFullName, String accessToken) {
        String[] parts = isValidRepoFullName(repoFullName);
        String owner = parts[0];
        String repo = parts[1];

        Mono<String> createWebhookMono = webClient.post()
                .uri(githubApiBaseUrl + "/repos/{owner}/{repo}/hooks", owner, repo)
                .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildWebhookRequestBody())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new GithubApiException(ErrorCode.GITHUB_API_ERROR)))
                )
                .bodyToMono(CreateWebhookResponse.class)
                .map(CreateWebhookResponse::getId);

        Mono<String> getRepoUrlMono = webClient.get()
                .uri(githubApiBaseUrl + "/repos/{owner}/{repo}", owner, repo)
                .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new GithubApiException(ErrorCode.GITHUB_API_ERROR)))
                )
                .bodyToMono(GetRepositoryResponse.class)
                .map(GetRepositoryResponse::getHtml_url);

        return Mono.zip(createWebhookMono, getRepoUrlMono)
                .map(tuple -> new WebhookResult(tuple.getT1(), tuple.getT2())); // T1: getRepoUrl 결과, T2: createWebhook 결과
    }

    private String[] isValidRepoFullName(String repoFullName) {
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            log.error("[isValidRepoFullName] Invalid repository full name: {}", repoFullName);
            throw new InvalidRepositoryNameException(ErrorCode.INVALID_REPOSITORY_NAME);
        }
        return parts;
    }

    private CreateWebhookRequest buildWebhookRequestBody() {
        CreateWebhookRequest.Config config = new CreateWebhookRequest.Config(
                callbackUrl,
                "json",
                webhookSecret,
                "0" // SSL 검증 사용 시 "1"
        );
        return new CreateWebhookRequest("web", true,
                new String[]{"push"}, config);
    }

    @Getter
    @AllArgsConstructor
    private static class CreateWebhookRequest {
        private final String name;     // "web" 고정
        private final boolean active;
        private final String[] events;
        private final Config config;

        @Getter
        @AllArgsConstructor
        private static class Config {
            private final String url;
            private final String content_type;
            private final String secret;
            private final String insecure_ssl;
        }
    }

    @Getter
    private static class CreateWebhookResponse {
        private String id; // webhookId
    }

    @Getter
    private static class GetRepositoryResponse {
        private String html_url;
    }
}
