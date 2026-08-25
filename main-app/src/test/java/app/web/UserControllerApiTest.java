package app.web;

import org.app.Application;
import org.app.advice.GlobalVariableAdvice;
import org.app.config.SubscriptionProperties;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.service.MovieService;
import org.app.notification.service.NotificationService;
import org.app.subscription.service.SubscriptionService;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.UserController;
import org.app.web.dto.PurchaseSubscriptionDto;
import org.app.web.dto.UpdateProfileRequest;
import org.app.web.dto.UserHeaderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = ThymeleafAutoConfiguration.class
)
@Import({GlobalVariableAdvice.class, UserControllerApiTest.MockMvcViewConfig.class})
@ContextConfiguration(classes = Application.class)
class UserControllerApiTest {

    @TestConfiguration
    static class MockMvcViewConfig {
        @Bean
        ViewResolver mockMvcViewResolver() {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix("/templates/");
            resolver.setSuffix(".html");
            return resolver;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private UserService userService;
    @MockitoBean private SubscriptionService subscriptionService;
    @MockitoBean private SubscriptionProperties subscriptionProperties;
    @MockitoBean private MovieService movieService;
    @MockitoBean private NotificationService notificationService;

    private User loggedUser;

    @BeforeEach
    void setUp() {
        loggedUser = User.builder()
                .id(UUID.randomUUID())
                .username("api_user")
                .email("api_user@example.com")
                .build();

        when(userService.getUserByUsernameOrEmail("api_user")).thenReturn(loggedUser);
        when(userService.getUserHeaderDto(loggedUser.getId())).thenReturn(new UserHeaderDto());
        when(userService.getUserByUsername("api_user")).thenReturn(loggedUser);

        when(movieService.search(
                nullable(String.class), nullable(Integer.class),
                nullable(Genre.class), nullable(Country.class),
                nullable(String.class), any(Pageable.class)
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        when(movieService.getMoviesByPublisher(eq(loggedUser.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(userService.getCurrentProfileData(loggedUser.getId()))
                .thenReturn(new UpdateProfileRequest());
        when(subscriptionProperties.getPlans()).thenReturn(List.of());
    }

    @Test
    void getHomePageShouldReturnHomeView() throws Exception {
        mockMvc.perform(get("/home").with(user("api_user"))
                        .param("keyword", "Matrix").param("sort", "title"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists(
                        "currentUser", "movies", "genres", "releaseYears", "countries"));

        verify(movieService).search(eq("Matrix"), eq(null), eq(null), eq(null),
                eq("title"), any(Pageable.class));
    }

    @Test
    void getMyProfileShouldReturnMyProfileView() throws Exception {
        mockMvc.perform(get("/home/my-profile").with(user("api_user")))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeExists("currentUser", "updateProfileRequest", "movies"));

        verify(userService).getUserByUsername("api_user");
        verify(movieService).getMoviesByPublisher(eq(loggedUser.getId()), any(Pageable.class));
        verify(userService).getCurrentProfileData(loggedUser.getId());
    }

    @Test
    void updateMyProfileWithValidDataShouldRedirect() throws Exception {
        mockMvc.perform(post("/home/my-profile/update")
                        .with(user("api_user")).with(csrf())
                        .param("pictureUrl", "https://example.com/profile.jpg")
                        .param("firstName", "Ivan")
                        .param("lastName", "Ivanov")
                        .param("email", "ivan@example.com")
                        .param("phoneNumber", "0888123456")
                        .param("birthDate", "1995-05-12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/my-profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).updateUserProfile(eq("api_user"), any(UpdateProfileRequest.class));
    }

    @Test
    void updateMyProfileWithInvalidDataShouldReturnMyProfileView() throws Exception {
        mockMvc.perform(post("/home/my-profile/update")
                        .with(user("api_user")).with(csrf())
                        .param("pictureUrl", "invalid-url")
                        .param("phoneNumber", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-profile"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeHasFieldErrors(
                        "updateProfileRequest", "pictureUrl", "phoneNumber"));

        verify(userService, never()).updateUserProfile(any(), any(UpdateProfileRequest.class));
    }

    @Test
    void getSubscriptionsShouldReturnSubscriptionsView() throws Exception {
        mockMvc.perform(get("/home/subscriptions").with(user("api_user")))
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeExists("currentUser", "plans", "purchaseSubscriptionDto"));
    }

    @Test
    void makePurchaseWithValidDataShouldRedirect() throws Exception {
        mockMvc.perform(post("/home/subscriptions/purchase")
                        .with(user("api_user")).with(csrf())
                        .param("planCode", "STANDARD-30")
                        .param("cardNumber", "1234567812345678")
                        .param("expiry", "12/28")
                        .param("cvv", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/subscriptions"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(subscriptionService).addPlan(any(PurchaseSubscriptionDto.class), eq("api_user"));
    }

    @Test
    void makePurchaseWithInvalidDataShouldReturnSubscriptionsView() throws Exception {
        mockMvc.perform(post("/home/subscriptions/purchase")
                        .with(user("api_user")).with(csrf())
                        .param("planCode", "")
                        .param("cardNumber", "123")
                        .param("expiry", "15/28")
                        .param("cvv", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeExists("currentUser", "plans", "hasErrors"))
                .andExpect(model().attributeHasFieldErrors(
                        "purchaseSubscriptionDto", "planCode", "cardNumber", "expiry", "cvv"));

        verify(subscriptionService, never()).addPlan(any(PurchaseSubscriptionDto.class), any());
    }

    @Test
    void getMyReviewsShouldReturnMyReviewsView() throws Exception {
        mockMvc.perform(get("/home/my-reviews").with(user("api_user")))
                .andExpect(status().isOk())
                .andExpect(view().name("my-reviews"))
                .andExpect(model().attributeExists("currentUser"));
    }
}