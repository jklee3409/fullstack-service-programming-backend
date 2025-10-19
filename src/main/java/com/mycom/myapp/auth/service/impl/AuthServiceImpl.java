package com.mycom.myapp.auth.service.impl;

import com.mycom.myapp.auth.dto.response.RefreshTokenResponseDto;
import com.mycom.myapp.auth.entity.RefreshToken;
import com.mycom.myapp.auth.repository.RefreshTokenRepository;
import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.auth.CustomJwtException;
import com.mycom.myapp.common.exception.custom.user.UserNotFoundException;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtServiceImpl jwtService;

    @Transactional
    public RefreshTokenResponseDto refreshTokens(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            log.warn("[refreshTokens] 유효하지 않은 리프레쉬 토큰: {}", refreshToken);
            throw new CustomJwtException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String githubId = jwtService.extractGithubId(refreshToken);
        User user = findUserByGithubId(githubId);
        RefreshToken foundRefreshToken = findRefreshTokenByUser(user);

        // 리프레시 토큰 일치 여부 확인
        if (!foundRefreshToken.getToken().equals(refreshToken)) {
            refreshTokenRepository.delete(foundRefreshToken);
            log.warn("[refreshTokens] 리프레쉬 토큰이 일치하지 않습니다.");
            throw new CustomJwtException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateAndSaveRefreshToken(user);

        return new RefreshTokenResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String githubId) {
        User user = findUserByGithubId(githubId);
        refreshTokenRepository.deleteByUserId(user.getId());
        log.info("[logout] 사용자 {}의 리프레쉬 토큰을 삭제하였습니다.", githubId);
    }

    private User findUserByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private RefreshToken findRefreshTokenByUser(User user) {
        return refreshTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("[refreshTokens] 사용자 {}의 리프레쉬 토큰을 찾을 수 없습니다.", user.getId());
                    return new CustomJwtException(ErrorCode.INVALID_REFRESH_TOKEN);
                });
    }
}
