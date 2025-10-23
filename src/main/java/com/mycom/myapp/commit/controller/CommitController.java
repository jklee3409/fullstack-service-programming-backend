package com.mycom.myapp.commit.controller;

import com.mycom.myapp.auth.config.CustomUserDetails;
import com.mycom.myapp.commit.dto.CommitGroupPageDto;
import com.mycom.myapp.commit.dto.response.GetCommitDetailResponseDto;
import com.mycom.myapp.commit.service.CommitService;
import com.mycom.myapp.common.dto.base.BaseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Commit API", description = "커밋 목록 및 분석 결과 조회 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommitController {
    private final CommitService commitService;

    @GetMapping("/repositories/{repositoryId}/commits")
    @Operation(summary = "커밋 요약 목록 조회 API", description = "특정 리포지토리의 커밋 요약을 날짜별로 묶어 반환합니다.")
    public BaseResponseDto<CommitGroupPageDto> getCommitSummaries(
            @PathVariable Long repositoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String githubId = userDetails.getUsername();
        log.info("[getCommitSummaries] 커밋 요약 목록 조회 요청: repositoryId={}, page={}, size={}, githubId={}",
                repositoryId, page, size, githubId);

        CommitGroupPageDto response = commitService.getCommitsSummariesByRepository(repositoryId, githubId, page, size);
        return BaseResponseDto.success(response);
    }

    @GetMapping("/commits/{commitId}")
    @Operation(summary = "커밋 상세 조회 API", description = "특정 커밋의 상세 정보를 반환합니다.")
    public BaseResponseDto<GetCommitDetailResponseDto> getCommitDetail(
            @PathVariable Long commitId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String githubId = userDetails.getUsername();
        log.info("[getCommitDetail] 커밋 상세 조회 요청: commitId={}, githubId={}", commitId, githubId);

        GetCommitDetailResponseDto response = commitService.getCommitDetail(commitId, githubId);
        return BaseResponseDto.success(response);
    }
}
