package app.service;

import org.app.Application;
import org.app.notification.model.Notification;
import org.app.notification.repository.NotificationRepository;
import org.app.scheduling.SubscriptionScheduler;
import org.app.subscription.model.Subscription;
import org.app.subscription.repository.SubscriptionRepository;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Transactional
class SubscriptionSchedulerITest {

    @Autowired
    private SubscriptionScheduler subscriptionScheduler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        subscriptionRepository.deleteAll();
    }

    @Test
    void notifyUsersAboutExpiringSubscriptions_shouldCreateNotificationForExpiringUsers() {
        LocalDateTime targetExpiration = LocalDate.now().plusDays(3).atTime(12, 0);

        Subscription subscription = Subscription.builder()
                .price(BigDecimal.valueOf(10.00))
                .startDate(LocalDateTime.now().minusDays(27))
                .expirationDate(targetExpiration)
                .planCode("PREMIUM")
                .active(true)
                .period(30)
                .build();
        subscription = subscriptionRepository.save(subscription);

        User user = User.builder()
                .username("subuser")
                .email("subuser@example.com")
                .password("password123")
                .role(Role.USER)
                .createdOn(LocalDateTime.now().minusDays(27))
                .isActive(true)
                .subscription(subscription)
                .build();
        userRepository.save(user);

        subscriptionScheduler.notifyUsersAboutExpiringSubscriptions();

        List<Notification> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size());
        assertEquals(user.getId(), notifications.get(0).getUser().getId());
        assertTrue(notifications.get(0).getMessage().contains("Your subscription is expiring in 3 days!"));
    }
}