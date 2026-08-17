package app.review.service;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.exception.ReviewNotFoundException;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.web.dto.CreateReviewDto;
import app.web.dto.UpdateReviewDto;
import app.web.dto.ViewCommentsDto;
import app.web.dto.ViewReviewsAndCommentsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final static String ERROR_MESSAGE_REVIEW_NOT_FOUND = "Review with id: %s not found";


    private final ReviewRepository reviewRepository;
    private final CommentService commentService;

    public ReviewService(ReviewRepository reviewRepository, CommentService commentService) {
        this.reviewRepository = reviewRepository;
        this.commentService = commentService;
    }


    public Review createReview(CreateReviewDto reviewDto) {

        Review review = Review.builder()
                .content(reviewDto.getContent())
                .movieId(reviewDto.getMovieId())
                .publisherId(reviewDto.getPublisherId())
                .publisherUsername(reviewDto.getPublisherUsername())
                .userRating(reviewDto.getUserRating())
                .userCountry(reviewDto.getUserCountry())
                .isDeleted(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

       return reviewRepository.save(review);
    }

    public void deleteReview(UUID id) {

        Review reviewToDelete = getReviewById(id);

        if(reviewToDelete.isDeleted()){

            throw new ReviewNotFoundException(ERROR_MESSAGE_REVIEW_NOT_FOUND.formatted(id));
        }

        reviewToDelete.setDeleted(true);
        reviewToDelete.setUpdatedOn(LocalDateTime.now());

        reviewRepository.save(reviewToDelete);

    }

    public void updateReview(UpdateReviewDto updateReviewDto) {

        Review reviewToUpdate = getReviewById(updateReviewDto.getReviewId());

        reviewToUpdate.setContent(updateReviewDto.getContent());
        reviewToUpdate.setUserRating(updateReviewDto.getRating());
        reviewToUpdate.setUpdatedOn(LocalDateTime.now());

        reviewRepository.save(reviewToUpdate);
    }

    public Page<ViewReviewsAndCommentsDto> getAllActivesReviewsAndComments(UUID movieId, Pageable pageable) {

        Page<Review> reviews =
                getAllReviewsByMovieIdIsNotDeleted(movieId, pageable);

        return reviews.map(review -> {


            List<Comment> comments = commentService.getAllCommentsByReviewIdAndIsDeletedFalse(review.getId());


            List<ViewCommentsDto> commentsDto = comments.stream()
                    .map(comment -> new ViewCommentsDto(
                            comment.getId(),
                            comment.getContent(),
                            comment.getPublisherUsername(),
                            comment.getCreatedOn(),
                            comment.getUpdatedOn()
                    ))
                    .toList();


            return new ViewReviewsAndCommentsDto(
                    review.getContent(),
                    review.getId(),
                    review.getPublisherUsername(),
                    review.getUserRating(),
                    review.getCreatedOn(),
                    commentsDto
            );
        });


    }

    public Page<Review> getAllReviewsByMovieIdIsNotDeleted(UUID movieId, Pageable pageable) {

     return reviewRepository.findAllByMovieIdAndIsDeletedFalse(movieId, pageable);
    }



    private Review getReviewById(UUID id) {

        return reviewRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ReviewNotFoundException(ERROR_MESSAGE_REVIEW_NOT_FOUND.formatted(id)));
    }
}
