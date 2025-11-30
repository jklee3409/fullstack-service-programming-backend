package com.mycom.myapp.notification.repository;

import com.mycom.myapp.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByFcmTokenOrderByCreatedAtDesc(String fcmToken);
}
