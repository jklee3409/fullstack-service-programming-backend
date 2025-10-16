package com.mycom.myapp.service.impl;

import com.mycom.myapp.entity.RefreshToken;
import com.mycom.myapp.entity.User;
import com.mycom.myapp.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtServiceImpl {

    private final SecretKey key;
    private final RefreshTokenRepository refreshTokenRepository;

    private final long accessTokenValidity = 60 * 60 * 1000L; // 1시간
    private final long refreshTokenValidity = 14 * 24 * 60 * 60 * 1000L; // 14일

    public JwtServiceImpl (@Value("${jwt.secret}") String secret,
                           RefreshTokenRepository refreshTokenRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(User user) {
        log.info("Generating access token for user ID: {}", user.getId());
        return generateToken(user, accessTokenValidity);
    }

    @Transactional
    public String generateAndSaveRefreshToken(User user) {
        String refreshToken = generateToken(user, refreshTokenValidity);
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());

        if (existingToken.isPresent()) {
            existingToken.get().updateToken(refreshToken);
            log.info("Updated existing refresh token for user ID: {}", user.getId());
        } else {
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshToken)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        }
        return refreshToken;
    }

    private String generateToken(User user, long validity) {
        return Jwts.builder()
                .setSubject(user.getGithubId())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(key)
                .compact();
    }
}
