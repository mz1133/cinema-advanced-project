package app.comment;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.comment.service.CommentService;
import app.exception.CommentNotFoundException;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.web.dto.CreateCommentDto;
import app.web.dto.DeleteCommentDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceUTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CommentService commentService;




    @Test
    void givenValidCommentData_whenCreateComment_thenCommentIsCreatedAndSaved() {


        UUID reviewId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(reviewId)
                .content("Great review!")
                .publisherId(publisherId)
                .publisherUsername("Vik123")
                .build();

        Review review = Review.builder()
                .id(reviewId)
                .build();

        Comment savedComment = Comment.builder()
                .id(UUID.randomUUID())
                .content(commentDto.getContent())
                .publisherId(commentDto.getPublisherId())
                .publisherUsername(commentDto.getPublisherUsername())
                .isDeleted(false)
                .review(review)
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);


        Comment result = commentService.createComment(commentDto);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Great review!");
        assertThat(result.getPublisherId()).isEqualTo(publisherId);
        assertThat(result.getPublisherUsername()).isEqualTo("Vik123");
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getReview()).isEqualTo(review);

        verify(reviewRepository, times(1))
                .findById(reviewId);

        verify(commentRepository, times(1))
                .save(any(Comment.class));
    }


    @Test
    void givenMissingReview_whenCreateComment_thenExceptionIsThrown() {


        UUID reviewId = UUID.randomUUID();

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .reviewId(reviewId)
                .content("Great review!")
                .publisherId(UUID.randomUUID())
                .publisherUsername("Vik123")
                .build();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());


        assertThrows(
                RuntimeException.class,
                () -> commentService.createComment(commentDto)
        );

        verify(commentRepository, never())
                .save(any());
    }




    @Test
    void givenActiveComment_whenDeleteComment_thenCommentIsMarkedAsDeletedAndSaved() {


        UUID commentId = UUID.randomUUID();

        DeleteCommentDto deleteCommentDto = DeleteCommentDto.builder()
                .commentId(commentId)
                .build();

        Comment comment = Comment.builder()
                .id(commentId)
                .isDeleted(false)
                .build();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));


        commentService.deleteComment(deleteCommentDto);


        assertThat(comment.isDeleted()).isTrue();

        verify(commentRepository, times(1))
                .findById(commentId);

        verify(commentRepository, times(1))
                .save(comment);
    }


    @Test
    void givenMissingComment_whenDeleteComment_thenCommentNotFoundExceptionIsThrown() {


        UUID commentId = UUID.randomUUID();

        DeleteCommentDto deleteCommentDto = DeleteCommentDto.builder()
                .commentId(commentId)
                .build();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());


        assertThrows(
                CommentNotFoundException.class,
                () -> commentService.deleteComment(deleteCommentDto)
        );

        verify(commentRepository, never())
                .save(any());
    }


    @Test
    void givenDeletedComment_whenDeleteComment_thenCommentNotFoundExceptionIsThrown() {


        UUID commentId = UUID.randomUUID();

        DeleteCommentDto deleteCommentDto = DeleteCommentDto.builder()
                .commentId(commentId)
                .build();

        Comment comment = Comment.builder()
                .id(commentId)
                .isDeleted(true)
                .build();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));


        assertThrows(
                CommentNotFoundException.class,
                () -> commentService.deleteComment(deleteCommentDto)
        );

        verify(commentRepository, never())
                .save(any());
    }




    @Test
    void givenActiveCommentsForReview_whenGetAllCommentsByReviewIdAndIsDeletedFalse_thenReturnComments() {


        UUID reviewId = UUID.randomUUID();

        Comment comment1 = Comment.builder()
                .id(UUID.randomUUID())
                .content("First comment")
                .isDeleted(false)
                .build();

        Comment comment2 = Comment.builder()
                .id(UUID.randomUUID())
                .content("Second comment")
                .isDeleted(false)
                .build();

        List<Comment> comments = List.of(comment1, comment2);

        when(commentRepository
                .findAllByReviewIdAndIsDeletedFalse(reviewId))
                .thenReturn(comments);


        List<Comment> result =
                commentService.getAllCommentsByReviewIdAndIsDeletedFalse(reviewId);


        assertThat(result)
                .hasSize(2)
                .containsExactly(comment1, comment2);

        verify(commentRepository, times(1))
                .findAllByReviewIdAndIsDeletedFalse(reviewId);
    }


    @Test
    void givenNoActiveCommentsForReview_whenGetAllCommentsByReviewIdAndIsDeletedFalse_thenReturnEmptyList() {


        UUID reviewId = UUID.randomUUID();

        when(commentRepository
                .findAllByReviewIdAndIsDeletedFalse(reviewId))
                .thenReturn(List.of());


        List<Comment> result =
                commentService.getAllCommentsByReviewIdAndIsDeletedFalse(reviewId);


        assertThat(result).isEmpty();

        verify(commentRepository, times(1))
                .findAllByReviewIdAndIsDeletedFalse(reviewId);
    }
}