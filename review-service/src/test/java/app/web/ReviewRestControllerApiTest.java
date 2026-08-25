package app.web;

import app.review.service.ReviewService;
import app.web.dto.AdminReviewDto;
import app.web.dto.CreateReviewDto;
import app.web.dto.EditReviewDto;
import app.web.dto.ViewReviewsAndCommentsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;


import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewRestController.class)
public class ReviewRestControllerApiTest {

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postRequestToCreateReview_happyPath() throws Exception {

        UUID movieId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "movieId": "%s",
                            "content": "This is a great movie and I really enjoyed it!",
                            "userRating": 5
                        }
                        """.formatted(movieId))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        verify(reviewService, times(1))
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void postRequestToCreateReview_withContentTooShort_returnsBadRequest() throws Exception {

        UUID movieId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "movieId": "%s",
                            "content": "Too short",
                            "userRating": 5
                        }
                        """.formatted(movieId))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void postRequestToCreateReview_withEmptyContent_returnsBadRequest() throws Exception {

        UUID movieId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "movieId": "%s",
                            "content": "",
                            "userRating": 5
                        }
                        """.formatted(movieId))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void postRequestToCreateReview_withNullMovieId_returnsBadRequest() throws Exception {

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "movieId": null,
                            "content": "This is a valid review with enough content!",
                            "userRating": 5
                        }
                        """)
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void postRequestToCreateReview_withNullRating_returnsBadRequest() throws Exception {

        UUID movieId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "movieId": "%s",
                            "content": "This is a valid review with enough content!",
                            "userRating": null
                        }
                        """.formatted(movieId))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void postRequestToCreateReview_withRatingBelowMinimum_returnsBadRequest() throws Exception {

        UUID movieId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "movieId": "%s",
                            "content": "This is a valid review with enough content!",
                            "userRating": 0
                        }
                        """.formatted(movieId))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void postRequestToCreateReview_withRatingAboveMaximum_returnsBadRequest() throws Exception {

        UUID movieId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "movieId": "%s",
                            "content": "This is a valid review with enough content!",
                            "userRating": 11
                        }
                        """.formatted(movieId))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void postRequestToCreateReview_withoutRequestBody_returnsBadRequest() throws Exception {

        MockHttpServletRequestBuilder request = post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .createReview(any(CreateReviewDto.class));
    }

    @Test
    void deleteRequestToReview_happyPath() throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = delete("/api/reviews/{id}", reviewId)
                .param("isAdmin", "true")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(reviewService, times(1))
                .deleteReview(reviewId, true);
    }

    @Test
    void deleteRequestToReview_whenUserIsNotAdmin_returnsOk() throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = delete("/api/reviews/{id}", reviewId)
                .param("isAdmin", "false")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(reviewService, times(1))
                .deleteReview(reviewId, false);
    }

    @Test
    void deleteRequestToReview_withIsAdminTrue_returnsOk() throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                delete("/api/reviews/{id}", reviewId)
                        .param("isAdmin", "true")
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(reviewService)
                .deleteReview(reviewId, true);
    }

    @Test
    void getRequestToReviewsByMovie_returnsReviewsPage() throws Exception {

        UUID movieId = UUID.randomUUID();

        ViewReviewsAndCommentsDto review = new ViewReviewsAndCommentsDto();

        Page<ViewReviewsAndCommentsDto> page = new PageImpl<>(
                List.of(review),
                PageRequest.of(0, 10),
                1
        );

        when(reviewService.getAllActivesReviewsAndCommentsByMovieId(
                eq(movieId),
                any(Pageable.class)
        )).thenReturn(page);

        MockHttpServletRequestBuilder request =
                get("/api/reviews/movies/{id}", movieId);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(reviewService, times(1))
                .getAllActivesReviewsAndCommentsByMovieId(
                        eq(movieId),
                        any(Pageable.class)
                );
    }

    @Test
    void getRequestToReviewsByMovie_withPagination_returnsCorrectPage() throws Exception {

        UUID movieId = UUID.randomUUID();

        Page<ViewReviewsAndCommentsDto> page = new PageImpl<>(
                List.of(),
                PageRequest.of(2, 5),
                15
        );

        when(reviewService.getAllActivesReviewsAndCommentsByMovieId(
                eq(movieId),
                any(Pageable.class)
        )).thenReturn(page);

        MockHttpServletRequestBuilder request =
                get("/api/reviews/movies/{id}", movieId)
                        .param("page", "2")
                        .param("size", "5");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalElements").value(15));

        verify(reviewService, times(1))
                .getAllActivesReviewsAndCommentsByMovieId(
                        eq(movieId),
                        any(Pageable.class)
                );
    }

    @Test
    void getRequestToAllReviews_withFilters_returnsReviewsPage() throws Exception {

        UUID movieId = UUID.randomUUID();

        AdminReviewDto review = new AdminReviewDto();

        Page<AdminReviewDto> page = new PageImpl<>(
                List.of(review),
                PageRequest.of(0, 10),
                1
        );

        when(reviewService.getAllReviewsAndComments(
                eq("batman"),
                eq(movieId),
                eq("Vik123"),
                eq("Batman"),
                any(Pageable.class)
        )).thenReturn(page);

        MockHttpServletRequestBuilder request =
                get("/api/reviews/reviews")
                        .param("keyword", "batman")
                        .param("movieId", movieId.toString())
                        .param("publisherUsername", "Vik123")
                        .param("movieTitle", "Batman");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(reviewService, times(1))
                .getAllReviewsAndComments(
                        eq("batman"),
                        eq(movieId),
                        eq("Vik123"),
                        eq("Batman"),
                        any(Pageable.class)
                );
    }

    @Test
    void getRequestToAllReviews_withoutFilters_returnsEmptyPage() throws Exception {

        Page<AdminReviewDto> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(reviewService.getAllReviewsAndComments(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        MockHttpServletRequestBuilder request =
                get("/api/reviews/reviews");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(reviewService, times(1))
                .getAllReviewsAndComments(
                        isNull(),
                        isNull(),
                        isNull(),
                        isNull(),
                        any(Pageable.class)
                );
    }

    @Test
    void getRequestToUserReviews_returnsReviewsPage() throws Exception {

        UUID userId = UUID.randomUUID();

        AdminReviewDto review = new AdminReviewDto();

        Page<AdminReviewDto> page = new PageImpl<>(
                List.of(review),
                PageRequest.of(1, 5),
                12
        );

        when(reviewService.getFilteredReviewsByUserId(
                eq(userId),
                eq("batman"),
                isNull(),
                eq("Batman"),
                any(Pageable.class)
        )).thenReturn(page);

        MockHttpServletRequestBuilder request =
                get("/api/reviews/user/{userId}", userId)
                        .param("keyword", "batman")
                        .param("movieTitle", "Batman")
                        .param("page", "1")
                        .param("size", "5");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalElements").value(12));

        verify(reviewService, times(1))
                .getFilteredReviewsByUserId(
                        eq(userId),
                        eq("batman"),
                        isNull(),
                        eq("Batman"),
                        eq(PageRequest.of(1, 5))
                );
    }

    @Test
    void getRequestToUserReviews_withoutPaginationParameters_usesDefaultValues()
            throws Exception {

        UUID userId = UUID.randomUUID();

        Page<AdminReviewDto> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(reviewService.getFilteredReviewsByUserId(
                eq(userId),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        MockHttpServletRequestBuilder request =
                get("/api/reviews/user/{userId}", userId);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(reviewService, times(1))
                .getFilteredReviewsByUserId(
                        eq(userId),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(PageRequest.of(0, 10))
                );
    }

    @Test
    void postRequestToRestoreReview_happyPath() throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                post("/api/reviews/restore/{id}", reviewId)
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(reviewService, times(1))
                .restoreReview(reviewId);
    }

    @Test
    void putRequestToUpdateReview_happyPath() throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                put("/api/reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5,
                                    "content": "This is an updated review with enough content!"
                                }
                                """)
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(reviewService, times(1))
                .updateReview(
                        eq(reviewId),
                        any(EditReviewDto.class)
                );
    }

    @Test
    void putRequestToUpdateReview_withContentTooShort_returnsBadRequest()
            throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                put("/api/reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5,
                                    "content": "Too short"
                                }
                                """)
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .updateReview(any(), any(EditReviewDto.class));
    }

    @Test
    void putRequestToUpdateReview_withRatingBelowMinimum_returnsBadRequest()
            throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                put("/api/reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 0,
                                    "content": "This is a valid review with enough content!"
                                }
                                """)
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .updateReview(any(), any(EditReviewDto.class));
    }

    @Test
    void putRequestToUpdateReview_withRatingAboveMaximum_returnsBadRequest()
            throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                put("/api/reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 11,
                                    "content": "This is a valid review with enough content!"
                                }
                                """)
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .updateReview(any(), any(EditReviewDto.class));
    }

    @Test
    void putRequestToUpdateReview_withBlankContent_returnsBadRequest()
            throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                put("/api/reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rating": 5,
                                    "content": ""
                                }
                                """)
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .updateReview(any(), any(EditReviewDto.class));
    }

    @Test
    void putRequestToUpdateReview_withoutRequestBody_returnsBadRequest()
            throws Exception {

        UUID reviewId = UUID.randomUUID();

        MockHttpServletRequestBuilder request =
                put("/api/reviews/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(reviewService, never())
                .updateReview(any(), any(EditReviewDto.class));
    }


}