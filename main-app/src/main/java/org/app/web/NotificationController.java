package org.app.web;

import org.app.notification.service.NotificationService;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final UserService userService;
    private final NotificationService notificationService;

    public NotificationController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView showNotifications(Principal principal) {

        ModelAndView modelAndView = new ModelAndView("notifications");

        User user = userService.getUserByUsernameOrEmail(principal.getName());
        modelAndView.addObject("notifications",
                notificationService.getNotificationsForUser(user.getId()));

        return modelAndView;
    }

    @PostMapping("/{id}")
    public String markAsReadNotification(@PathVariable UUID id) {

        notificationService.setReadNotification(id);

        return "redirect:/notifications";
    }

    @PostMapping("/notification/{id}")
    public String deleteNotification(@PathVariable UUID id,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {

        if(id == null) {
            redirectAttributes
                    .addFlashAttribute
                            ("errorMessage", "Notification not found!");
            return "redirect:/notifications";
        }

        notificationService.deleteNotification(id, principal.getName());
        redirectAttributes
                .addFlashAttribute
                        ("successMessage", "Notification deleted successfully!");

        return "redirect:/notifications";
    }





}
