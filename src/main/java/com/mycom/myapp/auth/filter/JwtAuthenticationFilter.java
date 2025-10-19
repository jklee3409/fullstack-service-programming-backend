package com.mycom.myapp.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycom.myapp.auth.service.impl.JwtServiceImpl;
import com.mycom.myapp.common.constant.FilterConstant;
import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.common.dto.base.ErrorResponseDto;
import com.mycom.myapp.common.exception.code.ErrorCode;
import com.mycom.myapp.common.exception.custom.auth.CustomJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtServiceImpl jwtService;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 공개된 경로는 인증 없이 통과
        if (isPassedList(request)) {
            log.debug("[JwtFilter] passed-list에 대한 요청입니다. URI: {}.", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String jwt, githubId;

        // 보호된 경로인데 토큰이 없는 경우
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("[JwtFilter] protected path 요청에 토큰이 존재하지 않습니다: {}", request.getRequestURI());
            writeErrorResponse(response, ErrorCode.MISSING_TOKEN);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            githubId = jwtService.extractGithubId(jwt);

            // SecurityContext에 인증 정보가 없는 경우에만 인증 처리
            if (StringUtils.hasText(githubId) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(githubId);

                if (jwtService.isValidToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("[JwtFilter] 유효하지 않은 JWT 토큰입니다. URI: {}", request.getRequestURI());
                    writeErrorResponse(response, ErrorCode.INVALID_TOKEN);
                    return;
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("[JwtFilter] Access token이 만료되었습니다. URI: {}", request.getRequestURI());
            writeErrorResponse(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (CustomJwtException e) {
            log.warn("[JwtFilter] 유효하지 않은 JWT 토큰입니다. Error: {}, URI: {}", e.getErrorCode().getName(), request.getRequestURI());
            writeErrorResponse(response, e.getErrorCode());
        }
    }

    private boolean isPassedList(HttpServletRequest request) {
        String uri = request.getRequestURI();

        for (String path : FilterConstant.WHITE_LIST) {
            if (pathMatcher.match(path, uri)) {
                return true;
            }
        }

        return false;
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        log.info("[writeErrorResponse] ErrorCode: {}", errorCode);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        BaseResponseDto<ErrorResponseDto> fail = BaseResponseDto.fail(errorCode);

        String jsonResponse = new ObjectMapper().writeValueAsString(fail);
        response.getWriter().write(jsonResponse);
    }
}
