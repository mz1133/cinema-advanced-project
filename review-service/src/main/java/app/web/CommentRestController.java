package app.web;

import app.comment.service.CommentService;
import app.web.dto.CreateCommentDto;

import app.web.dto.DeleteCommentDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentRestController {

    private final CommentService commentService;

    public CommentRestController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Void> createComment(@Valid @RequestBody CreateCommentDto comment) {

        commentService.createComment(comment);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/delete")
    public ResponseEntity<Void> deleteComment(@Valid @RequestBody DeleteCommentDto deleteCommentDto) {

        commentService.deleteComment(deleteCommentDto);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
