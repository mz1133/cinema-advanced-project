package app.review;

import app.comment.model.Comment;

import app.comment.service.CommentService;
import app.exception.CommentNotFoundException;
import app.exception.ReviewNotFoundException;
import app.review.model.Review;

import app.review.repository.ReviewRepository;
import app.review.service.ReviewService;
import app.web.dto.*;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReviewCommentITests {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReviewRepository reviewRepository;




    @Test
    void givenExistingReview_whenCreateComment_thenCommentIsLinkedToReview() {

        UUID movieId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Amazing movie!")
                .movieId(movieId)
                .movieTitle("Interstellar")
                .publisherId(publisherId)
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review review = reviewService.createReview(reviewDto);

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(review.getId())
                .content("I completely agree!")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .build();

        Comment comment = commentService.createComment(commentDto);

        assertThat(comment).isNotNull();

        assertThat(comment.getId()).isNotNull();

        assertThat(comment.getContent())
                .isEqualTo("I completely agree!");

        assertThat(comment.getPublisherUsername())
                .isEqualTo("Pesho");

        assertThat(comment.isDeleted())
                .isFalse();

        assertThat(comment.getReview())
                .isNotNull();

        assertThat(comment.getReview().getId())
                .isEqualTo(review.getId());
    }

    @Test
    void givenMissingReview_whenCreateComment_thenExceptionIsThrown() {

        UUID missingReviewId = UUID.randomUUID();

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(missingReviewId)
                .content("Some comment")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .build();

        assertThatThrownBy(() -> commentService.createComment(commentDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Review not found");
    }

    @Test
    void givenExistingReview_whenGetReviewById_thenReturnCorrectReview() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Great movie!")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(10)
                .build();

        Review createdReview = reviewService.createReview(reviewDto);

        Review result = reviewService.getReviewById(createdReview.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(createdReview.getId());
        assertThat(result.getContent()).isEqualTo("Great movie!");
        assertThat(result.getMovieTitle()).isEqualTo("Interstellar");
        assertThat(result.getPublisherUsername()).isEqualTo("Vik123");
        assertThat(result.getUserRating()).isEqualTo(10);
    }

    @Test
    void givenMissingReview_whenGetReviewById_thenExceptionIsThrown() {

        UUID missingReviewId = UUID.randomUUID();

        assertThatThrownBy(() -> reviewService.getReviewById(missingReviewId))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessage("Review with id: " + missingReviewId + " not found");
    }

    @Test
    void givenExistingReview_whenUpdateReview_thenReviewDetailsAreUpdated() {

        CreateReviewDto createReviewDto = CreateReviewDto.builder()
                .content("Good movie")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(7)
                .build();

        Review review = reviewService.createReview(createReviewDto);

        EditReviewDto editReviewDto = EditReviewDto.builder()
                .content("Amazing movie!")
                .rating(10)
                .build();

        reviewService.updateReview(review.getId(), editReviewDto);

        Review updatedReview = reviewService.getReviewById(review.getId());

        assertThat(updatedReview.getContent())
                .isEqualTo("Amazing movie!");

        assertThat(updatedReview.getUserRating())
                .isEqualTo(10);
    }


    @Test
    void givenExistingActiveReview_whenDeleteReviewAsUser_thenReviewIsMarkedAsDeleted() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Good movie")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(8)
                .build();

        Review review = reviewService.createReview(reviewDto);

        reviewService.deleteReview(review.getId(), false);

        Review deletedReview = reviewService.getReviewById(review.getId());

        assertThat(deletedReview.isDeleted())
                .isTrue();

        assertThat(deletedReview.isDeletedByAdministrator())
                .isFalse();
    }


    @Test
    void givenExistingActiveReview_whenDeleteReviewAsAdmin_thenReviewIsMarkedAsDeletedByAdministrator() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Good movie")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(8)
                .build();

        Review review = reviewService.createReview(reviewDto);

        reviewService.deleteReview(review.getId(), true);

        Review deletedReview = reviewService.getReviewById(review.getId());

        assertThat(deletedReview.isDeleted())
                .isTrue();

        assertThat(deletedReview.isDeletedByAdministrator())
                .isTrue();
    }

    @Test
    void givenAlreadyDeletedReview_whenDeleteReview_thenExceptionIsThrown() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Good movie")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(8)
                .build();

        Review review = reviewService.createReview(reviewDto);

        reviewService.deleteReview(review.getId(), false);

        assertThatThrownBy(() ->
                reviewService.deleteReview(review.getId(), false))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessage("Review with id: " + review.getId() + " not found");
    }


    @Test
    void givenDeletedReview_whenRestoreReview_thenReviewBecomesActive() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Good movie")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(8)
                .build();

        Review review = reviewService.createReview(reviewDto);

        reviewService.deleteReview(review.getId(), false);

        reviewService.restoreReview(review.getId());

        Review restoredReview = reviewService.getReviewById(review.getId());

        assertThat(restoredReview.isDeleted())
                .isFalse();
    }

    @Test
    void givenMissingReview_whenRestoreReview_thenExceptionIsThrown() {

        UUID missingReviewId = UUID.randomUUID();

        assertThatThrownBy(() ->
                reviewService.restoreReview(missingReviewId))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessage("Review with id: %s not found".formatted(missingReviewId));
    }

    @Test
    void givenExistingActiveComment_whenDeleteComment_thenCommentIsMarkedAsDeleted() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Amazing movie!")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review review = reviewService.createReview(reviewDto);

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(review.getId())
                .content("I agree!")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .build();

        Comment comment = commentService.createComment(commentDto);

        DeleteCommentDto deleteCommentDto = DeleteCommentDto.builder()
                .commentId(comment.getId())
                .build();

        commentService.deleteComment(deleteCommentDto);

        assertThat(comment.isDeleted())
                .isTrue();
    }

    @Test
    void givenMissingComment_whenDeleteComment_thenExceptionIsThrown() {

        UUID missingCommentId = UUID.randomUUID();

        DeleteCommentDto deleteCommentDto = DeleteCommentDto.builder()
                .commentId(missingCommentId)
                .build();

        assertThatThrownBy(() ->
                commentService.deleteComment(deleteCommentDto))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("Comment not found");
    }

    @Test
    void givenAlreadyDeletedComment_whenDeleteComment_thenExceptionIsThrown() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("Amazing movie!")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review review = reviewService.createReview(reviewDto);

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(review.getId())
                .content("I agree!")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .build();

        Comment comment = commentService.createComment(commentDto);

        DeleteCommentDto deleteCommentDto = DeleteCommentDto.builder()
                .commentId(comment.getId())
                .build();

        commentService.deleteComment(deleteCommentDto);

        assertThatThrownBy(() ->
                commentService.deleteComment(deleteCommentDto))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("Comment not found");
    }

    @Test
    void givenExistingActiveComments_whenGetAllCommentsByReviewId_thenReturnOnlyActiveComments() {

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("I really enjoyed this movie because the story and characters were amazing!")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review review = reviewService.createReview(reviewDto);

        CreateCommentDto firstCommentDto = CreateCommentDto.builder()
                .reviewId(review.getId())
                .content("First comment: The good review")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .build();

        CreateCommentDto secondCommentDto = CreateCommentDto.builder()
                .reviewId(review.getId())
                .content("Second comment: The good review")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Gosho")
                .build();

        Comment firstComment = commentService.createComment(firstCommentDto);
        Comment secondComment = commentService.createComment(secondCommentDto);

        DeleteCommentDto deleteCommentDto = DeleteCommentDto.builder()
                .commentId(secondComment.getId())
                .build();

        commentService.deleteComment(deleteCommentDto);

        var comments = commentService
                .getAllCommentsByReviewIdAndIsDeletedFalse(review.getId());

        assertThat(comments)
                .hasSize(1);

        assertThat(comments.get(0).getId())
                .isEqualTo(firstComment.getId());

        assertThat(comments.get(0).isDeleted())
                .isFalse();
    }

    @Test
    void givenExistingReviewWithComments_whenGetAllActivesReviewsAndCommentsByMovieId_thenReturnReviewWithComments() {

        UUID movieId = UUID.randomUUID();

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("I really enjoyed this movie because the story was absolutely amazing!")
                .movieId(movieId)
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review review = reviewService.createReview(reviewDto);

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(review.getId())
                .content("I completely agree!")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .build();

        commentService.createComment(commentDto);

        Pageable pageable = PageRequest.of(0, 10);

        Page<ViewReviewsAndCommentsDto> result =
                reviewService.getAllActivesReviewsAndCommentsByMovieId(movieId, pageable);

        assertThat(result).hasSize(1);

        assertThat(result.getContent().get(0).getReviewId())
                .isEqualTo(review.getId());

        assertThat(result.getContent().get(0).getComments())
                .hasSize(1);

        assertThat(result.getContent().get(0).getComments().get(0).getContent())
                .isEqualTo("I completely agree!");
    }

    @Test
    void givenReviewWithoutComments_whenGetAllActivesReviewsAndCommentsByMovieId_thenReturnReviewWithoutComments() {

        UUID movieId = UUID.randomUUID();

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("I really enjoyed this movie because the story was absolutely amazing!")
                .movieId(movieId)
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review review = reviewService.createReview(reviewDto);

        Pageable pageable = PageRequest.of(0, 10);

        Page<ViewReviewsAndCommentsDto> result =
                reviewService.getAllActivesReviewsAndCommentsByMovieId(movieId, pageable);

        assertThat(result).hasSize(1);

        assertThat(result.getContent().get(0).getReviewId())
                .isEqualTo(review.getId());

        assertThat(result.getContent().get(0).getComments())
                .isEmpty();
    }

    @Test
    void givenExistingReviewWithComments_whenGetFilteredReviewsByUserId_thenReturnReviewWithComments() {

        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        CreateReviewDto reviewDto = CreateReviewDto.builder()
                .content("I really enjoyed this movie because the story was absolutely amazing!")
                .movieId(movieId)
                .movieTitle("Interstellar")
                .publisherId(userId)
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        Review review = reviewService.createReview(reviewDto);

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(review.getId())
                .content("I completely agree with this review!")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .build();

        Comment comment = commentService.createComment(commentDto);

        Pageable pageable = PageRequest.of(0, 10);

        Page<AdminReviewDto> result =
                reviewService.getFilteredReviewsByUserId(
                        userId,
                        "amazing",
                        movieId,
                        "Interstellar",
                        pageable
                );

        assertThat(result).hasSize(1);

        AdminReviewDto resultReview = result.getContent().get(0);

        assertThat(resultReview.getReviewId())
                .isEqualTo(review.getId());

        assertThat(resultReview.getMovieId())
                .isEqualTo(movieId);

        assertThat(resultReview.getMovieTitle())
                .isEqualTo("Interstellar");

        assertThat(resultReview.getPublisherUsername())
                .isEqualTo("Vik123");

        assertThat(resultReview.getComments())
                .hasSize(1);

        assertThat(resultReview.getComments().get(0).getCommentId())
                .isEqualTo(comment.getId());

        assertThat(resultReview.getComments().get(0).getContent())
                .isEqualTo("I completely agree with this review!");

        assertThat(resultReview.getComments().get(0).getPublisherUsername())
                .isEqualTo("Pesho");

        assertThat(resultReview.getComments().get(0).isDeleted())
                .isFalse();
    }

    @Test
    void givenReviewsWithDifferentContent_whenGetFilteredReviewsByUserId_thenReturnOnlyMatchingReview() {

        UUID userId = UUID.randomUUID();

        UUID firstMovieId = UUID.randomUUID();

        UUID secondMovieId = UUID.randomUUID();

        CreateReviewDto firstReviewDto = CreateReviewDto.builder()
                .content("This movie was absolutely amazing and I really enjoyed it!")
                .movieId(firstMovieId)
                .movieTitle("Interstellar")
                .publisherId(userId)
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        CreateReviewDto secondReviewDto = CreateReviewDto.builder()
                .content("The movie was boring and I did not enjoy it.")
                .movieId(secondMovieId)
                .movieTitle("Avatar")
                .publisherId(userId)
                .publisherUsername("Vik123")
                .userRating(5)
                .build();

        Review firstReview = reviewService.createReview(firstReviewDto);
        Review secondReview = reviewService.createReview(secondReviewDto);

        Pageable pageable = PageRequest.of(0, 10);

        Page<AdminReviewDto> result =
                reviewService.getFilteredReviewsByUserId(
                        userId,
                        "amazing",
                        null,
                        null,
                        pageable
                );

        assertThat(result).hasSize(1);

        AdminReviewDto resultReview = result.getContent().get(0);

        assertThat(resultReview.getReviewId())
                .isEqualTo(firstReview.getId());

        assertThat(resultReview.getContent())
                .contains("amazing");

        assertThat(resultReview.getReviewId())
                .isNotEqualTo(secondReview.getId());
    }

    @Test
    void givenReviewsWithDifferentUsernames_whenGetAllReviewsAndCommentsWithUsername_thenReturnMatchingReviews() {

        CreateReviewDto firstReviewDto = CreateReviewDto.builder()
                .content("I really enjoyed this movie because the story was amazing!")
                .movieId(UUID.randomUUID())
                .movieTitle("Interstellar")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .userRating(9)
                .build();

        CreateReviewDto secondReviewDto = CreateReviewDto.builder()
                .content("This movie was interesting and had a great story!")
                .movieId(UUID.randomUUID())
                .movieTitle("Inception")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Pesho")
                .userRating(8)
                .build();

        Review firstReview = reviewService.createReview(firstReviewDto);
        reviewService.createReview(secondReviewDto);

        Pageable pageable = PageRequest.of(0, 10);

        Page<AdminReviewDto> result =
                reviewService.getAllReviewsAndComments(
                        null,
                        null,
                        "vik",
                        null,
                        pageable
                );

        assertThat(result).hasSize(1);

        assertThat(result.getContent().get(0).getReviewId())
                .isEqualTo(firstReview.getId());

        assertThat(result.getContent().get(0).getPublisherUsername())
                .isEqualTo("Vik123");
    }
}