package com.mycom.myapp.commit.dto.response;

import com.mycom.myapp.commit.dto.CommitFileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetCommitDetailResponseDto {
    private Long commitId;
    private String commitSha;
    private String authorName;
    private String authorEmail;
    private String originalCommitMessage;
    private String summary;
    private String analysisDetails;
    private String commitUrl;
    private CommitFileDto commitFile;
}
