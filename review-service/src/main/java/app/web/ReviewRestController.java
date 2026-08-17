package app.web;


import app.review.service.ReviewService;
import app.web.dto.CreateReviewDto;
import app.web.dto.UpdateReviewDto;
import app.web.dto.ViewReviewsAndCommentsDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("api/reviews")
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

    @PostMapping("/edit")
    public ResponseEntity<Void> updateReview(@Valid @RequestBody UpdateReviewDto updateReviewDto) {

        reviewService.updateReview(updateReviewDto);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {

        reviewService.deleteReview(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<Page<ViewReviewsAndCommentsDto>> getAllReviewsAndComments(@PathVariable UUID id,
                                                                                    Pageable pageable) {

       Page<ViewReviewsAndCommentsDto> reviewsAndComments = reviewService.getAllActivesReviewsAndComments(id, pageable);

       return ResponseEntity.status(HttpStatus.OK).body(reviewsAndComments);

    }
}
