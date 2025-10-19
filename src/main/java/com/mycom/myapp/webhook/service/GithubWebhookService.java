package com.mycom.myapp.webhook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.commit.entity.Commit;
import com.mycom.myapp.commit.entity.CommitFile;
import com.mycom.myapp.commit.repository.CommitFileRepository;
import com.mycom.myapp.commit.repository.CommitRepository;
import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.gitRepo.InvalidRepositoryNameException;
import com.mycom.myapp.common.exception.custom.gitRepo.RepositoryNotFoundException;
import com.mycom.myapp.common.exception.custom.github.GithubApiException;
import com.mycom.myapp.common.exception.custom.github.JsonParsingException;
import com.mycom.myapp.gemini.dto.AiCommitAnalysisDto;
import com.mycom.myapp.gemini.service.AiCommitAnalysisService;
import com.mycom.myapp.gitRepository.entity.GitRepository;
import com.mycom.myapp.gitRepository.repository.GitRepositoryRepository;
import com.mycom.myapp.webhook.dto.WebhookPayloadDto;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
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
    private final ObjectMapper objectMapper;
    private final CommitRepository commitRepository;
    private final GitRepositoryRepository gitRepositoryRepository;
    private final CommitFileRepository commitFileRepository;
    private final GithubApiService gitHubApiService;
    private final AiCommitAnalysisService aiCommitAnalysisService;

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
        log.info("[createWebhook] 웹훅 생성 요청을 시작합니다. repo: {}", repo);

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
                        logGithubError(response).flatMap(errorBody ->
                                Mono.error(new GithubApiException(ErrorCode.GITHUB_API_ERROR))
                        )
                )
                .bodyToMono(GetRepositoryResponse.class)
                .map(GetRepositoryResponse::getHtml_url);

        return Mono.zip(createWebhookMono, getRepoUrlMono)
                .map(tuple -> new WebhookResult(tuple.getT1(), tuple.getT2())); // T1: createWebhook 결과, T2: getRepoUrl 결과
    }

    @Transactional
    @Async("taskExecutor")
    public CompletableFuture<Void> processPushEvent(WebhookPayloadDto.PushPayload payload) {
        String repoFullName = payload.getRepository().getFullName();
        log.info("[Webhook] push 이벤트에 대한 처리를 시작합니다. repository: {}", repoFullName);

        GitRepository repository = gitRepositoryRepository.findByRepoFullName(repoFullName)
                .orElseThrow(() -> {
                    log.error("[Webhook] 리포지토리를 찾을 수 없습니다. repo: {}", repoFullName);
                    return new RepositoryNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND);
                });

        String accessToken = repository.getUser().getGithubAccessToken();

        for (WebhookPayloadDto.CommitInfo commitInfo : payload.getCommits()) {
            String commitSha = commitInfo.getId();
            if (commitRepository.existsByCommitSha(commitSha)) {
                log.info("[Webhook] Commit {} 은 이미 처리되었습니다. Skip.", commitSha);
                continue;
            }

            // 1. GitHub에서 Diff 정보 가져오기
            String diffContent = gitHubApiService.getCommitDiff(repoFullName, commitSha, accessToken).block();
            if (diffContent == null || diffContent.isEmpty()) {
                log.warn("[Webhook] Diff 내용이 비어있습니다. {}. Skip.", commitSha);
                continue;
            }

            // 2. Gemini API로 Diff 분석 요청
            AiCommitAnalysisDto analysisResult = aiCommitAnalysisService.analyzeCommitDiff(diffContent);

            // 3. 분석 결과 DB에 저장
            saveCommitAnalysis(repository, commitInfo, analysisResult, diffContent);
        }

        return CompletableFuture.completedFuture(null);
    }

    private void saveCommitAnalysis(GitRepository repository, WebhookPayloadDto.CommitInfo commitInfo, AiCommitAnalysisDto analysisResult, String diffContent) {
        try {
            Commit commit = Commit.builder()
                    .gitRepository(repository)
                    .commitSha(commitInfo.getId())
                    .originalCommitMessage(commitInfo.getMessage())
                    .authorName(commitInfo.getAuthor().getName())
                    .authorEmail(commitInfo.getAuthor().getEmail())
                    .committedDate(commitInfo.getTimestamp().toLocalDateTime())
                    .summary(analysisResult.getSummary())
                    .analysisDetails(objectMapper.writeValueAsString(analysisResult))
                    .status("ANALYZED")
                    .build();
            Commit savedCommit = commitRepository.save(commit);
            log.info("[saveCommitAnalysis] commit {} 을 저장했습니다. repo {}", savedCommit.getCommitSha(), repository.getRepoFullName());

            // Diff를 파싱하여 CommitFile 저장
            List<CommitFile> commitFiles = parseDiffAndCreateFiles(diffContent, savedCommit);
            commitFileRepository.saveAll(commitFiles);
            log.info("[saveCommitAnalysis] {} 개의 file changes를 저장했습니다. commit {}", commitFiles.size(), savedCommit.getCommitSha());

        } catch (JsonProcessingException e) {
            log.error("[saveCommitAnalysis] AI 분석 결과 JSON을 파싱하는 과정에서 오류가 발생했습니다.", e);
            throw new JsonParsingException(ErrorCode.JSON_PARSING_ERROR);
        }
    }

    // Diff 파서
    private List<CommitFile> parseDiffAndCreateFiles(String fullDiff, Commit commit) {
        return Arrays.stream(fullDiff.split("diff --git"))
                .filter(s -> !s.isBlank())
                .map(fileDiff -> {
                    String[] lines = fileDiff.split("\n");
                    String fileNameLine = lines[0]; // " a/path/to/file.java b/path/to/file.java"
                    String fileName = fileNameLine.substring(fileNameLine.indexOf(" b/") + 3).trim();

                    String status = "modified";
                    if (fileDiff.contains("new file mode")) status = "added";
                    if (fileDiff.contains("deleted file mode")) status = "removed";

                    return CommitFile.builder()
                            .commit(commit)
                            .filename(fileName)
                            .status(status)
                            .diff(fileDiff)
                            .build();
                }).toList();
    }

    private String[] isValidRepoFullName(String repoFullName) {
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            log.error("[isValidRepoFullName] 유효하지 않은 리포지토리 이름입니다. repo: {}", repoFullName);
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

    private Mono<String> logGithubError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("[Empty Response Body]")
                .map(errorBody -> {
                    log.error("[logGithubError] GitHub API Error: Status Code = {}, Response Body = {}", response.statusCode(), errorBody);
                    return errorBody;
                });
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
