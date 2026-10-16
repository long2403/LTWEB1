package com.example.lab1.controller;

import com.example.lab1.entity.ReaderNotification;
import com.example.lab1.repository.ReaderNotificationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me/notifications")
public class ReaderNotificationController {
    private final ReaderNotificationRepository notifications;

    public ReaderNotificationController(ReaderNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<ReaderNotification> all(Authentication authentication) {
        return notifications.findTop50ByRecipientOrderByCreatedAtDesc(authentication.getName());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        return Map.of("count", notifications.countByRecipientAndReadFalse(authentication.getName()));
    }

    @PostMapping("/{id}/read")
    @Transactional
    public void markRead(@PathVariable Integer id, Authentication authentication) {
        ReaderNotification notification = notifications.findByIdAndRecipient(id, authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thông báo không tồn tại."));
        notification.markRead();
    }

    @PostMapping("/read-all")
    @Transactional
    public Map<String, Integer> markAllRead(Authentication authentication) {
        return Map.of("updated", notifications.markAllRead(authentication.getName()));
    }
}
