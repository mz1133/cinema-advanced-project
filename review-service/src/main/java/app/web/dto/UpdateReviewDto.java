package app.web.dto;

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
public class UpdateReviewDto {

    @NotBlank
    private UUID reviewId;

    @NotBlank(message = "Symbol must be between 20 and 200 symbols")
    private String content;

    private Integer rating;

}
