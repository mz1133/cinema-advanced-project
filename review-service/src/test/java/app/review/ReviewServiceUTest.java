package app.review;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.comment.service.CommentService;
import app.exception.ReviewNotFoundException;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.review.service.ReviewService;
import app.web.dto.AdminCommentDto;
import app.web.dto.AdminReviewDto;
import app.web.dto.CreateReviewDto;
import app.web.dto.EditReviewDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceUTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ReviewService reviewService;




    @Test
    void givenValidReviewData_whenCreateReview_thenReviewIsCreatedAndSaved() {


        UUID movieId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();

        CreateReviewDto dto = CreateReviewDto.builder()
                .content("Amazing movie!")
                .movieId(movieId)
                .movieTitle("Interstellar")
                .publisherId(publisherId)
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review savedReview = Review.builder()
                .id(UUID.randomUUID())
                .content(dto.getContent())
                .movieId(dto.getMovieId())
                .movieTitle(dto.getMovieTitle())
                .publisherId(dto.getPublisherId())
                .publisherUsername(dto.getPublisherUsername())
                .userRating(dto.getUserRating())
                .isDeleted(false)
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);


        Review result = reviewService.createReview(dto);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Amazing movie!");
        assertThat(result.getMovieId()).isEqualTo(movieId);
        assertThat(result.getMovieTitle()).isEqualTo("Interstellar");
        assertThat(result.getPublisherId()).isEqualTo(publisherId);
        assertThat(result.getPublisherUsername()).isEqualTo("Vik123");
        assertThat(result.getUserRating()).isEqualTo(9);
        assertThat(result.isDeleted()).isFalse();

        verify(reviewRepository, times(1)).save(any(Review.class));
    }




    @Test
    void givenActiveReview_whenDeleteReviewByUser_thenReviewIsMarkedAsDeleted() {


        UUID reviewId = UUID.randomUUID();

        Review review = Review.builder()
                .id(reviewId)
                .isDeleted(false)
                .isDeletedByAdministrator(false)
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));


        reviewService.deleteReview(reviewId, false);


        assertThat(review.isDeleted()).isTrue();
        assertThat(review.isDeletedByAdministrator()).isFalse();

        verify(reviewRepository, times(1)).save(review);
    }


    @Test
    void givenActiveReview_whenDeleteReviewByAdmin_thenReviewIsMarkedAsDeletedByAdministrator() {


        UUID reviewId = UUID.randomUUID();

        Review review = Review.builder()
                .id(reviewId)
                .isDeleted(false)
                .isDeletedByAdministrator(false)
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));


        reviewService.deleteReview(reviewId, true);


        assertThat(review.isDeleted()).isTrue();
        assertThat(review.isDeletedByAdministrator()).isTrue();

        verify(reviewRepository, times(1)).save(review);
    }


    @Test
    void givenDeletedReview_whenDeleteReview_thenReviewNotFoundExceptionIsThrown() {


        UUID reviewId = UUID.randomUUID();

        Review review = Review.builder()
                .id(reviewId)
                .isDeleted(true)
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));


        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(reviewId, false)
        );

        verify(reviewRepository, never()).save(any());
    }


    @Test
    void givenMissingReview_whenDeleteReview_thenReviewNotFoundExceptionIsThrown() {


        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());


        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(reviewId, false)
        );

        verify(reviewRepository, never()).save(any());
    }




    @Test
    void givenExistingReview_whenUpdateReview_thenReviewDetailsAreUpdatedAndSaved() {


        UUID reviewId = UUID.randomUUID();

        Review review = Review.builder()
                .id(reviewId)
                .content("Old content")
                .userRating(5)
                .build();

        EditReviewDto dto = EditReviewDto.builder()
                .content("Updated content")
                .rating(9)
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));


        reviewService.updateReview(reviewId, dto);


        assertThat(review.getContent()).isEqualTo("Updated content");
        assertThat(review.getUserRating()).isEqualTo(9);

        verify(reviewRepository, times(1)).save(review);
    }


    @Test
    void givenMissingReview_whenUpdateReview_thenReviewNotFoundExceptionIsThrown() {


        UUID reviewId = UUID.randomUUID();

        EditReviewDto dto = EditReviewDto.builder()
                .content("Updated content")
                .rating(9)
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());


        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(reviewId, dto)
        );

        verify(reviewRepository, never()).save(any());
    }




    @Test
    void givenExistingReview_whenGetReviewById_thenReturnReview() {


        UUID reviewId = UUID.randomUUID();

        Review review = Review.builder()
                .id(reviewId)
                .content("Great movie!")
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));


        Review result = reviewService.getReviewById(reviewId);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reviewId);
        assertThat(result.getContent()).isEqualTo("Great movie!");

        verify(reviewRepository, times(1)).findById(reviewId);
    }


    @Test
    void givenMissingReview_whenGetReviewById_thenReviewNotFoundExceptionIsThrown() {


        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());


        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.getReviewById(reviewId)
        );
    }




    @Test
    void givenActiveReviewsForMovie_whenGetAllReviewsByMovieIdIsNotDeleted_thenReturnReviews() {


        UUID movieId = UUID.randomUUID();

        Review review1 = Review.builder()
                .id(UUID.randomUUID())
                .movieId(movieId)
                .isDeleted(false)
                .build();

        Review review2 = Review.builder()
                .id(UUID.randomUUID())
                .movieId(movieId)
                .isDeleted(false)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Review> page = new PageImpl<>(
                List.of(review1, review2),
                pageable,
                2
        );

        when(reviewRepository
                .findAllByMovieIdAndIsDeletedFalseOrderByCreatedOnDesc(movieId, pageable))
                .thenReturn(page);


        Page<Review> result =
                reviewService.getAllReviewsByMovieIdIsNotDeleted(movieId, pageable);


        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .containsExactly(review1, review2);

        verify(reviewRepository, times(1))
                .findAllByMovieIdAndIsDeletedFalseOrderByCreatedOnDesc(movieId, pageable);
    }




    @Test
    void givenReviewsWithComments_whenGetAllReviewsAndComments_thenReturnReviewsWithComments() {


        UUID reviewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);

        Review review = Review.builder()
                .id(reviewId)
                .movieId(movieId)
                .movieTitle("Interstellar")
                .publisherId(publisherId)
                .publisherUsername("Vik123")
                .content("Amazing movie!")
                .userRating(9)
                .isDeleted(false)
                .createdOn(LocalDateTime.now())
                .build();

        Page<Review> reviewPage = new PageImpl<>(
                List.of(review),
                pageable,
                1
        );

        Comment comment = Comment.builder()
                .id(commentId)
                .content("I completely agree!")
                .publisherUsername("Pesho")
                .publisherId(UUID.randomUUID())
                .isDeleted(false)
                .createdOn(LocalDateTime.now())
                .review(review)
                .build();

        when(reviewRepository.findAll((Specification<Review>) any(), eq(pageable)))
                .thenReturn(reviewPage);

        when(commentRepository.findByReviewIdInAndIsDeletedFalse(
                List.of(reviewId)))
                .thenReturn(List.of(comment));


        Page<AdminReviewDto> result =
                reviewService.getAllReviewsAndComments(
                        null,
                        null,
                        null,
                        null,
                        pageable
                );


        assertThat(result.getContent()).hasSize(1);

        AdminReviewDto reviewDto = result.getContent().get(0);

        assertThat(reviewDto.getReviewId()).isEqualTo(reviewId);
        assertThat(reviewDto.getMovieId()).isEqualTo(movieId);
        assertThat(reviewDto.getMovieTitle()).isEqualTo("Interstellar");
        assertThat(reviewDto.getContent()).isEqualTo("Amazing movie!");
        assertThat(reviewDto.getPublisherUsername()).isEqualTo("Vik123");
        assertThat(reviewDto.getUserRating()).isEqualTo(9);

        assertThat(reviewDto.getComments()).hasSize(1);

        AdminCommentDto commentDto = reviewDto.getComments().get(0);

        assertThat(commentDto.getCommentId()).isEqualTo(commentId);
        assertThat(commentDto.getContent()).isEqualTo("I completely agree!");
        assertThat(commentDto.getPublisherUsername()).isEqualTo("Pesho");

        verify(reviewRepository, times(1))
                .findAll((Specification<Review>) any(), eq(pageable));

        verify(commentRepository, times(1))
                .findByReviewIdInAndIsDeletedFalse(List.of(reviewId));
    }


    @Test
    void givenNoReviews_whenGetAllReviewsAndComments_thenReturnEmptyPage() {


        Pageable pageable = PageRequest.of(0, 10);

        Page<Review> emptyPage = new PageImpl<>(
                List.of(),
                pageable,
                0
        );

        when(reviewRepository.findAll((Specification<Review>) any(), eq(pageable)))
                .thenReturn(emptyPage);


        Page<AdminReviewDto> result =
                reviewService.getAllReviewsAndComments(
                        null,
                        null,
                        null,
                        null,
                        pageable
                );


        assertThat(result.getContent()).isEmpty();

        verify(reviewRepository, times(1))
                .findAll((Specification<Review>) any(), eq(pageable));

        verify(commentRepository, never())
                .findByReviewIdInAndIsDeletedFalse(any());
    }




    @Test
    void givenReviewsForUser_whenGetFilteredReviewsByUserId_thenReturnFilteredReviews() {


        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);

        Review review = Review.builder()
                .id(reviewId)
                .publisherId(userId)
                .movieTitle("Interstellar")
                .content("Amazing movie!")
                .publisherUsername("Vik123")
                .userRating(9)
                .isDeleted(true)
                .isDeletedByAdministrator(true)
                .createdOn(LocalDateTime.now())
                .build();

        Page<Review> reviewPage = new PageImpl<>(
                List.of(review),
                pageable,
                1
        );

        when(reviewRepository.findAll((Specification<Review>) any(), eq(pageable)))
                .thenReturn(reviewPage);


        Page<AdminReviewDto> result =
                reviewService.getFilteredReviewsByUserId(
                        userId,
                        null,
                        null,
                        null,
                        pageable
                );


        assertThat(result.getContent()).hasSize(1);

        AdminReviewDto dto = result.getContent().get(0);

        assertThat(dto.getReviewId()).isEqualTo(reviewId);
        assertThat(dto.getMovieTitle()).isEqualTo("Interstellar");
        assertThat(dto.getPublisherUsername()).isEqualTo("Vik123");
        assertThat(dto.getContent()).isEqualTo("Amazing movie!");
        assertThat(dto.getUserRating()).isEqualTo(9);
        assertThat(dto.isDeleted()).isTrue();

        verify(reviewRepository, times(1))
                .findAll((Specification<Review>) any(), eq(pageable));
    }


    @Test
    void givenNoReviewsForUser_whenGetFilteredReviewsByUserId_thenReturnEmptyPage() {


        UUID userId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Review> emptyPage = new PageImpl<>(
                List.of(),
                pageable,
                0
        );

        when(reviewRepository.findAll((Specification<Review>) any(), eq(pageable)))
                .thenReturn(emptyPage);


        Page<AdminReviewDto> result =
                reviewService.getFilteredReviewsByUserId(
                        userId,
                        null,
                        null,
                        null,
                        pageable
                );


        assertThat(result.getContent()).isEmpty();

        verify(reviewRepository, times(1))
                .findAll((Specification<Review>) any(), eq(pageable));

        verify(commentRepository, never())
                .findByReviewIdInAndIsDeletedFalse(any());
    }




    @Test
    void givenDeletedReview_whenRestoreReview_thenReviewBecomesActive() {


        UUID reviewId = UUID.randomUUID();

        Review review = Review.builder()
                .id(reviewId)
                .isDeleted(true)
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));


        reviewService.restoreReview(reviewId);


        assertThat(review.isDeleted()).isFalse();

        verify(reviewRepository, times(1)).save(review);
    }


    @Test
    void givenMissingReview_whenRestoreReview_thenReviewNotFoundExceptionIsThrown() {


        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());


        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.restoreReview(reviewId)
        );

        verify(reviewRepository, never()).save(any());
    }
}