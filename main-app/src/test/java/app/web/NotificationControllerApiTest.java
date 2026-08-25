package app.web;

import org.app.Application;
import org.app.notification.service.NotificationService;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.NotificationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = NotificationController.class,
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class},
        properties = {
                "spring.mvc.view.prefix=/WEB-INF/views/",
                "spring.mvc.view.suffix=.html"
        }
)
@ContextConfiguration(classes = Application.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void showNotifications_shouldReturnNotificationsView() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");

        when(userService.getUserByUsernameOrEmail("testUser")).thenReturn(user);
        when(notificationService.getNotificationsForUser(userId)).thenReturn(Collections.emptyList());

        MockHttpServletRequestBuilder request = get("/notifications")
                .with(user("testUser"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"));

        verify(userService, atLeastOnce()).getUserByUsernameOrEmail("testUser");
        verify(notificationService, times(1)).getNotificationsForUser(userId);
    }
    @Test
    void markAsReadNotification_shouldRedirectToNotifications() throws Exception {
        UUID notifId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/notifications/" + notifId)
                .with(user("testUser"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService, times(1)).setReadNotification(notifId);
    }

    @Test
    void deleteNotification_shouldRedirectToNotifications() throws Exception {
        UUID notifId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/notifications/notification/" + notifId)
                .with(user("testUser"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(notificationService, times(1)).deleteNotification(eq(notifId), eq("testUser"));
    }

    @Test
    void deleteNotification_withNullId_shouldHandleNullId() {
        NotificationController controller = new NotificationController(userService, notificationService);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = controller.deleteNotification(null, null, redirectAttributes);

        assertEquals("redirect:/notifications", viewName);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMessage"));
        verify(notificationService, never()).deleteNotification(any(), any());
    }
}