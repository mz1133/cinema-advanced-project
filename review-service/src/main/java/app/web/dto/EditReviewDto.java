package app.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditReviewDto {

    @Min(value = 1, message = "Rating must be at least 1!")
    @Max(value = 10, message = "Rating must be at most 10!")
    private Integer rating;

    @NotBlank(message = "Content cannot be blank!")
    @Size(min = 20, max = 1000, message = "Content must be between 20 and 1000 characters!")
    private String content;

}
