package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.exception.CommentNotFoundException;
import app.review.model.Review;
import app.review.repository.ReviewRepository;

import app.web.dto.CreateCommentDto;
import app.web.dto.DeleteCommentDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class CommentService {

    private final static String ERROR_MESSAGE_COMMENT_NOT_FOUND = "Comment not found";

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    public CommentService(CommentRepository commentRepository, ReviewRepository reviewRepository) {
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
    }

    @CacheEvict(value = "reviews", allEntries = true)
    public Comment createComment(CreateCommentDto commentDto) {

        Review review = reviewRepository.findById(commentDto.getReviewId())
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Comment createdComment = Comment.builder()
                .content(commentDto.getContent())
                .publisherId(commentDto.getPublisherId())
                .publisherUsername(commentDto.getPublisherUsername())
                .isDeleted(false)
                .review(review)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        log.info("Successfully created comment by username: {%s}"
                .formatted(commentDto.getPublisherUsername()));

        return commentRepository.save(createdComment);
    }

    @CacheEvict(value = "reviews", allEntries = true)
    public void deleteComment(DeleteCommentDto deleteCommentDto) {

        Comment comment = findCommentById(deleteCommentDto.getCommentId());

        if (comment.isDeleted()) {
            throw new CommentNotFoundException(ERROR_MESSAGE_COMMENT_NOT_FOUND);
        }

        comment.setDeleted(true);
        commentRepository.save(comment);

        log.info("Successfully deleted comment id: {%s} ".formatted(deleteCommentDto.getCommentId()));
    }

    public List<Comment> getAllCommentsByReviewIdAndIsDeletedFalse(UUID movieId) {

        return commentRepository.findAllByReviewIdAndIsDeletedFalse(movieId);
    }


    private Comment findCommentById(UUID commentId) {

        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ERROR_MESSAGE_COMMENT_NOT_FOUND));
    }


}
