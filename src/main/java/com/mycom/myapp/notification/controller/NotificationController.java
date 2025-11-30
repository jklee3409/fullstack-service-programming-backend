package com.mycom.myapp.notification.controller;

import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.notification.dto.NotificationDto;
import com.mycom.myapp.notification.entity.Notification;
import com.mycom.myapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public BaseResponseDto<List<NotificationDto>> getNotifications(
            @RequestParam("fcmToken") String fcmToken
    ) {
        log.info("[getNotifications] 알림 조회 요청 수신. fcmToken: {}", fcmToken);
        List<NotificationDto> result = notificationService.getNotifications(fcmToken);
        log.info("[getNotifications] 알림 목록 조회 성공.");
        return BaseResponseDto.success(result);
    }
}


