package org.app.scheduling;


import org.app.notification.service.NotificationService;

import org.app.user.model.User;
import org.app.user.service.UserService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class SubscriptionScheduler {

    private static final String MESSAGE_SUBSCRIPTION_EXPIRED_AFTER_3_DAYS = "Your subscription is expiring in 3 days! Renew it to keep enjoying uninterrupted access, expired date: %s";
    private static final String TYPE_NOTIFICATION = "System";

    private final UserService userService;
    private final NotificationService notificationService;

    public SubscriptionScheduler(UserService userService,
                                 NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }


    @Scheduled(cron = "0 1 0 * * *")
    public void notifyUsersAboutExpiringSubscriptions() {


        LocalDate targetDate = LocalDate.now().plusDays(3);


        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        List<User> usersWithExpiringSub = userService.getUsersBySubscriptionExpirationDate(startOfDay, endOfDay);

        for (User user : usersWithExpiringSub) {
            String message = String.format(MESSAGE_SUBSCRIPTION_EXPIRED_AFTER_3_DAYS, user.getSubscription().getExpirationDate());
            notificationService.createNotification(user, message, TYPE_NOTIFICATION);
        }
    }
}
