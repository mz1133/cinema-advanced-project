package org.app.reviewclient;


import org.app.web.dto.*;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "review-service", url = "http://localhost:8081")
public interface ReviewClient {

    @GetMapping("/api/reviews/movies/{id}")
    CustomPageDto<ViewReviewsAndCommentsDto> getReviewMovie(
            @PathVariable("id") UUID id,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @PostMapping("/api/reviews")
    ResponseEntity<Void> createReview(@RequestBody CreateReviewDto createReviewDto);

    @PostMapping("/api/comments")
    ResponseEntity<Void> createComment(@RequestBody CreateCommentDto createCommentDto);

    @GetMapping("/api/reviews/reviews")
    CustomPageDto<AdminReviewDto> getAllReviewsAndComments(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "movieId", required = false) UUID movieId,
            @RequestParam(value = "publisherUsername", required = false) String publisherUsername,
            @RequestParam(value = "movieTitle", required = false) String movieTitle,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @GetMapping("/api/reviews/user/{userId}")
    CustomPageDto<AdminReviewDto> getUserReviews(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "movieId", required = false) UUID movieId,
            @RequestParam(value = "movieTitle", required = false) String movieTitle,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @DeleteMapping("/api/reviews/{id}")
    void deleteReview(@PathVariable("id") UUID id, @RequestParam(name = "isAdmin") boolean isAdmin);

    @PostMapping("/api/reviews/restore/{id}")
    void restoreReview(@PathVariable("id") UUID id);

    @PutMapping("/api/reviews/{id}")
    void updateReview(@PathVariable("id") UUID id, @RequestBody EditReviewDto dto);

    @PostMapping("/api/comments/delete")
    void deleteComment(@RequestBody DeleteCommentDto deleteCommentDto);




}
