package com.mycom.myapp.auth.config;

import com.mycom.myapp.auth.filter.JwtAuthenticationFilter;
import com.mycom.myapp.common.constant.FilterConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. 세션 사용 안함
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 화이트리스트 경로는 모두 허용
                        .requestMatchers(FilterConstant.WHITE_LIST).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 5. JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 모든 출처 허용
        configuration.setAllowedOrigins(List.of("*"));

        // 허용할 HTTP 메서드 지정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더 지정 (Authorization, Content-Type 등)
        configuration.setAllowedHeaders(List.of("*"));

        // 인증 정보 포함 여부 (setAllowedOrigins가 *일 때는 false여야 함, 특정 도메인일 경우 true)
        // Flutter 앱은 브라우저 쿠키를 쓰지 않으므로 false로도 통신 가능
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위 설정 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}