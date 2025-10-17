package com.mycom.myapp.gitRepository.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RegisterRepositoryResponseDto {
    private Long id;
    private String repoFullName;
    private String repoUrl;
    private String webhookId;
    private LocalDateTime createdAt;
}
