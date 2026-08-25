package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewDto {

    private UUID reviewId;

    private UUID movieId;

    private Integer rating;

    private boolean deleted;

    private String content;

    private String publisherUsername;

    private UUID publisherId;

    private String movieTitle;

    private Integer userRating;

    private LocalDateTime createdOn;

    private List<AdminCommentDto> comments;
}
