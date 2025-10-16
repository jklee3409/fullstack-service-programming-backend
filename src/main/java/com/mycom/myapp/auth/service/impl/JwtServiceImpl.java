package com.mycom.myapp.auth.service.impl;

import com.mycom.myapp.auth.entity.RefreshToken;
import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.CustomJwtException;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
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

    // 토큰 검증
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String githubId = extractGithubId(token);
        return (githubId.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // GitHub ID 추출
    public String extractGithubId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // User ID 추출
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    // 특정 클레임 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 토큰 생성
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

    // 모든 클레임 추출
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new CustomJwtException(ErrorCode.UNSUPPORTED_JWT);
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            throw new CustomJwtException(ErrorCode.MALFORMED_JWT);
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new CustomJwtException(ErrorCode.INVALID_SIGNATURE);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new CustomJwtException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    // 토큰 만료 여부 확인
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // 토큰 만료 시간 추출
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
