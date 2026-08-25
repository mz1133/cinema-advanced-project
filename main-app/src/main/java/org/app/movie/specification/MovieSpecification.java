package org.app.movie.specification;

import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.springframework.data.jpa.domain.Specification;


public class MovieSpecification {


    public static  Specification<Movie> hasKeyword(String keyword) {

        return ((root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.isEmpty()) {
                return null;
            }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                    "%" + keyword.toLowerCase() + "%");
        });
    }



    public static Specification<Movie> hasYear(Integer year) {

        return (root, query, criteriaBuilder) -> {

            if (year == null) {
                return null;
            }

            return criteriaBuilder.equal(root.get("year"), year);
        };

    }

    public static Specification<Movie> hasGenre(Genre genre) {

        return (root, query, cb) -> {

            if (genre == null) {
                return null;
            }

            query.distinct(true);

            return cb.isMember(genre, root.get("genre")

            );
        };
    }

    public static Specification<Movie> hasCountry(Country country) {

        return (root, query, cb) -> {

            if (country == null) {
                return null;
            }

            query.distinct(true);

            return cb.isMember(country, root.get("country")

            );
        };

    }

    public static Specification<Movie> isNotDeleted() {

        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));

    }


}
