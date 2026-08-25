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
public class ReviewDeleteNotification {

    @NotNull
    private UUID reviewId;

    @NotNull
    private UUID publisherId;

    @NotBlank(message = "Review eason cannot be blank")
    @Size(min = 20, max = 200, message = "Review reason must be between 20 and 200 characters!")
    private String reason;
}
