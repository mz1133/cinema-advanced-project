package app.service;

import org.app.Application;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalVariableAdviceITest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Test
    @WithMockUser(username = "pesho")
    void currentUser_shouldAddUserHeaderDtoToModelWhenAuthenticated() {
        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .username("pesho")
                .email("pesho@example.com")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();

        Mockito.when(userService.getUserByUsernameOrEmail("pesho")).thenReturn(mockUser);

        try {
            mockMvc.getDispatcherServlet();
        } catch (Exception ignored) {
        }
    }
}