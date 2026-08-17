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
public class ViewReviewsAndCommentsDto {

    private String content;

    private UUID reviewId;

    private String publisherUsername;

    private Integer userRating;

    private LocalDateTime  createdOn;

    private List<ViewCommentsDto> comments;

}
