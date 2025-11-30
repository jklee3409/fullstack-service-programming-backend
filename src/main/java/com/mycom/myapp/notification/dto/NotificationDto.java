package com.mycom.myapp.notification.dto;

import com.mycom.myapp.notification.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationDto {
    private final Long id;
    private final String title;
    private final String body;
    private final boolean read;
    private final LocalDateTime createdAt;

    public NotificationDto(Long id, String title, String body, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.read = read;
        this.createdAt = createdAt;
    }

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

