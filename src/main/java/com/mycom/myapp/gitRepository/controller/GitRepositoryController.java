package com.mycom.myapp.gitRepository.controller;

import com.mycom.myapp.auth.config.CustomUserDetails;
import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.gitRepository.dto.request.RegisterRepositoryRequestDto;
import com.mycom.myapp.gitRepository.dto.response.GetRepositoryResponseDto;
import com.mycom.myapp.gitRepository.dto.response.RegisterRepositoryResponseDto;
import com.mycom.myapp.gitRepository.service.impl.GitRepositoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Github Repository API", description = "Github Repository 등록 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
public class GitRepositoryController {
    private final GitRepositoryServiceImpl gitRepositoryService;

    @PostMapping
    @Operation(
            summary = "Github Repository 등록",
            description = """
        ## 사용자의 Github 리포지토리 등록 및 Webhook 생성
        리포지토리의 `full_name`을 바탕으로 **중복 여부를 검증**하고, GitHub에 **Webhook**을 생성합니다.
        성공 시 내부에 리포지토리 메타를 저장하고 등록 결과를 반환합니다.

        **요청 바디**
        - `repoFullName` (String, 필수): 예) `owner/repo`

        **권한**
        - 인증 필요 (Bearer Access Token)

        **주요 실패 코드**
        - 20000: USER_NOT_FOUND - 사용자를 찾을 수 없습니다.
        - 30000: ALREADY_REGISTERED_REPOSITORY - 이미 등록된 저장소입니다.
        - 40000: GITHUB_API_ERROR - 깃허브 API 호출 중 오류가 발생했습니다.
        - 40001: JSON_PARSING_ERROR - JSON 파싱 중 오류가 발생했습니다.
        """
    )
    public BaseResponseDto<RegisterRepositoryResponseDto> registerRepository(
            @RequestBody RegisterRepositoryRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String githubId = userDetails.getUsername();
        RegisterRepositoryResponseDto response = gitRepositoryService.registerRepository(request, githubId);
        return BaseResponseDto.success(response);
    }

    @GetMapping
    @Operation(
            summary = "등록된 리포지토리 목록 조회",
            description = """
        ## 사용자가 등록한 Github 리포지토리 목록 반환
        현재 로그인한 사용자 기준으로 등록된 리포지토리 리스트를 반환합니다.

        **권한**
        - 인증 필요 (Bearer Access Token)

        **주요 실패 코드**
        - 20000: USER_NOT_FOUND - 사용자를 찾을 수 없습니다.
        """
    )
    public BaseResponseDto<List<GetRepositoryResponseDto>> getRepositories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String githubId = userDetails.getUsername();
        List<GetRepositoryResponseDto> result = gitRepositoryService.getAllRepositories(githubId);
        return BaseResponseDto.success(result);
    }
}
