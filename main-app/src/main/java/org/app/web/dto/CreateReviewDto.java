package org.app.web.dto;


import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewDto {

    private UUID publisherId;

    @NotNull
    private UUID movieId;

    @Column(nullable = false)
    private String movieTitle;

    private String publisherUsername;

    @NotBlank(message = "The review cannot be empty")
    @Size(min = 20, max = 1000, message = "The review must be between 20 and 1000 sybols")
    private String content;

    @NotNull(message = "The rating must be between 1 and 10")
    @Min(value = 1, message = "Must be higher or equal than 1")
    @Max(value = 10, message = "Must be lower or equal than 10")
    private Integer userRating;

    private boolean isDeleted;
}
