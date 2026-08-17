package app.web.dto;


import app.review.model.Review;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDto {

    @NotBlank
    private UUID publisherId;

    @NotBlank
    private String publisherUsername;

    @NotBlank(message = "Symbol must be between 5 and 50 symbols")
    private String content;

    @NotBlank
    private Review review;

    private boolean isDeleted;
}
