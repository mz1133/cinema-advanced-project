package org.app.notification.service;



import lombok.extern.slf4j.Slf4j;
import org.app.notification.model.Notification;
import org.app.notification.repository.NotificationRepository;
import org.app.user.model.User;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createNotification(User user, String message, String type) {



        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        log.info("Notification with id: { %s }, has been created to user username: {%s}".formatted(notification.getId(), user.getUsername()));

    }



    @Transactional(readOnly = true)
    public List<Notification> getUnreadForUser(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }


    @Transactional
    public void deleteNotification(UUID notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized");
        }

        notification.setRead(true);
        notification.setDeleted(true);
        notificationRepository.save(notification);

        log.info("Notification with id: { %s }, has been deleted".formatted(notificationId));
    }


    public List<Notification> getNotificationsForUser(UUID id) {

        return notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(id);
    }

    public void setReadNotification(UUID id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.setRead(true);

        notificationRepository.save(notification);

    }



}
