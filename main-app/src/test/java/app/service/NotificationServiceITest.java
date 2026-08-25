package app.service;

import org.app.Application;
import org.app.notification.model.Notification;
import org.app.notification.repository.NotificationRepository;
import org.app.notification.service.NotificationService;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = Application.class)
class NotificationServiceITest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        
        testUser = User.builder()
                .username("pesho")
                .password("password123")
                .email("pesho@example.com")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    void createNotification_shouldSaveSuccessfully() {
        notificationService.createNotification(testUser, "Test message", "INFO");

        List<Notification> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size());
        assertEquals("Test message", notifications.get(0).getMessage());
        assertEquals(testUser.getId(), notifications.get(0).getUser().getId());
        assertFalse(notifications.get(0).isRead());
        assertFalse(notifications.get(0).isDeleted());
    }

    @Test
    void getUnreadForUser_shouldReturnOnlyUnread() {
        Notification n1 = Notification.builder()
                .user(testUser)
                .message("Unread msg")
                .type("INFO")
                .isRead(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification n2 = Notification.builder()
                .user(testUser)
                .message("Read msg")
                .type("INFO")
                .isRead(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .build();

        notificationRepository.saveAll(List.of(n1, n2));

        List<Notification> unread = notificationService.getUnreadForUser(testUser.getId());

        assertEquals(1, unread.size());
        assertEquals("Unread msg", unread.get(0).getMessage());
    }

    @Test
    void getUnreadCount_shouldReturnCorrectNumber() {
        Notification n1 = Notification.builder()
                .user(testUser)
                .message("Msg 1")
                .type("INFO")
                .isRead(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification n2 = Notification.builder()
                .user(testUser)
                .message("Msg 2")
                .type("INFO")
                .isRead(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.saveAll(List.of(n1, n2));

        long count = notificationService.getUnreadCount(testUser.getId());
        assertEquals(2, count);
    }

    @Test
    void setReadNotification_shouldMarkAsRead() {
        Notification notification = Notification.builder()
                .user(testUser)
                .message("To be read")
                .type("INFO")
                .isRead(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);

        notificationService.setReadNotification(notification.getId());

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertTrue(updated.isRead());
    }

    @Test
    void deleteNotification_shouldMarkAsReadAndDeleted_WhenUserMatches() {
        Notification notification = Notification.builder()
                .user(testUser)
                .message("To be deleted")
                .type("INFO")
                .isRead(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);

        notificationService.deleteNotification(notification.getId(), "pesho");

        Notification deleted = notificationRepository.findById(notification.getId()).orElseThrow();
        assertTrue(deleted.isRead());
        assertTrue(deleted.isDeleted());
    }

    @Test
    void deleteNotification_shouldThrowSecurityException_WhenUserDoesNotMatch() {
        Notification notification = Notification.builder()
                .user(testUser)
                .message("Secure msg")
                .type("INFO")
                .isRead(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);

        Notification finalNotification = notification;
        assertThrows(SecurityException.class, () -> {
            notificationService.deleteNotification(finalNotification.getId(), "gosho");
        });
    }
}