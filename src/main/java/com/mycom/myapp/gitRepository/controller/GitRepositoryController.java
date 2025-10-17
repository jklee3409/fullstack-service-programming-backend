package com.mycom.myapp.gitRepository.controller;

import com.mycom.myapp.auth.config.CustomUserDetails;
import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.gitRepository.dto.request.RegisterRepositoryRequestDto;
import com.mycom.myapp.gitRepository.dto.response.RegisterRepositoryResponseDto;
import com.mycom.myapp.gitRepository.service.impl.GitRepositoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
public class GitRepositoryController {
    private final GitRepositoryServiceImpl gitRepositoryService;

    @PostMapping
    @Operation(summary = "Github repository 등록", description = "Github repository를 등록하고, webhook을 생성합니다.")
    public BaseResponseDto<RegisterRepositoryResponseDto> registerRepository(
            @RequestBody RegisterRepositoryRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        String githubId = userDetails.getUsername();
        RegisterRepositoryResponseDto response = gitRepositoryService.registerRepository(request, githubId);
        return BaseResponseDto.success(response);
    }
}
