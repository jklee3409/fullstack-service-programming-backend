package com.mycom.myapp.service.impl;

import com.mycom.myapp.client.GithubClient;
import com.mycom.myapp.dto.AuthTokens;
import com.mycom.myapp.entity.User;
import com.mycom.myapp.service.OAuthService;
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
        String accessToken = tokenResponse.get("access_token");
        log.info("GitHub access token received: {}", accessToken);

        Map<String, Object> userInfo = githubClient.getUserInfo(accessToken);
        log.info("GitHub user info retrieved: {}", userInfo);

        User user = userService.getOrRegisterUser(userInfo);
        log.info("Authenticated User ID: {}", user.getId());

        String jwtAccessToken = jwtService.generateAccessToken(user);
        String jwtRefreshToken = jwtService.generateAndSaveRefreshToken(user);

        return new AuthTokens(jwtAccessToken, jwtRefreshToken);
    }
}
