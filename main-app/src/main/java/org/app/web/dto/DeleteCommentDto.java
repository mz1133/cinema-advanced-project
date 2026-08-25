package org.app.web.dto;

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
public class DeleteCommentDto {

    @NotNull(message = "Review ID cannot be null!")
    private UUID reviewId;

    @NotNull(message = "Publisher ID cannot be null!")
    private UUID publisherId;

    @NotNull(message = "Comment ID cannot be null!")
    private UUID commentId;

    @NotBlank(message = "Comment reason is required!")
    @Size(min = 20, max = 200, message = "Comment reason must be between 20 and 200 characters!")
    private String reason;
}
