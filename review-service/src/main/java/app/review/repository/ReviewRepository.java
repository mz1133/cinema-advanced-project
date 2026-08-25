package app.review.repository;

import app.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;



import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {

    Page<Review> findAllByMovieIdAndIsDeletedFalseOrderByCreatedOnDesc(UUID id, Pageable pageable);



    Page<Review> findAll(Specification<Review> spec, Pageable pageable);


}
