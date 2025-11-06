package com.mycom.myapp.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            log.info("FCM token is empty. Skipping notification.");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        try {
            firebaseMessaging.send(message);
            log.info("Successfully sent FCM notification to token: {}", token);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification: {}", e.getMessage());
        }
    }
}
