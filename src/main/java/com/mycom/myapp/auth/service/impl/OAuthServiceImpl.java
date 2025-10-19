package com.mycom.myapp.auth.service.impl;

import com.mycom.myapp.auth.client.GithubClient;
import com.mycom.myapp.auth.dto.AuthTokens;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.auth.service.OAuthService;
import com.mycom.myapp.user.service.impl.UserServiceImpl;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {
    private final GithubClient githubClient;
    private final UserServiceImpl userService;
    private final JwtServiceImpl jwtService;

    @Override
    @Transactional
    public AuthTokens loginWithGithub(String code) {
        Map<String, String> tokenResponse = githubClient.getAccessToken(code);
        String githubAccessToken = tokenResponse.get("access_token");
        log.info("[loginWithGithub] Github access token을 받았습니다.");

        Map<String, Object> userInfo = githubClient.getUserInfo(githubAccessToken);
        log.info("[loginWithGithub] GitHub 사용자 정보가 도착했습니다: {}", userInfo);

        User user = userService.getOrRegisterUser(userInfo);
        log.info("[loginWithGithub] Authenticated User ID: {}", user.getId());

        user.updateGithubAccessToken(githubAccessToken);

        String jwtAccessToken = jwtService.generateAccessToken(user);
        String jwtRefreshToken = jwtService.generateAndSaveRefreshToken(user);

        return new AuthTokens(jwtAccessToken, jwtRefreshToken);
    }
}
