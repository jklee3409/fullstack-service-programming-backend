package com.mycom.myapp.notification.service;

import com.mycom.myapp.notification.dto.NotificationDto;
import com.mycom.myapp.notification.entity.Notification;
import com.mycom.myapp.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        List<Notification> notifications = notificationRepository.findByFcmTokenOrderByCreatedAtDesc(fcmToken);

        return notifications.stream()
                .map(NotificationDto::from)
                .toList();
    }
}
