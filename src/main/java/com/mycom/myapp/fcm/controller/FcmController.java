package com.mycom.myapp.fcm.controller;

import com.mycom.myapp.auth.config.CustomUserDetails;
import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.fcm.dto.FcmTokenRequestDto;
import com.mycom.myapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final UserService userService;

    @PostMapping("/token")
    public BaseResponseDto<Void> registerToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FcmTokenRequestDto request
    ) {
        String githubId = userDetails.getUsername();
        log.info("[registerToken] 사용자 {} 의 FCM 토큰 등록 요청: {}", githubId, request.fcmToken());
        userService.updateFcmToken(githubId, request.fcmToken());
        return BaseResponseDto.success(null);
    }
}
