package org.app.event;


import org.app.event.events.CommentDeletedByAdminEvent;
import org.app.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CommentEventListener {

    private final static String TYPE_NOTIFICATION = "Delete Comment";

    private final NotificationService notificationService;

    public CommentEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @EventListener
    public void handleCommentDeletedByAdmin(CommentDeletedByAdminEvent event) {
        String message = String.format("Your comment id: '%s' was deleted by an administrator. Reason: %s",
                event.getCommentId(),
                event.getReason());

        notificationService.createNotification
                (event.getUser(), message, TYPE_NOTIFICATION);
    }

}
