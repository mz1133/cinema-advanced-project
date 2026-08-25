package app.specification;



import app.review.model.Review;
import app.util.SpecificationSearch;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;



import static org.assertj.core.api.Assertions.assertThat;

class SpecificationSearchUTest {

    @Test
    void givenNullUsername_whenHasUsername_thenReturnNull() {

        Specification<Review> specification =
                SpecificationSearch.hasUsername(null);

        assertThat(specification).isNotNull();

        assertThat(specification.toPredicate(null, null, null))
                .isNull();
    }

    @Test
    void givenEmptyUsername_whenHasUsername_thenReturnNull() {

        Specification<Review> specification =
                SpecificationSearch.hasUsername("");

        assertThat(specification).isNotNull();

        assertThat(specification.toPredicate(null, null, null))
                .isNull();
    }

    @Test
    void givenNullPublisherId_whenHasPublisherId_thenReturnNull() {

        Specification<Review> specification =
                SpecificationSearch.hasPublisherId(null);

        assertThat(specification).isNotNull();

        assertThat(specification.toPredicate(null, null, null))
                .isNull();
    }
}
