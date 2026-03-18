package com.grab.api.service;

import com.grab.api.service.domain.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

  private final SimpMessagingTemplate messagingTemplate;

  public NotificationService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void send(Notification notification) {
    var destination = "/topic/user/" + notification.recipientId();
    LOGGER.info(
        "Sending notification: recipientId={}, destination={}, title={}",
        notification.recipientId(),
        destination,
        notification.title());
    messagingTemplate.convertAndSend(
        destination, new WebSocketMessage(notification.title(), notification.payload()));
  }

  public record WebSocketMessage(String title, String payload) {}
}
