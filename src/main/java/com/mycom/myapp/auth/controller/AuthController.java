package com.mycom.myapp.auth.controller;

import com.mycom.myapp.auth.dto.AuthTokens;
import com.mycom.myapp.auth.dto.request.GithubLoginRequestDto;
import com.mycom.myapp.auth.service.OAuthService;
import com.mycom.myapp.common.dto.base.BaseResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final OAuthService oAuthService;

    @PostMapping("/login/github")
    public BaseResponseDto<AuthTokens> githubLogin(@RequestBody GithubLoginRequestDto request) {
        AuthTokens authTokens = oAuthService.loginWithGithub(request.getCode());
        return BaseResponseDto.success(authTokens);
    }
}

