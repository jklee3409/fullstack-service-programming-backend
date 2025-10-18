package com.mycom.myapp.auth.controller;

import com.mycom.myapp.auth.dto.AuthTokens;
import com.mycom.myapp.auth.dto.request.GithubLoginRequestDto;
import com.mycom.myapp.auth.dto.response.RefreshTokenResponseDto;
import com.mycom.myapp.auth.service.OAuthService;
import com.mycom.myapp.auth.service.impl.AuthServiceImpl;
import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.auth.CustomJwtException;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 API", description = "사용자 로그인, 로그아웃 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final OAuthService oAuthService;
    private final AuthServiceImpl authService;

    @PostMapping("/login/github")
    @Operation(summary = "GitHub 로그인 API", description = """
            ## GitHub OAuth를 통한 로그인 및 회원가입을 처리합니다.
            클라이언트로부터 전달받은 인가 코드를 사용하여 GitHub로부터 액세스 토큰을 발급받고, 이를 통해 사용자 정보를 조회합니다.
            조회된 사용자 정보를 바탕으로 회원가입 또는 로그인을 수행하고, JWT 액세스 토큰과 리프레시 토큰을 발급하여 반환합니다.
            
            ***
            
            ### 📥 요청 파라미터
            * `code` (String, 필수): GitHub OAuth 인증 후 클라이언트가 받은 인가 코드
            
            ### 🔑 권한
            * 모든 사용자
            
            ### ❌ 주요 실패 코드
            * 10000: UNSUPPORTED_JWT - 지원하지 않는 JWT 토큰입니다.
            * 10001: MALFORMED_JWT - 올바르지 않은 JWT 토큰입니다.
            * 10002: INVALID_SIGNATURE - 잘못된 JWT 서명입니다.
            * 10003: EMPTY_JWT_CLAIMS - JWT claims이 비어있습니다.
            
            ### 📝 참고 사항
            * 이 API는 GitHub OAuth 인증 플로우의 일부로 사용됩니다.
            * 클라이언트는 GitHub 로그인 후 받은 인가 코드를 이 API에 전달해야 합니다.
            """)
    public BaseResponseDto<AuthTokens> githubLogin(@RequestBody GithubLoginRequestDto request) {
        AuthTokens authTokens = oAuthService.loginWithGithub(request.getCode());
        return BaseResponseDto.success(authTokens);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 리프레시 API", description = """
            ## 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
            클라이언트로부터 전달받은 리프레시 토큰을 검증하고, 유효한 경우 새로운 액세스 토큰과 리프레시 토큰을 생성하여 반환합니다.           
            """)
    public BaseResponseDto<RefreshTokenResponseDto> refreshTokens(
        @RequestHeader("Authorization-Refresh") String refreshToken
    ) {
        if (StringUtils.hasText(refreshToken) && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        } else {
            throw new CustomJwtException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshTokenResponseDto response = authService.refreshTokens(refreshToken);
        return BaseResponseDto.success(response);
    }
}

