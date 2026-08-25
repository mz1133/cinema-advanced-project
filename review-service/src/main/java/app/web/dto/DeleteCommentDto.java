package app.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCommentDto {

    @NotNull
    private UUID reviewId;
    @NotNull
    private UUID publisherId;
    @NotNull
    private UUID commentId;
    @NotNull

    @NotNull
    private String reason;
}
