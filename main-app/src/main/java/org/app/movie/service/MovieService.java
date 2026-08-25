package org.app.movie.service;



import lombok.extern.slf4j.Slf4j;
import org.app.actor.model.Actor;
import org.app.actor.repository.ActorRepository;
import org.app.actor.service.ActorService;
import org.app.exception.MovieNotFoundException;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.app.movie.repository.MovieRepository;
import org.app.movie.specification.MovieSpecification;

import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.dto.CreateMovieRequest;
import org.app.web.dto.EditMovieDetails;
import org.app.web.dto.MovieOptionsDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class MovieService {

    private static final String MESSAGE_POSTED_BY_CINE_CATALOG = "cine-catalog";
    private static final String ERROR_MESSAGE_MOVIE_NOT_FOUND_TO_DELETE = "The movie you are trying to delete could not be found.";
    private static final String ERROR_MESSAGE_RESTORE_MOVIE_NOT_FOUND = "The movie you are trying to restore could not be found.";
    private static final String ERROR_MESSAGE_MOVIE_NOT_FOUND = "Movie not found.";
    private static final LocalDateTime LOCAL_DATE_TIME_NOW = LocalDateTime.now(ZoneId.systemDefault());
    private static final String SEARCH_FILTER_BU_TITLE = "title";

    private final ActorService actorService;
    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final UserService userService;


    public MovieService(ActorService actorService, MovieRepository movieRepository, ActorRepository actorRepository, UserService userService) {
        this.actorService = actorService;
        this.movieRepository = movieRepository;
        this.actorRepository = actorRepository;
        this.userService = userService;

    }

    @CacheEvict(value = "movies", allEntries = true)
    public void addMovie(CreateMovieRequest createMovieRequest, String username, Role role) {

        User user = userService.getUserByUsername(username);

        Movie movie = buildMovie(createMovieRequest, role, user);

        saveMovie(movie);

        log.info("Movie with id: { %s }, with name: {%s} has been added".formatted(movie.getId(), movie.getTitle()));
    }

    public Page<Movie> getMovieByKeyword(String keyword, Pageable pageable, String searchType) {

        if (searchType.trim().equalsIgnoreCase(SEARCH_FILTER_BU_TITLE)) {
            return movieRepository.getFirstByTitle(keyword, pageable);

        } else if (searchType.trim().equalsIgnoreCase("id")) {

            try {
                UUID movieId = UUID.fromString(keyword);

                return movieRepository.getFirstById(movieId, pageable);

            } catch (IllegalArgumentException e) {

                log.error("ERROR: " + e.getMessage());
            }
        }

        return Page.empty(pageable);
    }

    @Cacheable(value = "movies")
    public Page<Movie> getAllMoviesPageable(Pageable pageable) {

        return movieRepository.findAll(pageable);
    }

    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(UUID movieToDeleteId) {

        Movie movie = getMovieById(movieToDeleteId, ERROR_MESSAGE_MOVIE_NOT_FOUND_TO_DELETE);
        movie.setDeleted(true);

        saveMovie(movie);

        log.info("Movie with id: { %s }, has been deleted".formatted(movie.getId()));
    }

    public void restoreMovie(UUID movieToRestoreId) {

        Movie movie = getMovieById(movieToRestoreId, ERROR_MESSAGE_RESTORE_MOVIE_NOT_FOUND);
        movie.setDeleted(false);

        saveMovie(movie);

        log.info("Movie with id: { %s }, has been restored".formatted(movie.getId()));
    }

    public Movie getMovieById(UUID movieId, String message) {

        Optional<Movie> movie = movieRepository.findById(movieId);

        if (movie.isEmpty()) {
            throw new MovieNotFoundException(message);
        }

        return movie.get();

    }

    public Page<Movie> search(String keyword,
                              Integer year,
                              Genre genre,
                              Country country,
                              String sort,
                              Pageable pageable) {

        Specification<Movie> specification = MovieSpecification.isNotDeleted()
                .and(MovieSpecification.hasKeyword(keyword))
                .and(MovieSpecification.hasYear(year))
                .and(MovieSpecification.hasGenre(genre))
                .and(MovieSpecification.hasCountry(country));

        Pageable sortPageable = createPageable(pageable, sort);

        return movieRepository.findAll(specification, sortPageable);

    }

    public Movie getMovie(UUID movieId) {

        Optional<Movie> movie = Optional.of(movieRepository
                .findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(ERROR_MESSAGE_MOVIE_NOT_FOUND)));

        return movie.get();
    }

    public EditMovieDetails getMovieEditDetails(UUID id) {

        return getEditMovieDetails(id);
    }

    public void editMovie(EditMovieDetails editMovieDetails, UUID movieId) {

        Movie movieToEdit = buildEditMovie(editMovieDetails, movieId);

        saveMovie(movieToEdit);

        log.info("Movie with id: { %s }, has been edited".formatted(movieToEdit.getId()));
    }

    public MovieOptionsDto getMovieOptions() {

        return MovieOptionsDto.builder()
                .genres(List.of(Genre.values()))
                .countries(List.of(Country.values()))
                .actorsFullNameAndId(actorService.getAllActorsFullNameAndId())
                .build();

    }

    public Page<Movie> getMoviesByPublisher(UUID id, Pageable pageable) {

        return movieRepository.findByPublisherId(id, pageable);
    }

    private EditMovieDetails getEditMovieDetails(UUID id) {
        Movie movie = getMovieById(id, ERROR_MESSAGE_MOVIE_NOT_FOUND);

        return EditMovieDetails.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .genres(movie.getGenre())
                .year(movie.getYear())
                .director(movie.getDirector())
                .posterUrl(movie.getPosterUrl())
                .countries(movie.getCountry())
                .description(movie.getDescription())
                .studio(movie.getStudio())
                .duration(movie.getDurationMinutes())
                .rating(movie.getRating())
                .actorsIds(movie.getActors()
                        .stream()
                        .map(Actor::getId)
                        .toList())
                .build();
    }

    @NonNull
    private Movie buildEditMovie(EditMovieDetails editMovieDetails, UUID movieId) {

        Movie movieToEdit = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(ERROR_MESSAGE_MOVIE_NOT_FOUND));

        movieToEdit.setTitle(editMovieDetails.getTitle());
        movieToEdit.setGenre(editMovieDetails.getGenres());
        movieToEdit.setYear(editMovieDetails.getYear());
        movieToEdit.setDirector(editMovieDetails.getDirector());
        movieToEdit.setPosterUrl(editMovieDetails.getPosterUrl());
        movieToEdit.setCountry(editMovieDetails.getCountries());
        movieToEdit.setDescription(editMovieDetails.getDescription());
        movieToEdit.setStudio(editMovieDetails.getStudio());
        movieToEdit.setDurationMinutes(editMovieDetails.getDuration());
        movieToEdit.setRating(editMovieDetails.getRating());

        List<Actor> actors = actorRepository.findAllById(editMovieDetails.getActorsIds());

        movieToEdit.setActors(actors);
        movieToEdit.setUpdatedOn(LOCAL_DATE_TIME_NOW);

        return movieToEdit;
    }

    private void saveMovie(Movie movie) {
        movieRepository.save(movie);
    }

    private Pageable createPageable(Pageable pageable, String sort) {

        Sort sorting;

        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("ALL")) {

            sorting = Sort.unsorted();

        } else {

            sorting = switch (sort) {

                case "newest" -> Sort.by("createdOn").descending();
                case "rating_desc" -> Sort.by("rating").descending();
                case "years_desc" -> Sort.by("year").descending();
                case SEARCH_FILTER_BU_TITLE -> Sort.by(SEARCH_FILTER_BU_TITLE).ascending();
                default -> Sort.unsorted();
            };

        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting);
    }

    @NonNull
    private Movie buildMovie(CreateMovieRequest createMovieRequest, Role role, User user) {

        return Movie.builder()

                .title(createMovieRequest.getTitle())
                .genre(createMovieRequest.getGenres())
                .year(createMovieRequest.getYear())
                .director(createMovieRequest.getDirector())
                .posterUrl(createMovieRequest.getPosterUrl())
                .country(createMovieRequest.getCountries())
                .description(createMovieRequest.getDescription())
                .studio(createMovieRequest.getStudio())
                .rating(createMovieRequest.getRating())
                .deleted(false)
                .durationMinutes(createMovieRequest.getDuration())
                .actors(actorRepository.findAllById(createMovieRequest.getActorsIds()))
                .postedBy(getPostedByName(user.getUsername(), role))
                .publisher(user)
                .createdOn(LOCAL_DATE_TIME_NOW)
                .updatedOn(LOCAL_DATE_TIME_NOW)

                .build();
    }

    private String getPostedByName(String username, Role role) {

        if (role == Role.ADMIN || role == Role.SUPER_ADMIN) {

            return MESSAGE_POSTED_BY_CINE_CATALOG;
        }

        return username;
    }
}
