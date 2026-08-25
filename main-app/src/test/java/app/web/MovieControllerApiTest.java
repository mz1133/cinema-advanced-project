package org.app.web;

import org.app.Application;
import org.app.actor.service.ActorService;
import org.app.movie.model.Movie;
import org.app.movie.service.MovieService;
import org.app.notification.service.NotificationService;
import org.app.reviewclient.ReviewClient;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.dto.CustomPageDto;
import org.app.web.dto.EditMovieDetails;
import org.app.web.dto.MovieOptionsDto;
import org.app.web.dto.ViewReviewsAndCommentsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
        controllers = MovieController.class,
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class},
        properties = {
                "spring.mvc.view.prefix=/WEB-INF/views/",
                "spring.mvc.view.suffix=.html"
        }
)
@ContextConfiguration(classes = Application.class)
public class MovieControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ActorService actorService;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private ReviewClient reviewClient;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAddMoviePage_shouldReturnAddMovieView() throws Exception {
        when(movieService.getMovieOptions()).thenReturn(new MovieOptionsDto());

        MockHttpServletRequestBuilder request = get("/movies/new")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-movies"))
                .andExpect(model().attributeExists("movieDto"))
                .andExpect(model().attributeExists("movieOptions"));

        verify(movieService, times(1)).getMovieOptions();
    }

    @Test
    void addMovie_withValidData_shouldRedirectToNewMovie() throws Exception {
        User user = new User();
        user.setUsername("admin");
        user.setRole(Role.ADMIN);

        when(movieService.getMovieOptions()).thenReturn(new MovieOptionsDto());
        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(user);

        MockHttpServletRequestBuilder request = post("/movies/new")
                .param("title", "Test Movie")
                .param("description", "Valid movie description text here")
                .param("trailerUrl", "https://youtube.com")
                .param("posterUrl", "https://image.com")
                .param("releaseYear", "2023")
                .param("studio", "Warner Bros")
                .param("duration", "120")
                .param("year", "2023")
                .param("genres", "ACTION")
                .param("countries", "USA")
                .param("actorsIds", UUID.randomUUID().toString())
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/new"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(movieService, times(1)).addMovie(any(), eq("admin"), eq(Role.ADMIN));
    }

    @Test
    void addMovie_withInvalidData_shouldReturnAddMovieView() throws Exception {
        when(movieService.getMovieOptions()).thenReturn(new MovieOptionsDto());

        MockHttpServletRequestBuilder request = post("/movies/new")
                .param("title", "")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-movies"))
                .andExpect(model().attributeExists("movieOptions"));

        verify(movieService, never()).addMovie(any(), any(), any());
    }

    @Test
    void getDetails_shouldReturnMovieDetailsView() throws Exception {
        UUID movieId = UUID.randomUUID();
        Movie movie = new Movie();
        CustomPageDto<ViewReviewsAndCommentsDto> reviewsPage = new CustomPageDto<>();
        reviewsPage.setContent(Collections.emptyList());

        when(movieService.getMovie(movieId)).thenReturn(movie);
        when(reviewClient.getReviewMovie(eq(movieId), anyInt(), anyInt())).thenReturn(reviewsPage);

        MockHttpServletRequestBuilder request = get("/movies/details/" + movieId)
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("movie-details"))
                .andExpect(model().attributeExists("movie"))
                .andExpect(model().attributeExists("review"))
                .andExpect(model().attributeExists("createReviewDto"))
                .andExpect(model().attributeExists("createCommentDto"));

        verify(movieService, times(1)).getMovie(movieId);
        verify(reviewClient, times(1)).getReviewMovie(eq(movieId), eq(0), eq(10));
    }

    @Test
    void getAddActorPage_shouldReturnAddActorView() throws Exception {
        MockHttpServletRequestBuilder request = get("/movies/actors")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-actors"))
                .andExpect(model().attributeExists("actor"));
    }

    @Test
    void addActor_withValidData_shouldRedirectToActors() throws Exception {
        MockHttpServletRequestBuilder request = post("/movies/actors")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("birthDate", "1990-01-01")
                .param("age", "34")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/actors"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(actorService, times(1)).addActor(any());
    }

    @Test
    void addActor_withInvalidData_shouldReturnAddActorView() throws Exception {
        MockHttpServletRequestBuilder request = post("/movies/actors")
                .param("name", "")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-actors"));

        verify(actorService, never()).addActor(any());
    }

    @Test
    void getMovieEditPage_shouldReturnEditMovieView() throws Exception {
        UUID movieId = UUID.randomUUID();
        when(movieService.getMovieEditDetails(movieId)).thenReturn(new EditMovieDetails());
        when(movieService.getMovieOptions()).thenReturn(new MovieOptionsDto());

        MockHttpServletRequestBuilder request = get("/movies/" + movieId + "/edit")
                .param("source", "admin")
                .with(user("admin"));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-movie"))
                .andExpect(model().attributeExists("movieDto"))
                .andExpect(model().attributeExists("movieId"))
                .andExpect(model().attributeExists("movieOptions"))
                .andExpect(model().attribute("source", "admin"));

        verify(movieService, times(1)).getMovieEditDetails(movieId);
        verify(movieService, times(1)).getMovieOptions();
    }

    @Test
    void editMovie_asAdminWithSourceAdmin_shouldRedirectToAdminMovies() throws Exception {
        UUID movieId = UUID.randomUUID();
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        Movie movie = new Movie();
        movie.setPublisher(admin);

        when(userService.getUserByUsernameOrEmail("admin")).thenReturn(admin);
        when(movieService.getMovie(movieId)).thenReturn(movie);

        MockHttpServletRequestBuilder request = post("/movies/" + movieId + "/edit")
                .param("title", "Updated Title")
                .param("description", "Updated description text here")
                .param("studio", "Warner Bros")
                .param("duration", "120")
                .param("year", "2023")
                .param("genres", "ACTION")
                .param("countries", "USA")
                .param("actorsIds", UUID.randomUUID().toString())
                .param("source", "admin")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(movieService, times(1)).editMovie(any(), eq(movieId));
    }

    @Test
    void editMovie_asPublisherWithSourceProfile_shouldRedirectToProfile() throws Exception {
        UUID movieId = UUID.randomUUID();
        User publisher = new User();
        publisher.setId(UUID.randomUUID());
        publisher.setUsername("publisher");
        publisher.setRole(Role.USER);

        Movie movie = new Movie();
        movie.setPublisher(publisher);

        when(userService.getUserByUsernameOrEmail("publisher")).thenReturn(publisher);
        when(movieService.getMovie(movieId)).thenReturn(movie);

        MockHttpServletRequestBuilder request = post("/movies/" + movieId + "/edit")
                .param("title", "Updated Title")
                .param("description", "Updated description text here")
                .param("studio", "Warner Bros")
                .param("duration", "120")
                .param("year", "2023")
                .param("genres", "ACTION")
                .param("countries", "USA")
                .param("actorsIds", UUID.randomUUID().toString())
                .param("source", "profile")
                .with(user("publisher"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home/my-profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(movieService, times(1)).editMovie(any(), eq(movieId));
    }

    @Test
    void editMovie_unauthorizedUser_shouldRedirectToError() throws Exception {
        UUID movieId = UUID.randomUUID();
        User normalUser = new User();
        normalUser.setId(UUID.randomUUID());
        normalUser.setUsername("user");
        normalUser.setRole(Role.USER);

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        Movie movie = new Movie();
        movie.setPublisher(otherUser);

        when(userService.getUserByUsernameOrEmail("user")).thenReturn(normalUser);
        when(movieService.getMovie(movieId)).thenReturn(movie);

        MockHttpServletRequestBuilder request = post("/movies/" + movieId + "/edit")
                .param("title", "Updated Title")
                .param("description", "Updated description text here")
                .param("studio", "Warner Bros")
                .param("duration", "120")
                .param("year", "2023")
                .param("genres", "ACTION")
                .param("countries", "USA")
                .param("actorsIds", UUID.randomUUID().toString())
                .param("source", "profile")
                .with(user("user"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));

        verify(movieService, never()).editMovie(any(), any());
    }

    @Test
    void editMovie_withInvalidData_shouldReturnEditMovieView() throws Exception {
        UUID movieId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/movies/" + movieId + "/edit")
                .param("title", "")
                .param("source", "admin")
                .with(user("admin"))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-movie"))
                .andExpect(model().attribute("source", "admin"));

        verify(movieService, never()).editMovie(any(), any());
    }

    @Test
    void getMovieEditPage_withNullMovieId_shouldRedirectToEditMovieDirectly() {
        MovieService mockMovieService = mock(MovieService.class);
        UserService mockUserService = mock(UserService.class);
        ActorService mockActorService = mock(ActorService.class);
        ReviewClient mockReviewClient = mock(ReviewClient.class);

        MovieController controller = new MovieController(mockUserService, mockActorService, mockMovieService, mockReviewClient);
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        ModelAndView modelAndView = controller.getMovieEditPage(null, redirectAttributes, "admin");

        assertEquals("redirect:/edit-movie", modelAndView.getViewName());
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMessage"));
        verify(mockMovieService, never()).getMovieEditDetails(any());
    }
}