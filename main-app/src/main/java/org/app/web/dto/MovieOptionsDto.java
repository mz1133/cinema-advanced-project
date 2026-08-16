package org.app.web.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;


import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieOptionsDto {

    private List<Genre> genres = new ArrayList<>();

    private List<Country> countries = new ArrayList<>();

    private List<ActorForm> actorsFullNameAndId = new ArrayList<>();
}
