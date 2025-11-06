package com.mycom.myapp.webhook.controller;

import com.mycom.myapp.webhook.dto.WebhookPayloadDto;
import com.mycom.myapp.webhook.service.GithubWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Github Webhook API", description = "Github Webhook 리스너 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhook")
public class GithubWebhookController {
    private final GithubWebhookService webhookService;

    @PostMapping("/github")
    @Operation(
            summary = "GitHub Webhook 수신(API Listener)",
            description = """
        ## GitHub Webhook 이벤트 수신 및 처리
        GitHub가 전송하는 Webhook 요청을 수신합니다. **push 이벤트**만 처리합니다.
        이벤트 본문은 `WebhookPayloadDto.PushPayload`로 역직렬화되며, 내부적으로 다음을 수행합니다.
        1) 저장소(full_name) 검증 및 조회  
        2) GitHub API를 통해 커밋 diff 획득  
        3) AI 커밋 분석(Gemini) 호출  
        4) 커밋/파일 변경 내역 저장 및 FCM 알림 전송

        **요청 헤더**
        - `X-GitHub-Event` (String, 필수): GitHub 이벤트 타입. 현재 `push`만 처리
        - `X-GitHub-Delivery` (String, 권장): 이벤트 고유 식별자(UUID)
        - `X-Hub-Signature-256` (String, 권장): 서명 검증 헤더(서명 검증 TODO)

        **요청 바디**
        - `WebhookPayloadDto.PushPayload` 형식 JSON

        **처리 정책**
        - `X-GitHub-Event`가 `push`가 아니면 200 OK로 무시 처리합니다(스킵 로그만 남김).

        **권한**
        - 외부 GitHub Webhook 호출용 엔드포인트(인증 불필요)

        **주요 실패 코드**
        - 30001: INVALID_REPOSITORY_NAME - 올바르지 않은 저장소 이름입니다.
        - 30002: REPOSITORY_NOT_FOUND - 저장소를 찾을 수 없습니다.
        - 40000: GITHUB_API_ERROR - 깃허브 API 호출 중 오류가 발생했습니다.
        - 40001: JSON_PARSING_ERROR - JSON 파싱 중 오류가 발생했습니다.
        - 50000: GEMINI_API_ERROR - Gemini API 호출 중 오류가 발생했습니다.

        **비고**
        - 일부 처리(커밋 저장/AI 분석)는 비동기로 동작합니다(@Async). 컨트롤러는 정상 수신 시 200 OK를 반환합니다.
        """
    )
    public ResponseEntity<Void> handleGithubWebhook(
            @RequestBody WebhookPayloadDto.PushPayload payload,
            @RequestHeader("X-GitHub-Event") String githubEvent
    ) {
        if (!"push".equals(githubEvent)) {
            log.info("[Webhook] Received event '{}', but only processing 'push' events. Skipping.", githubEvent);
            return ResponseEntity.ok().build();
        }

        // TODO: X-Hub-Signature-256 헤더를 사용하여 요청의 진위 여부 검증

        log.info("[Webhook] Push event received for repository: {}", payload.getRepository().getFullName());

        webhookService.processPushEvent(payload);

        return ResponseEntity.ok().build();
    }
}
