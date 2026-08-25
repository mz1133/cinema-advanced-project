package app.web;

import org.app.Application;
import org.app.movie.model.Movie;
import org.app.movie.service.MovieService;
import org.app.notification.service.NotificationService;
import org.app.reviewclient.ReviewClient;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.AdminController;
import org.app.web.dto.AdminReviewDto;
import org.app.web.dto.CustomPageDto;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminController.class,
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@ContextConfiguration(classes = Application.class)
public class AdminControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private ReviewClient reviewClient;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getManageUserPage_withAdminRole_shouldReturnManageUsersView() throws Exception {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        Page<User> usersPage = new PageImpl<>(List.of(adminUser));

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);
        when(userService.getAllUsersPageable(any(), eq(adminUser.getId()))).thenReturn(usersPage);

        MockHttpServletRequestBuilder request = get("/admin/users")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("manage-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("roles"));

        verify(userService, atLeastOnce()).getUserByUsernameOrEmail("admin");
        verify(userService, times(1)).getAllUsersPageable(any(), eq(adminUser.getId()));
    }

    @Test
    void getManageUserPage_withUserRole_shouldRedirectToHome() throws Exception {
        User normalUser = new User();
        normalUser.setId(UUID.randomUUID());
        normalUser.setUsername("user");
        normalUser.setRole(Role.USER);

        when(userService.getUserByUsernameOrEmail("user")).thenReturn(normalUser);

        MockHttpServletRequestBuilder request = get("/admin/users")
                .with(user("user"));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, never()).getAllUsersPageable(any(), any());
    }

    @Test
    void getManageUserPage_withKeyword_shouldReturnManageUsersView() throws Exception {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        Page<User> usersPage = Page.empty();

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);
        when(userService.getUserByKeyWord(eq("test"), any(), eq(adminUser.getId()))).thenReturn(usersPage);

        MockHttpServletRequestBuilder request = get("/admin/users")
                .param("keyword", "test")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("manage-users"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("keyword", "test"));

        verify(userService, times(1)).getUserByKeyWord(eq("test"), any(), eq(adminUser.getId()));
    }

    @Test
    void getManageMoviesPage_withAdminRole_shouldReturnManageMoviesView() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        Page<Movie> moviesPage = Page.empty();

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);
        when(movieService.getAllMoviesPageable(any())).thenReturn(moviesPage);

        MockHttpServletRequestBuilder request = get("/admin/movies")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("manage-movies"))
                .andExpect(model().attributeExists("movies"))
                .andExpect(model().attributeExists("message"));

        verify(movieService, times(1)).getAllMoviesPageable(any());
    }

    @Test
    void getManageMoviesPage_withUserRole_shouldRedirectToHome() throws Exception {
        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setRole(Role.USER);

        when(userService.getUserByUsernameOrEmail("user")).thenReturn(normalUser);

        MockHttpServletRequestBuilder request = get("/admin/movies")
                .with(user("user"));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(movieService, never()).getAllMoviesPageable(any());
    }

    @Test
    void changeUserRole_shouldRedirectToAdminUsers() throws Exception {
        UUID userId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/admin/users/role")
                .param("role", "ADMIN")
                .param("userToChangeRoleId", userId.toString())
                .param("keyword", "search")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?keyword=search"))
                .andExpect(flash().attributeExists("successChangeRole"));

        verify(userService, times(1)).changeUserRole(eq("admin"), eq(Role.ADMIN), eq(userId));
    }

    @Test
    void changeUserRole_withoutUserId_shouldRedirectToAdminUsers() throws Exception {
        MockHttpServletRequestBuilder request = post("/admin/users/role")
                .param("role", "ADMIN")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, never()).changeUserRole(any(), any(), any());
    }

    @Test
    void makeUserInactive_shouldRedirectToAdminUsers() throws Exception {
        UUID userId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setUsername("targetUser");

        when(userService.getUserById(userId)).thenReturn(targetUser);

        MockHttpServletRequestBuilder request = patch("/admin/users/active")
                .param("userId", userId.toString())
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successChangeUserStatus"));

        verify(userService, times(1)).changeUserActivationStatus(eq("admin"), eq(userId));
    }

    @Test
    void deleteMovie_withAdminRole_shouldRedirectToAdminMovies() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        UUID movieId = UUID.randomUUID();

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);

        MockHttpServletRequestBuilder request = delete("/admin/movies")
                .param("movieIdToDelete", movieId.toString())
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(movieService, times(1)).deleteMovie(eq(movieId));
    }

    @Test
    void restoreMovie_withAdminRole_shouldRedirectToAdminMovies() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        UUID movieId = UUID.randomUUID();

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);

        MockHttpServletRequestBuilder request = patch("/admin/movies")
                .param("movieIdToRestore", movieId.toString())
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(movieService, times(1)).restoreMovie(eq(movieId));
    }

    @Test
    void getAllReviewsAndComments_shouldReturnManageReviewsView() throws Exception {
        CustomPageDto<AdminReviewDto> pageDto = new CustomPageDto<>();
        pageDto.setContent(Collections.emptyList());

        when(reviewClient.getAllReviewsAndComments(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageDto);

        MockHttpServletRequestBuilder request = get("/admin/reviews")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("manage-reviews"))
                .andExpect(model().attributeExists("pageData"))
                .andExpect(model().attributeExists("reviewDeleteNotification"))
                .andExpect(model().attributeExists("deleteCommentDto"));

        verify(reviewClient, times(1)).getAllReviewsAndComments(any(), any(), any(), any(), anyInt(), anyInt());
    }
    @Test
    void getManageUserPage_withValidKeywordAndResults_shouldReturnUsers() throws Exception {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        Page<User> usersPage = new PageImpl<>(List.of(new User()));

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);
        when(userService.getUserByKeyWord(eq("john"), any(), eq(adminUser.getId()))).thenReturn(usersPage);

        MockHttpServletRequestBuilder request = get("/admin/users")
                .param("keyword", "john")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("manage-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeDoesNotExist("message"));
    }

    @Test
    void getManageMoviesPage_withKeywordAndSearchType_shouldCallGetMovieByKeyword() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        Page<Movie> moviesPage = new PageImpl<>(List.of(new Movie()));

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);
        when(movieService.getMovieByKeyword(eq("Matrix"), any(), eq("title"))).thenReturn(moviesPage);

        MockHttpServletRequestBuilder request = get("/admin/movies")
                .param("keyword", "Matrix")
                .param("searchType", "title")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("manage-movies"))
                .andExpect(model().attributeExists("movies"));

        verify(movieService, times(1)).getMovieByKeyword(eq("Matrix"), any(), eq("title"));
        verify(movieService, never()).getAllMoviesPageable(any());
    }

    @Test
    void getManageMoviesPage_withKeywordButNoSearchType_shouldCallGetAllMoviesPageable() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);
        when(movieService.getAllMoviesPageable(any())).thenReturn(Page.empty());

        MockHttpServletRequestBuilder request = get("/admin/movies")
                .param("keyword", "Matrix")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("manage-movies"));

        verify(movieService, times(1)).getAllMoviesPageable(any());
    }

    @Test
    void changeUserRole_withoutKeyword_shouldRedirectWithoutKeywordParam() throws Exception {
        UUID userId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/admin/users/role")
                .param("role", "ADMIN")
                .param("userToChangeRoleId", userId.toString())
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successChangeRole"));
    }

    @Test
    void makeUserInactive_withoutUserId_shouldRedirectToAdminUsers() throws Exception {
        MockHttpServletRequestBuilder request = patch("/admin/users/active")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, never()).changeUserActivationStatus(any(), any());
    }

    @Test
    void deleteMovie_withUserRole_shouldRedirectToHome() throws Exception {
        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setRole(Role.USER);

        when(userService.getUserByUsernameOrEmail("user")).thenReturn(normalUser);

        MockHttpServletRequestBuilder request = delete("/admin/movies")
                .param("movieIdToDelete", UUID.randomUUID().toString())
                .with(user("user"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(movieService, never()).deleteMovie(any());
    }

    @Test
    void deleteMovie_withoutMovieId_shouldRedirectToErrorPage() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);

        MockHttpServletRequestBuilder request = delete("/admin/movies")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));

        verify(movieService, never()).deleteMovie(any());
    }

    @Test
    void restoreMovie_withUserRole_shouldRedirectToHome() throws Exception {
        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setRole(Role.USER);

        when(userService.getUserByUsernameOrEmail("user")).thenReturn(normalUser);

        MockHttpServletRequestBuilder request = patch("/admin/movies")
                .param("movieIdToRestore", UUID.randomUUID().toString())
                .with(user("user"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(movieService, never()).restoreMovie(any());
    }

    @Test
    void restoreMovie_withoutMovieId_shouldRedirectToErrorPage() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(adminUser);

        MockHttpServletRequestBuilder request = patch("/admin/movies")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));

        verify(movieService, never()).restoreMovie(any());
    }
}