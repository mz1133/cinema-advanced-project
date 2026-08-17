package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.exception.CommentNotFoundException;
import app.web.dto.CreateCommentDto;
import app.web.dto.UpdateCommentDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final static String ERROR_MESSAGE_COMMENT_NOT_FOUND = "Comment not found";

   private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }


    public Comment createComment(CreateCommentDto comment) {

        Comment createdComment = Comment.builder()
                .content(comment.getContent())
                .publisherId(comment.getPublisherId())
                .publisherUsername(comment.getPublisherUsername())
                .isDeleted(false)
                .review(comment.getReview())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

       return commentRepository.save(createdComment);

    }

    public void  deleteComment(UUID id) {

        Comment comment = findCommentById(id);

        if(comment.isDeleted()) {
            throw new CommentNotFoundException(ERROR_MESSAGE_COMMENT_NOT_FOUND);
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    public Comment updateComment(UpdateCommentDto updateCommentDto) {

        Comment comment = findCommentById(updateCommentDto.getCommentId());

        comment.setContent(updateCommentDto.getCommentContent());
        comment.setUpdatedOn(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public List<Comment> getAllCommentsByReviewIdAndIsDeletedFalse(UUID movieId) {

        return commentRepository.findAllByReviewIdAndIsDeletedFalse(movieId);
    }


    private Comment findCommentById(UUID commentId) {

        return  commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ERROR_MESSAGE_COMMENT_NOT_FOUND));
    }


}
