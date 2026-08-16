package org.app.movie.repository;

import org.app.movie.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID>, JpaSpecificationExecutor<Movie> {

    Page<Movie> getFirstById(UUID keyword, Pageable pageable);

    Page<Movie> getFirstByTitle(String keyword, Pageable pageable);


    Page<Movie> findByPublisherId(UUID id, Pageable pageable);
}
