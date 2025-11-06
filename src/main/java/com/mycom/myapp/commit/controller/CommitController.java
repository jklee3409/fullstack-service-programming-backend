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
    @Operation(
            summary = "커밋 요약 목록 조회 API",
            description = """
        ## 특정 리포지토리의 커밋 요약 목록(페이지네이션)
        리포지토리 소유권을 확인한 뒤, 최신 순으로 커밋 요약 페이지를 반환합니다.

        **경로 변수**
        - `repositoryId` (Long, 필수)

        **쿼리 파라미터**
        - `page` (int, 기본 0)
        - `size` (int, 기본 20)

        **권한**
        - 인증 필요 (Bearer Access Token)

        **주요 실패 코드**
        - 30002: REPOSITORY_NOT_FOUND - 저장소를 찾을 수 없습니다.
        - 30003: REPOSITORY_ACCESS_DENIED - 해당 리포지토리에 대한 접근 권한이 없습니다.
        - 20000: USER_NOT_FOUND - 사용자를 찾을 수 없습니다.
        """
    )
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
    @Operation(
            summary = "커밋 상세 조회 API",
            description = """
        ## 특정 커밋의 상세 정보 반환
        커밋 메타/파일/요약 등의 상세 정보를 반환합니다.

        **경로 변수**
        - `commitId` (Long, 필수)

        **권한**
        - 인증 필요 (Bearer Access Token)

        **주요 실패 코드**
        - 60000: COMMIT_NOT_FOUND - 커밋을 찾을 수 없습니다.
        - 30003: REPOSITORY_ACCESS_DENIED - 해당 리포지토리에 대한 접근 권한이 없습니다. (해당 커밋 소유 리포지토리 기준)
        - 20000: USER_NOT_FOUND - 사용자를 찾을 수 없습니다.
        """
    )
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
