package app.web;

import org.app.Application;
import org.app.movie.model.Movie;
import org.app.movie.service.MovieService;
import org.app.notification.service.NotificationService;
import org.app.reviewclient.ReviewClient;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.ReviewController;
import org.app.web.dto.AdminReviewDto;
import org.app.web.dto.CreateCommentDto;
import org.app.web.dto.CreateReviewDto;
import org.app.web.dto.CustomPageDto;
import org.app.web.dto.ViewReviewsAndCommentsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.ok;
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
        controllers = ReviewController.class,
        excludeAutoConfiguration = ThymeleafAutoConfiguration.class
)
@Import(ReviewControllerApiTest.MockMvcViewConfig.class)
@ContextConfiguration(classes = Application.class)
class ReviewControllerApiTest {

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

    @MockitoBean private ReviewClient reviewClient;
    @MockitoBean private UserService userService;
    @MockitoBean private MovieService movieService;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private org.springframework.context.ApplicationEventPublisher publisher;

    @Autowired
    private MockMvc mockMvc;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        defaultUser = new User();
        defaultUser.setId(UUID.randomUUID());
        defaultUser.setUsername("testUser");
        defaultUser.setPictureUrl("default.jpg");

        lenient().when(userService.getUserByUsername(anyString())).thenReturn(defaultUser);
        lenient().when(userService.getUserByUsernameOrEmail(anyString())).thenReturn(defaultUser);
        lenient().when(userService.getUserById(any(UUID.class))).thenReturn(defaultUser);
    }

    @Test
    void createReview_withValidData_shouldRedirectToMovieDetails() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Test Movie");

        when(userService.getUserByUsername("testUser")).thenReturn(user);
        when(movieService.getMovie(movieId)).thenReturn(movie);
        when(reviewClient.createReview(any())).thenReturn(ok().build());

        mockMvc.perform(post("/review/" + movieId)
                        .param("movieId", movieId.toString())
                        .param("content", "This is an excellent movie review!")
                        .param("userRating", "5")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/details/" + movieId + "#reviews-list"));

        ArgumentCaptor<CreateReviewDto> captor = ArgumentCaptor.forClass(CreateReviewDto.class);
        verify(reviewClient).createReview(captor.capture());
        CreateReviewDto saved = captor.getValue();
        assertEquals(userId, saved.getPublisherId());
        assertEquals("testUser", saved.getPublisherUsername());
        assertEquals(movieId, saved.getMovieId());
        assertEquals("Test Movie", saved.getMovieTitle());
        assertFalse(saved.isDeleted());
    }

    @Test
    void createReview_withInvalidData_shouldReturnMovieDetailsView() throws Exception {
        UUID movieId = UUID.randomUUID();
        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Test Movie");

        CustomPageDto<ViewReviewsAndCommentsDto> pageDto = new CustomPageDto<>();
        pageDto.setContent(Collections.emptyList());

        when(movieService.getMovie(movieId)).thenReturn(movie);
        when(reviewClient.getReviewMovie(eq(movieId), eq(0), eq(10))).thenReturn(pageDto);

        mockMvc.perform(post("/review/" + movieId)
                        .param("movieId", movieId.toString())
                        .param("content", "")
                        .param("userRating", "5")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("movie-details"))
                .andExpect(model().attributeExists("movie", "review", "createCommentDto"));

        verify(reviewClient, never()).createReview(any());
    }

    @Test
    void createComment_withValidData_shouldRedirectToMovieDetails() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        when(userService.getUserByUsername("testUser")).thenReturn(user);
        when(reviewClient.createComment(any())).thenReturn(ok().build());

        mockMvc.perform(post("/review/" + reviewId + "/comments")
                        .param("movieId", movieId.toString())
                        .param("reviewId", reviewId.toString())
                        .param("content", "Great point about the ending.")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/details/" + movieId + "#comments-" + reviewId));

        ArgumentCaptor<CreateCommentDto> captor = ArgumentCaptor.forClass(CreateCommentDto.class);
        verify(reviewClient).createComment(captor.capture());
        CreateCommentDto saved = captor.getValue();
        assertEquals(userId, saved.getPublisherId());
        assertEquals("testUser", saved.getPublisherUsername());
        assertEquals(reviewId, saved.getReviewId());
    }

    @Test
    void createComment_withInvalidData_shouldReturnMovieDetailsView() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("Test Movie");

        CustomPageDto<ViewReviewsAndCommentsDto> reviewsPage = new CustomPageDto<>();
        reviewsPage.setContent(Collections.emptyList());

        when(movieService.getMovie(movieId)).thenReturn(movie);
        when(reviewClient.getReviewMovie(eq(movieId), eq(0), eq(10))).thenReturn(reviewsPage);

        mockMvc.perform(post("/review/" + reviewId + "/comments")
                        .param("movieId", movieId.toString())
                        .param("content", "")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("movie-details"))
                .andExpect(model().attributeExists("movie", "review", "createReviewDto"));

        verify(reviewClient, never()).createComment(any());
    }

    @Test
    void getMyReviews_shouldReturnMyReviewsView() throws Exception {
        CustomPageDto<AdminReviewDto> pageData = new CustomPageDto<>();
        pageData.setContent(Collections.emptyList());

        when(reviewClient.getUserReviews(
                eq(defaultUser.getId()), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(pageData);

        mockMvc.perform(get("/review/my-reviews").with(user("testUser")))
                .andExpect(status().isOk())
                .andExpect(view().name("my-reviews"))
                .andExpect(model().attributeExists("pageData", "pageSize", "reviewEditDto"));

        verify(reviewClient).getUserReviews(
                eq(defaultUser.getId()), isNull(), isNull(), isNull(), eq(0), eq(10)
        );
    }

    @Test
    void getMyReviews_withFilters_shouldPassThemToClient() throws Exception {
        UUID movieId = UUID.randomUUID();
        CustomPageDto<AdminReviewDto> pageData = new CustomPageDto<>();
        pageData.setContent(Collections.emptyList());

        when(reviewClient.getUserReviews(
                eq(defaultUser.getId()), eq("matrix"), eq(movieId), eq("Matrix"), eq(1), eq(5)
        )).thenReturn(pageData);

        mockMvc.perform(get("/review/my-reviews")
                        .param("keyword", "matrix")
                        .param("movieId", movieId.toString())
                        .param("movieTitle", "Matrix")
                        .param("page", "1")
                        .param("size", "5")
                        .with(user("testUser")))
                .andExpect(status().isOk())
                .andExpect(view().name("my-reviews"))
                .andExpect(model().attribute("keyword", "matrix"))
                .andExpect(model().attribute("movieId", movieId))
                .andExpect(model().attribute("movieTitle", "Matrix"))
                .andExpect(model().attribute("pageSize", 5));
    }

    @Test
    void deleteMyReview_shouldRedirectToMyReviews() throws Exception {
        UUID reviewId = UUID.randomUUID();
        when(userService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(post("/review/my-reviews/" + reviewId)
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/review/my-reviews"))
                .andExpect(flash().attribute("successMessage", "Review deleted successfully!"));

        verify(reviewClient).deleteReview(reviewId, false);
    }

    @Test
    void deleteMyReview_asAdmin_shouldCallClientWithAdminFlag() throws Exception {
        UUID reviewId = UUID.randomUUID();
        when(userService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(post("/review/my-reviews/" + reviewId)
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/review/my-reviews"));

        verify(reviewClient).deleteReview(reviewId, true);
    }

    @Test
    void restoreMyReview_shouldRedirectToMyReviews() throws Exception {
        UUID reviewId = UUID.randomUUID();
        doNothing().when(reviewClient).restoreReview(reviewId);

        mockMvc.perform(post("/review/my-reviews/" + reviewId + "/restore")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/review/my-reviews"))
                .andExpect(flash().attribute("successMessage", "Review restore successfully!"));

        verify(reviewClient).restoreReview(reviewId);
    }

    @Test
    void updateReview_withValidData_shouldRedirectToMyReviews() throws Exception {
        UUID reviewId = UUID.randomUUID();
        doNothing().when(reviewClient).updateReview(eq(reviewId), any());

        mockMvc.perform(post("/review/my-reviews/edit/" + reviewId)
                        .param("content", "This is my updated movie review content!")
                        .param("rating", "8")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/review/my-reviews"))
                .andExpect(flash().attribute("successMessage", "Review successfully updated!"));

        verify(reviewClient).updateReview(eq(reviewId), any());
    }

    @Test
    void updateReview_withInvalidData_shouldReturnMyReviewsView() throws Exception {
        UUID reviewId = UUID.randomUUID();
        CustomPageDto<AdminReviewDto> pageData = new CustomPageDto<>();
        pageData.setContent(Collections.emptyList());

        when(reviewClient.getUserReviews(
                eq(defaultUser.getId()), isNull(), isNull(), isNull(), eq(0), eq(10)
        )).thenReturn(pageData);

        mockMvc.perform(post("/review/my-reviews/edit/" + reviewId)
                        .param("content", "")
                        .param("rating", "8")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("my-reviews"))
                .andExpect(model().attributeExists("pageData", "failedReviewId"))
                .andExpect(model().attribute("failedReviewId", reviewId));

        verify(reviewClient, never()).updateReview(any(), any());
    }

    @Test
    void deleteReview_asAdmin_shouldDeletePublishEventAndRedirect() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        when(userService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(post("/review/review/" + reviewId)
                        .param("reason", "This review violates our community rules guidelines")
                        .param("publisherId", publisherId.toString())
                        .param("reviewId", reviewId.toString())
                        .with(user("admin"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews"))
                .andExpect(flash().attribute("success", "Review was successfully deleted!"));

        verify(reviewClient).deleteReview(reviewId, true);
        verify(userService).getUserById(publisherId);

    }

    @Test
    void deleteReview_asNonAdmin_shouldRedirectToHome() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        when(userService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(post("/review/review/" + reviewId)
                        .param("reason", "This review violates our community rules guidelines")
                        .param("publisherId", publisherId.toString())
                        .param("reviewId", reviewId.toString())
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(reviewClient, never()).deleteReview(any(), anyBoolean());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void deleteReview_withInvalidData_shouldReturnManageReviewsView() throws Exception {
        CustomPageDto<AdminReviewDto> reviews = new CustomPageDto<>();
        reviews.setContent(Collections.emptyList());
        when(reviewClient.getAllReviewsAndComments(isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(reviews);

        mockMvc.perform(post("/review/review/" + UUID.randomUUID())
                        .param("reason", "short")
                        .with(user("admin"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manage-reviews"))
                .andExpect(model().attributeExists("pageData", "pageSize", "deleteCommentDto"));

        verify(reviewClient, never()).deleteReview(any(UUID.class), anyBoolean());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void deleteComment_asAdmin_shouldDeletePublishEventAndRedirect() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        when(userService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(post("/review/comments/delete")
                        .param("commentId", commentId.toString())
                        .param("reviewId", reviewId.toString())
                        .param("publisherId", publisherId.toString())
                        .param("reason", "This comment violates our community rules guidelines")
                        .with(user("admin"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews"))
                .andExpect(flash().attribute("success", "Comment was successfully deleted!"));

        verify(reviewClient).deleteComment(any());
        verify(userService).getUserById(publisherId);

    }

    @Test
    void deleteComment_asNonAdmin_shouldRedirectToHome() throws Exception {
        when(userService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(post("/review/comments/delete")
                        .param("commentId", UUID.randomUUID().toString())
                        .param("reviewId", UUID.randomUUID().toString())
                        .param("publisherId", UUID.randomUUID().toString())
                        .param("reason", "This comment violates our community rules guidelines")
                        .with(user("testUser"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(reviewClient, never()).deleteComment(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void deleteComment_withInvalidData_shouldReturnManageReviewsView() throws Exception {
        CustomPageDto<AdminReviewDto> reviews = new CustomPageDto<>();
        reviews.setContent(Collections.emptyList());
        when(reviewClient.getAllReviewsAndComments(isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(reviews);

        mockMvc.perform(post("/review/comments/delete")
                        .param("reason", "short")
                        .with(user("admin"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manage-reviews"))
                .andExpect(model().attributeExists("pageData", "pageSize", "reviewDeleteNotification"));

        verify(reviewClient, never()).deleteComment(any());
        verify(publisher, never()).publishEvent(any());
    }
}