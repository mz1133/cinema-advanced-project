package app.web;

import org.app.Application;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.app.movie.service.MovieService;
import org.app.notification.service.NotificationService;
import org.app.user.service.UserService;
import org.app.web.IndexController;
import org.app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = IndexController.class,
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class},
        properties = {
                "spring.mvc.view.prefix=/WEB-INF/views/",
                "spring.mvc.view.suffix=.html"
        }
)
@ContextConfiguration(classes = Application.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIndexPage_withoutParams_shouldReturnIndexView() throws Exception {
        Page<Movie> moviesPage = new PageImpl<>(Collections.emptyList());
        when(movieService.search(isNull(), isNull(), isNull(), isNull(), isNull(), any())).thenReturn(moviesPage);

        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("movies"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attributeExists("releaseYears"))
                .andExpect(model().attributeExists("countries"));

        verify(movieService, times(1)).search(isNull(), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void getIndexPage_withParams_shouldReturnIndexView() throws Exception {
        Page<Movie> moviesPage = new PageImpl<>(Collections.emptyList());
        when(movieService.search(eq("Action"), eq(2023), eq(Genre.ACTION), eq(Country.USA), eq("sort"), any()))
                .thenReturn(moviesPage);

        MockHttpServletRequestBuilder request = get("/")
                .param("keyword", "Action")
                .param("year", "2023")
                .param("genre", "ACTION")
                .param("country", "USA")
                .param("sort", "sort");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("movies"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attributeExists("releaseYears"))
                .andExpect(model().attributeExists("countries"));

        verify(movieService, times(1)).search(eq("Action"), eq(2023), eq(Genre.ACTION), eq(Country.USA), eq("sort"), any());
    }

    @Test
    void getRegisterPage_shouldReturnRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = get("/register/form");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void registerNewUser_withValidData_shouldRedirectToLogin() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("username", "testUser")
                .param("email", "test@test.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("country", "BULGARIA")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void registerNewUser_withInvalidData_shouldReturnRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .param("username", "")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(userService, never()).register(any());
    }

    @Test
    void getLoginPage_withoutError_shouldReturnLoginView() throws Exception {
        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    void getLoginPage_withError_shouldReturnLoginViewWithErrorMsg() throws Exception {
        MockHttpServletRequestBuilder request = get("/login")
                .param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Invalid username or password!"));
    }

    @Test
    void getAboutPage_shouldReturnAboutView() throws Exception {
        MockHttpServletRequestBuilder request = get("/about");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }
}