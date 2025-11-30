package com.mycom.myapp.notification.service;

import com.mycom.myapp.notification.dto.NotificationDto;
import com.mycom.myapp.notification.entity.Notification;
import com.mycom.myapp.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification saveNotification(String fcmToken, String title, String body) {
        Notification notification = Notification.builder()
                .fcmToken(fcmToken)
                .title(title)
                .body(body)
                .build();
        return notificationRepository.save(notification);
    }

    public List<NotificationDto> getNotifications(String fcmToken) {
        log.info("[getNotifications] 알림 조회 요청 Service Layer 도착.");
        List<Notification> notifications = notificationRepository.findByFcmTokenOrderByCreatedAtDesc(fcmToken);

        log.info("[getNotifications] 알림 조회 성공");
        return notifications.stream()
                .map(NotificationDto::from)
                .toList();
    }
}
