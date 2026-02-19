package com.grab.api.service;

import com.grab.api.service.domain.notification.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private final SimpMessagingTemplate messagingTemplate;

  public NotificationService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void sendNotification(Notification notification) {
    var destination = "/topic/user/" + notification.recipientId();
    messagingTemplate.convertAndSend(
        destination, new WebSocketMessage(notification.title(), notification.payload()));
  }

  public record WebSocketMessage(String title, String payload) {}
}
