package com.mycom.myapp.commit.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommitResponseDto {
    private Long id;
    private String commitSha;
    private String originalCommitMessage;
    private String authorName;
    private LocalDateTime committedDate;
    private String summary;
}
