package app.util;


import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;


public class SpecificationSearch {


    public static <T> Specification<T> hasKeyword(String keyword) {

        return ((root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.isEmpty()) {
                return null;
            }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("content")),
                    "%" + keyword.toLowerCase() + "%");
        });
    }

    public static <T> Specification<T> hasMovieId(UUID movieId) {

        return (root, query, criteriaBuilder) -> {

            if (movieId == null) {
                return null;
            }

            return criteriaBuilder.equal(root.get("movieId"), movieId);
        };

    }

    public static <T> Specification<T> hasUsername(String publisherUsername) {

        return ((root, query, criteriaBuilder) -> {

            if (publisherUsername == null || publisherUsername.isEmpty()) {
                return null;
            }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("publisherUsername")),
                    "%" + publisherUsername.toLowerCase() + "%");
        });
    }

    public static <T> Specification<T> hasPublisherId(UUID publisherId) {
        return (root, query, criteriaBuilder) -> {
            if (publisherId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("publisherId"), publisherId);
        };
    }

    public static <T> Specification<T> hasMovieTitle(String movieTitle) {

        return ((root, query, criteriaBuilder) -> {

            if (movieTitle == null || movieTitle.isEmpty()) {
                return null;
            }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("movieTitle")),
                    "%" + movieTitle.toLowerCase() + "%");
        });
    }

    public static <T> Specification<T> isNotDeleted() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.isFalse(root.get("isDeleted"));
        };
    }

    public static <T> Specification<T> isDeletedByAdministrator() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.isFalse(root.get("isDeletedByAdministrator"));
        };
    }




}
