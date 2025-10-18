package com.mycom.myapp.webhook.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
public class GithubWebhookController {

    @PostMapping("/github")
    public ResponseEntity<Void> handleGithubWebhook() {
        log.info("[handleGithubWebhook] GitHub webhook received");
        return ResponseEntity.ok().build();
    }
}
