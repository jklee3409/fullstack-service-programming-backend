package com.mycom.myapp.gemini.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.commit.entity.Commit;
import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.gemini.GeminiApiException;
import com.mycom.myapp.fcm.service.FcmService;
import com.mycom.myapp.gemini.dto.AiCommitAnalysisDto;
import com.mycom.myapp.gitRepository.entity.GitRepository;
import com.mycom.myapp.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommitAnalysisService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final FcmService fcmService;

    private static final String PROMPT_TEMPLATE = """
            당신은 Git 커밋 diff를 분석하는 전문 프로그래머입니다.
            아래의 diff 내용을 분석하고, 결과를 JSON 형식으로 요약하세요.

            JSON 객체는 다음 세 가지 필드를 포함해야 합니다:
            1. `summary`: 커밋의 목적을 한국어로 한 문장으로 간결하게 요약한 내용
            2. `commitType`: 커밋의 유형을 다음 중 하나로 분류 — FEATURE, FIX, REFACTOR, DOCS, TEST, CHORE
            3. `mainChanges`: 객체 배열로, 각 객체는 `file`(변경된 파일 이름)과 
               `changeDescription`(그 파일에서 이루어진 주요 변경 사항을 한국어로 간단히 설명)을 포함해야 합니다.

            아래는 분석할 git diff입니다:
            ```diff
            {diff_content}
            ```

            응답은 JSON 객체만 순수하게 출력하세요.
            """;

    public AiCommitAnalysisDto analyzeCommitDiff(GitRepository repository, String diff) {
        try {
            log.info("[analyzeCommitDiff] 커밋 AI 분석을 위해 gemini API 호출을 시작합니다.");
            String jsonResponse = chatClient
                    .prompt()
                    .user(PROMPT_TEMPLATE.replace("{diff_content}", diff))
                    .call()
                    .content();

            String cleanJson = cleanJsonString(jsonResponse);
            log.info("[analyzeCommitDiff] gemini API JSON 응답을 수신하였습니다.");

            AiCommitAnalysisDto analysisDto = objectMapper.readValue(cleanJson, AiCommitAnalysisDto.class);

            sendFcmNotification(repository);

            return analysisDto;
        } catch (Exception e) {
            log.error("[analyzeCommitDiff] Gemini API 응답 수신 중 에러가 발생하였습니다.", e);
            throw new GeminiApiException(ErrorCode.GEMINI_API_ERROR);
        }
    }

    private void sendFcmNotification(GitRepository repository) {
        log.info("[sendFcmNotification] AI 분석 완료 알림을 전송합니다.");
        User user = repository.getUser();
        if (user == null || user.getFcmToken() == null) return;

        String repoName = repository.getRepoFullName();
        String title = "GitInsight 분석 완료";
        String body = String.format("[%s] 새로운 커밋 AI 분석이 완료되었어요", repoName);
        log.info("[sendFcmNotification] FCM 알림 전송: title={}, body={}", title, body);

        fcmService.sendNotification(user.getFcmToken(), title, body);
    }

    private String cleanJsonString(String rawResponse) {
        return rawResponse.replace("```json", "").replace("```", "").trim();
    }
}
