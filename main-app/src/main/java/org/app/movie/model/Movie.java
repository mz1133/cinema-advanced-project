package org.app.movie.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.actor.model.Actor;
import org.app.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer durationMinutes;

    private String posterUrl;

    private Double rating;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Genre> genre = new ArrayList<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Country> country = new ArrayList<>();

    private String director;

    @ElementCollection
    private List<String> producers = new ArrayList<>();

    private String writerBy;

    @Column(nullable = false)
    private String studio;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Actor> actors = new ArrayList<>();

    private String postedBy;

    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    private User publisher;

    @NotNull
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

}
