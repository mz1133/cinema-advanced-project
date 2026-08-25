package app.web.dto;


import app.review.model.Review;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private UUID publisherId;

    private String publisherUsername;

    @NotBlank(message = "Cannot be empty")
    @Size(min = 5, max = 300)
    private String content;

    @NotNull
    private UUID reviewId;

    private boolean isDeleted;
}
