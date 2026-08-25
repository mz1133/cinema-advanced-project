package org.app.advice;

import org.app.notification.service.NotificationService;
import org.app.user.model.User;

import org.app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;



@ControllerAdvice
public class GlobalNotificationAdvice {

    private final NotificationService notificationService;
    private final UserService userService;

    public GlobalNotificationAdvice(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @ModelAttribute("unreadCount")
    public long populateUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return 0;
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return user != null ? notificationService.getUnreadCount(user.getId()) : 0;
    }


}