package com.mycom.myapp.auth.controller;

import com.mycom.myapp.auth.config.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/github/login")
    @Operation(
            summary = "GitHub 로그인 API",
            description = """
        ## GitHub OAuth 로그인 및 회원 생성
        사용자가 GitHub 로그인 버튼을 통해 전달한 **Authorization Code**를 이용해 GitHub Access Token을 발급받고,
        사용자의 기본 정보를 조회한 뒤 신규 회원을 자동 등록하거나 기존 회원을 반환합니다.

        이후 서버는 **Access Token**과 **Refresh Token**을 발급하여 반환합니다.

        **요청 바디**
        - `code` (String, 필수): GitHub OAuth Redirect 후 전달받은 Authorization Code

        **응답**
        - `accessToken`: 액세스 토큰 (JWT)
        - `refreshToken`: 리프레시 토큰 (JWT)
        - `githubId`: 사용자 GitHub 로그인 ID
        - `username`: 사용자명

        **권한**
        - 비인증 상태에서도 호출 가능

        **주요 실패 코드**
        - 40000: GITHUB_API_ERROR - GitHub API 호출 중 오류가 발생했습니다.
        - 40001: JSON_PARSING_ERROR - GitHub 응답 파싱 중 오류가 발생했습니다.
        - 20001: USER_REGISTER_FAILED - 신규 회원 등록 중 오류가 발생했습니다.
        - 10007: TOKEN_CREATION_FAILED - JWT 생성 중 오류가 발생했습니다.
        - 10004: MISSING_TOKEN - GitHub 토큰이 존재하지 않습니다.
        """
    )
    public BaseResponseDto<AuthTokens> githubLogin(@RequestBody GithubLoginRequestDto request) {
        AuthTokens authTokens = oAuthService.loginWithGithub(request.getCode());
        return BaseResponseDto.success(authTokens);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 리프레시 API",
            description = """
        ## 리프레시 토큰으로 새 액세스/리프레시 토큰 발급
        클라이언트가 보유한 **리프레시 토큰**을 검증하여, 유효하면 새로운 **액세스 토큰**과 **리프레시 토큰**을 발급합니다.

        **요청 헤더**
        - `Authorization-Refresh` (String, 필수): 리프레시 토큰

        **권한**
        - 모든 사용자

        **주요 실패 코드**
        - 10004: MISSING_TOKEN - 토큰이 존재하지 않습니다.
        - 10006: INVALID_REFRESH_TOKEN - 유효하지 않은 리프레시 토큰입니다.
        - 10000: UNSUPPORTED_JWT - 지원하지 않는 JWT 토큰입니다.
        - 10001: MALFORMED_JWT - 올바르지 않은 JWT 토큰입니다.
        - 10002: INVALID_SIGNATURE - 잘못된 JWT 서명입니다.
        - 10003: EMPTY_JWT_CLAIMS - JWT claims이 비어있습니다.
        - 10005: EXPIRED_ACCESS_TOKEN - 액세스 토큰이 만료되었습니다. (액세스 갱신 문맥에서 참고)
        """
    )
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

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃 API",
            description = """
        ## 리프레시 토큰 무효화 및 로그아웃
        서버에 저장된 **리프레시 토큰 레코드**를 삭제하여 이후 토큰 재발급을 차단합니다.

        **권한**
        - 인증 필요 (Bearer Access Token)

        **주요 실패 코드**
        - 20000: USER_NOT_FOUND - 사용자를 찾을 수 없습니다.
        - 10004: MISSING_TOKEN - 토큰이 존재하지 않습니다.
        - 10000: UNSUPPORTED_JWT - 지원하지 않는 JWT 토큰입니다.
        - 10001: MALFORMED_JWT - 올바르지 않은 JWT 토큰입니다.
        - 10002: INVALID_SIGNATURE - 잘못된 JWT 서명입니다.
        - 10003: EMPTY_JWT_CLAIMS - JWT claims이 비어있습니다.
        """
    )
    public BaseResponseDto<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String githubId = userDetails.getUsername();
        authService.logout(githubId);
        return BaseResponseDto.voidSuccess();
    }
}

