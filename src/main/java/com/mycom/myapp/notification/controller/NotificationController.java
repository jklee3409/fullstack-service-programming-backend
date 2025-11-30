package com.mycom.myapp.notification.controller;

import com.mycom.myapp.common.dto.base.BaseResponseDto;
import com.mycom.myapp.notification.dto.NotificationDto;
import com.mycom.myapp.notification.entity.Notification;
import com.mycom.myapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public BaseResponseDto<List<NotificationDto>> getNotifications(
            @RequestParam("fcmToken") String fcmToken
    ) {
        List<NotificationDto> result = notificationService.getNotifications(fcmToken);
        return BaseResponseDto.success(result);
    }
}


