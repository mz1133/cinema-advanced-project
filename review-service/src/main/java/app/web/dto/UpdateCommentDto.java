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
public class UpdateCommentDto {

    @NotBlank
    private UUID commentId;

    @NotBlank
    private UUID publisherId;

    @NotBlank(message = "Symbol must be between 5 and 50 symbols")
    private String content;


}
