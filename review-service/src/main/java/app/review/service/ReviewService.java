package app.review.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.comment.service.CommentService;
import app.exception.ReviewNotFoundException;
import app.review.model.Review;
import app.review.repository.ReviewRepository;
import app.util.SpecificationSearch;
import app.web.dto.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ReviewService {

    private final static String ERROR_MESSAGE_REVIEW_NOT_FOUND = "Review with id: %s not found";


    private final ReviewRepository reviewRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    public ReviewService(ReviewRepository reviewRepository, CommentService commentService, CommentRepository commentRepository) {
        this.reviewRepository = reviewRepository;
        this.commentService = commentService;
        this.commentRepository = commentRepository;
    }

    @CacheEvict(value = "reviews", allEntries = true)
    public Review createReview(CreateReviewDto reviewDto) {

        Review review = Review.builder()
                .content(reviewDto.getContent())
                .movieId(reviewDto.getMovieId())
                .movieTitle(reviewDto.getMovieTitle())
                .publisherId(reviewDto.getPublisherId())
                .publisherUsername(reviewDto.getPublisherUsername())
                .userRating(reviewDto.getUserRating())
                .isDeleted(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        log.info("Successfully created review with id: {%s} by user id: {%s} ".formatted(review.getId(), reviewDto.getPublisherId()));
        return reviewRepository.save(review);
    }

    @Transactional
    @CacheEvict(value = "reviews", allEntries = true)
    public void deleteReview(UUID id, boolean isAdmin) {

        Review reviewToDelete = getReviewById(id);

        if (reviewToDelete.isDeleted()) {

            throw new ReviewNotFoundException(ERROR_MESSAGE_REVIEW_NOT_FOUND.formatted(id));
        }

        if(isAdmin){
            reviewToDelete.setDeletedByAdministrator(true);
        }

        reviewToDelete.setDeleted(true);
        reviewToDelete.setUpdatedOn(LocalDateTime.now());

        reviewRepository.save(reviewToDelete);

        log.info("Successfully delete review with id: {%s}".formatted(id));
    }

    @Transactional
    @CacheEvict(value = "reviews", allEntries = true)
    public void updateReview(UUID id, EditReviewDto updateReviewDto) {

        Review reviewToUpdate = getReviewById(id);

        reviewToUpdate.setContent(updateReviewDto.getContent());
        reviewToUpdate.setUserRating(updateReviewDto.getRating());
        reviewToUpdate.setUpdatedOn(LocalDateTime.now());

        reviewRepository.save(reviewToUpdate);

        log.info("Successfully update review with id: {%s}".formatted(id));
    }

    public Page<ViewReviewsAndCommentsDto> getAllActivesReviewsAndCommentsByMovieId(UUID movieId, Pageable pageable) {

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

        return reviewRepository.findAllByMovieIdAndIsDeletedFalseOrderByCreatedOnDesc(movieId, pageable);
    }

    public Review getReviewById(UUID id) {

        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(ERROR_MESSAGE_REVIEW_NOT_FOUND.formatted(id)));
    }

    @Cacheable(value = "reviews")
    public Page<AdminReviewDto> getAllReviewsAndComments(
            String keyword,
            UUID movieId,
            String publisherUsername,
            String movieTitle,
            Pageable pageable) {

        Specification<Review> specification =
                SpecificationSearch.<Review>hasKeyword(keyword)
                        .and(SpecificationSearch.hasMovieId(movieId))
                        .and(SpecificationSearch.hasUsername(publisherUsername))
                        .and(SpecificationSearch.hasMovieTitle(movieTitle))
                        .and(SpecificationSearch.isNotDeleted());


        Page<Review> reviews =
                reviewRepository.findAll(specification, pageable);

        List<UUID> reviewIds = reviews.getContent()
                .stream()
                .map(Review::getId)
                .toList();

        List<Comment> comments = reviewIds.isEmpty()
                ? Collections.emptyList()
                : commentRepository.findByReviewIdInAndIsDeletedFalse(reviewIds);

        Map<UUID, List<Comment>> commentsByReview =
                comments.stream()
                        .collect(Collectors.groupingBy(
                                comment -> comment.getReview().getId()
                        ));

        return reviews.map(review -> {

            List<AdminCommentDto> commentDtos =
                    commentsByReview
                            .getOrDefault(review.getId(), Collections.emptyList())
                            .stream()
                            .map(comment -> AdminCommentDto.builder()

                                    .commentId(comment.getId())
                                    .content(comment.getContent())
                                    .isDeleted(comment.isDeleted())
                                    .publisherUsername(comment.getPublisherUsername())
                                    .publisherId(comment.getPublisherId())
                                    .createdOn(comment.getCreatedOn())
                                    .build())
                            .toList();

            return AdminReviewDto.builder()
                    .reviewId(review.getId())
                    .movieId(review.getMovieId())
                    .rating(review.getUserRating())
                    .deleted(review.isDeleted())
                    .content(review.getContent())
                    .publisherUsername(review.getPublisherUsername())
                    .userRating(review.getUserRating())
                    .createdOn(review.getCreatedOn())
                    .publisherId(review.getPublisherId())
                    .movieTitle(review.getMovieTitle())
                    .comments(commentDtos)
                    .build();
        });
    }

    public Page<AdminReviewDto> getFilteredReviewsByUserId(UUID userId, String keyword, UUID movieId, String movieTitle, Pageable pageable) {

        Specification<Review> spec = SpecificationSearch.<Review>hasPublisherId(userId)
                .and(SpecificationSearch.hasKeyword(keyword))
                .and(SpecificationSearch.hasMovieId(movieId))
                .and(SpecificationSearch.hasMovieTitle(movieTitle))
                .and(SpecificationSearch.isDeletedByAdministrator());


        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);

        List<UUID> reviewIds = reviewPage.getContent()
                .stream()
                .map(Review::getId)
                .toList();

        List<Comment> comments = reviewIds.isEmpty()
                ? Collections.emptyList()
                : commentRepository.findByReviewIdInAndIsDeletedFalse(reviewIds);

        Map<UUID, List<Comment>> commentsByReview =
                comments.stream()
                        .collect(Collectors.groupingBy(
                                comment -> comment.getReview().getId()
                        ));

        return reviewPage.map(review -> {
            List<AdminCommentDto> commentDtos =
                    commentsByReview
                            .getOrDefault(review.getId(), Collections.emptyList())
                            .stream()
                            .map(comment -> AdminCommentDto.builder()
                                    .commentId(comment.getId())
                                    .content(comment.getContent())
                                    .isDeleted(comment.isDeleted())
                                    .publisherUsername(comment.getPublisherUsername())
                                    .createdOn(comment.getCreatedOn())
                                    .build())
                            .toList();

            return AdminReviewDto.builder()
                    .reviewId(review.getId())
                    .movieId(review.getMovieId())
                    .movieTitle(review.getMovieTitle())
                    .rating(review.getUserRating())
                    .deleted(review.isDeleted())
                    .content(review.getContent())
                    .publisherUsername(review.getPublisherUsername())
                    .userRating(review.getUserRating())
                    .createdOn(review.getCreatedOn())
                    .comments(commentDtos)
                    .build();
        });
    }

    @Transactional
    public void restoreReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ERROR_MESSAGE_REVIEW_NOT_FOUND.formatted(reviewId)));

        review.setDeleted(false);

        reviewRepository.save(review);

        log.info("Successfully restored review id: {%s} ".formatted(reviewId));
    }

}
