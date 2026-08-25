package org.app.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditMovieDetails {

    private UUID movieId;

    @NotBlank(message = "Title cannot be empty.")
    private String title;

    @NotEmpty(message = "Genre cannot be empty.")
    private List<Genre> genres = new ArrayList<>();

    @NotNull(message = "Year cannot be empty.")
    @Min(value = 1900, message = "Year can't be less than 1900 year.")
    @Max(value = 2100, message = "Year can't be high than 2100 year.")
    private Integer year;

    private String director;

    private String posterUrl;

    @NotEmpty(message = "County cannot be empty.")
    private List<Country> countries = new ArrayList<>();

    private String description;

    @NotBlank(message = "Studio cannot be empty.")
    private String studio;

    @NotNull(message = "Duration cannot be empty.")
    private Integer duration;

    private Double rating;

    @NotEmpty(message = "Actor cannot be empty.")
    private List<UUID> actorsIds = new ArrayList<>();

}
