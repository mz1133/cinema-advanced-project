package app.review.repository;

import app.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findAllByMovieIdAndIsDeletedFalse(UUID id, Pageable pageable);

   Optional<Review> findByIdAndIsDeletedFalse(UUID id);
}
