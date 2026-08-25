package app.web;


import app.review.service.ReviewService;
import app.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewRestController {

    private final ReviewService reviewService;

    public ReviewRestController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Void> createReview(@Valid @RequestBody CreateReviewDto createReviewDto) {

        reviewService.createReview(createReviewDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id, boolean isAdmin) {

        reviewService.deleteReview(id, isAdmin);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<CustomPageDto<ViewReviewsAndCommentsDto>>
    getAllReviewsAndCommentByMovie(@PathVariable UUID id,
                                                                                                   Pageable pageable) {
        Page<ViewReviewsAndCommentsDto> reviewsAndComments = reviewService
                .getAllActivesReviewsAndCommentsByMovieId(id, pageable);

        CustomPageDto<ViewReviewsAndCommentsDto> response = new CustomPageDto<>();

        response.setContent(reviewsAndComments.getContent());
        response.setCurrentPage(reviewsAndComments.getNumber());
        response.setTotalPages(reviewsAndComments.getTotalPages());
        response.setTotalElements(reviewsAndComments.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews")
    public ResponseEntity<CustomPageDto<AdminReviewDto>> getAllReviewsAndComments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) String publisherUsername,
            @RequestParam(required = false) String movieTitle,
            Pageable pageable) {

        Page<AdminReviewDto> reviews =
                reviewService.getAllReviewsAndComments(
                        keyword,
                        movieId,
                        publisherUsername,
                        movieTitle,
                        pageable

                );

        CustomPageDto<AdminReviewDto> response = new CustomPageDto<>();

        response.setContent(reviews.getContent());
        response.setCurrentPage(reviews.getNumber());
        response.setTotalPages(reviews.getTotalPages());
        response.setTotalElements(reviews.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomPageDto<AdminReviewDto>> getUserReviews(
            @PathVariable UUID userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) String movieTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<AdminReviewDto> reviewsPage = reviewService.getFilteredReviewsByUserId(
                userId, keyword, movieId, movieTitle, pageable
        );

        CustomPageDto<AdminReviewDto> customPage = CustomPageDto.<AdminReviewDto>builder()
                .content(reviewsPage.getContent())
                .currentPage(reviewsPage.getNumber())
                .totalPages(reviewsPage.getTotalPages())
                .totalElements(reviewsPage.getTotalElements())
                .build();

        return ResponseEntity.ok(customPage);
    }

    @PostMapping("/restore/{id}")
    public ResponseEntity<Void> restoreReview(@PathVariable("id") UUID id) {

        reviewService.restoreReview(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateReview(@PathVariable UUID id,
                                             @Valid @RequestBody EditReviewDto dto) {
        reviewService.updateReview(id, dto);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}




