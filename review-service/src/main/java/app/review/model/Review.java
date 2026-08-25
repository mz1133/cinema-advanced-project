package app.review.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    @Size(min = 20, max = 1000)
    private String content;

    @Column(nullable = false)
    private UUID movieId;

    @Column(nullable = false)
    private String movieTitle;

    @Column(nullable = false)
    private UUID publisherId;

    @Column(nullable = false)
    private String publisherUsername;

    private Integer userRating;

    private boolean isDeleted;

    private boolean isDeletedByAdministrator = false;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;


}
