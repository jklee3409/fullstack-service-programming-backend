package com.mycom.myapp.webhook.controller;

import com.mycom.myapp.webhook.dto.WebhookPayloadDto;
import com.mycom.myapp.webhook.service.GithubWebhookService;
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
