package org.app.event;

import org.app.event.events.ReviewDeleteEvent;
import org.app.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReviewDeleteEventListener {

    private final static String TYPE_NOTIFICATION = "Delete Review";
    private final static String DELETE_REVIEW_MESSAGE = "Your review has been deleted. Reason: %s";

    private final NotificationService notificationService;

    public ReviewDeleteEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @EventListener
    public void handleReviewDeleted(ReviewDeleteEvent event) {



        notificationService.createNotification(
                event.getUser(),
                DELETE_REVIEW_MESSAGE.formatted(event.getReasonMessage()),
                TYPE_NOTIFICATION
        );
    }
}
