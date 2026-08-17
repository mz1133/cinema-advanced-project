package app.review.service;

import app.exception.ReviewNotFoundException;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.web.dto.CreateReviewDto;
import app.web.dto.UpdateReviewDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final static String ERROR_MESSAGE_REVIEW_NOT_FOUND = "Review with id: %s not found";


    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
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

    public List<Review> getAllReviewsByMovieIdIsNotDeleted(UUID movieId) {

        return reviewRepository.findAllIsDeleteFalse();
    }

    public Review getByMovieId(UUID movieId) {

     return getReviewById(movieId);
    }


    private Review getReviewById(UUID id) {

        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(ERROR_MESSAGE_REVIEW_NOT_FOUND.formatted(id)));
    }
}
