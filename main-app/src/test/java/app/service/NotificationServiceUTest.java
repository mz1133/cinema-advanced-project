package app.service;

import org.app.notification.model.Notification;
import org.app.notification.repository.NotificationRepository;
import org.app.notification.service.NotificationService;
import org.app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;


    @Test
    void createNotification_happyPath_savesNotification() {

        User user = new User();
        user.setUsername("john");

        String message = "You have a new comment";
        String type = "COMMENT";

        notificationService.createNotification(user, message, type);

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository)
                .save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();

        assertNotNull(savedNotification);
        assertEquals(user, savedNotification.getUser());
        assertEquals(message, savedNotification.getMessage());
        assertEquals(type, savedNotification.getType());
        assertFalse(savedNotification.isRead());
        assertFalse(savedNotification.isDeleted());
        assertNotNull(savedNotification.getCreatedAt());
    }


    @Test
    void getUnreadForUser_happyPath_returnsUnreadNotifications() {

        UUID userId = UUID.randomUUID();

        Notification notification1 = Notification.builder()
                .id(UUID.randomUUID())
                .message("Message 1")
                .type("COMMENT")
                .build();

        Notification notification2 = Notification.builder()
                .id(UUID.randomUUID())
                .message("Message 2")
                .type("REVIEW")
                .build();

        List<Notification> notifications =
                List.of(notification1, notification2);

        when(notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId))
                .thenReturn(notifications);

        List<Notification> result =
                notificationService.getUnreadForUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(notifications, result);

        verify(notificationRepository)
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }


    @Test
    void getUnreadForUser_noNotifications_returnsEmptyList() {

        UUID userId = UUID.randomUUID();

        when(notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<Notification> result =
                notificationService.getUnreadForUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(notificationRepository)
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }


    @Test
    void getUnreadCount_happyPath_returnsUnreadCount() {

        UUID userId = UUID.randomUUID();

        when(notificationRepository
                .countByUserIdAndIsReadFalse(userId))
                .thenReturn(5L);

        long result =
                notificationService.getUnreadCount(userId);

        assertEquals(5L, result);

        verify(notificationRepository)
                .countByUserIdAndIsReadFalse(userId);
    }


    @Test
    void getUnreadCount_noUnreadNotifications_returnsZero() {

        UUID userId = UUID.randomUUID();

        when(notificationRepository
                .countByUserIdAndIsReadFalse(userId))
                .thenReturn(0L);

        long result =
                notificationService.getUnreadCount(userId);

        assertEquals(0L, result);

        verify(notificationRepository)
                .countByUserIdAndIsReadFalse(userId);
    }


    @Test
    void deleteNotification_happyPath_marksNotificationAsReadAndDeleted() {

        UUID notificationId = UUID.randomUUID();
        String username = "john";

        User user = new User();
        user.setUsername(username);

        Notification notification = Notification.builder()
                .id(notificationId)
                .user(user)
                .message("Test notification")
                .type("COMMENT")
                .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notificationId, username);

        assertTrue(notification.isRead());
        assertTrue(notification.isDeleted());

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }


    @Test
    void deleteNotification_notificationDoesNotExist_throwsException() {

        UUID notificationId = UUID.randomUUID();
        String username = "john";

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.deleteNotification(
                        notificationId,
                        username
                )
        );

        assertEquals(
                "Notification not found",
                exception.getMessage()
        );

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }


    @Test
    void deleteNotification_usernameDoesNotMatch_throwsException() {

        UUID notificationId = UUID.randomUUID();

        User user = new User();
        user.setUsername("john");

        Notification notification = Notification.builder()
                .id(notificationId)
                .user(user)
                .message("Test notification")
                .type("COMMENT")
                .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> notificationService.deleteNotification(
                        notificationId,
                        "otherUser"
                )
        );

        assertEquals("Unauthorized", exception.getMessage());

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));

        assertFalse(notification.isRead());
        assertFalse(notification.isDeleted());
    }


    @Test
    void getNotificationsForUser_happyPath_returnsNotifications() {

        UUID userId = UUID.randomUUID();

        List<Notification> notifications = List.of(
                Notification.builder()
                        .id(UUID.randomUUID())
                        .message("Message 1")
                        .type("COMMENT")
                        .build(),
                Notification.builder()
                        .id(UUID.randomUUID())
                        .message("Message 2")
                        .type("REVIEW")
                        .build()
        );

        when(notificationRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId))
                .thenReturn(notifications);

        List<Notification> result =
                notificationService.getNotificationsForUser(userId);

        assertNotNull(result);
        assertEquals(notifications, result);

        verify(notificationRepository)
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }


    @Test
    void getNotificationsForUser_noNotifications_returnsEmptyList() {

        UUID userId = UUID.randomUUID();

        when(notificationRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<Notification> result =
                notificationService.getNotificationsForUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(notificationRepository)
                .findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }


    @Test
    void setReadNotification_happyPath_marksNotificationAsRead() {

        UUID notificationId = UUID.randomUUID();

        Notification notification = Notification.builder()
                .id(notificationId)
                .message("Test notification")
                .type("COMMENT")
                .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        notificationService.setReadNotification(notificationId);

        assertTrue(notification.isRead());

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }


    @Test
    void setReadNotification_notificationDoesNotExist_throwsException() {

        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.setReadNotification(notificationId)
        );

        assertEquals(
                "Notification not found",
                exception.getMessage()
        );

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}