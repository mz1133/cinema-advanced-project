package app.service;

import org.app.Application;
import org.app.actor.model.Actor;
import org.app.actor.repository.ActorRepository;
import org.app.exception.MovieNotFoundException;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.app.movie.repository.MovieRepository;
import org.app.movie.service.MovieService;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.app.web.dto.CreateMovieRequest;
import org.app.web.dto.EditMovieDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
class MovieServiceITest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();
        actorRepository.deleteAll();
        userRepository.deleteAll();

        movieRepository.flush();
        actorRepository.flush();
        userRepository.flush();
    }


    @Test
    void addMovie_shouldSaveMovieToDatabase() {

        Actor actor = createActor(
                "Tom",
                "Hanks"
        );

        User user = createUser(
                "john",
                Role.USER
        );

        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("Forrest Gump")
                .genres(new ArrayList<>(List.of(Genre.DRAMA)))
                .year(1994)
                .director("Robert Zemeckis")
                .posterUrl("https://example.com/poster.jpg")
                .countries(new ArrayList<>(List.of(Country.USA)))
                .description("A movie about Forrest Gump.")
                .studio("Paramount Pictures")
                .duration(142)
                .rating(8.8)
                .actorsIds(new ArrayList<>(List.of(actor.getId())))
                .build();

        movieService.addMovie(
                request,
                user.getUsername(),
                Role.USER
        );

        Page<Movie> movies =
                movieRepository.findAll(PageRequest.of(0, 10));

        assertEquals(1, movies.getTotalElements());

        Movie savedMovie = movies.getContent().get(0);

        assertNotNull(savedMovie.getId());
        assertEquals("Forrest Gump", savedMovie.getTitle());
        assertEquals(1994, savedMovie.getYear());
        assertEquals(142, savedMovie.getDurationMinutes());
        assertEquals("Robert Zemeckis", savedMovie.getDirector());
        assertEquals("Paramount Pictures", savedMovie.getStudio());
        assertEquals(8.8, savedMovie.getRating());

        assertFalse(savedMovie.isDeleted());

        assertEquals(
                "john",
                savedMovie.getPostedBy()
        );

        assertNotNull(savedMovie.getPublisher());

        assertEquals(
                user.getId(),
                savedMovie.getPublisher().getId()
        );

        assertEquals(
                1,
                savedMovie.getActors().size()
        );

        assertEquals(
                actor.getId(),
                savedMovie.getActors().get(0).getId()
        );
    }


    @Test
    void addMovie_whenAdmin_shouldSetPostedByToCineCatalog() {

        Actor actor = createActor(
                "Leonardo",
                "DiCaprio"
        );

        User admin = createUser(
                "admin",
                Role.ADMIN
        );

        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("Inception")
                .genres(new ArrayList<>(List.of(
                        Genre.ACTION,
                        Genre.SCIENCE_FICTION
                )))
                .year(2010)
                .countries(new ArrayList<>(List.of(Country.USA)))
                .studio("Warner Bros.")
                .duration(148)
                .actorsIds(new ArrayList<>(List.of(actor.getId())))
                .build();

        movieService.addMovie(
                request,
                admin.getUsername(),
                Role.ADMIN
        );

        Movie savedMovie =
                movieRepository.findAll().get(0);

        assertEquals(
                "cine-catalog",
                savedMovie.getPostedBy()
        );

        assertNotNull(savedMovie.getPublisher());

        assertEquals(
                admin.getId(),
                savedMovie.getPublisher().getId()
        );
    }


    @Test
    void getMovieById_shouldReturnMovie() {

        Movie movie = createMovie(
                "The Matrix",
                1999
        );

        Movie result = movieService.getMovieById(
                movie.getId(),
                "Movie not found"
        );

        assertNotNull(result);

        assertEquals(
                movie.getId(),
                result.getId()
        );

        assertEquals(
                "The Matrix",
                result.getTitle()
        );

        assertEquals(
                1999,
                result.getYear()
        );
    }


    @Test
    void getMovieById_whenMovieDoesNotExist_shouldThrowException() {

        UUID movieId = UUID.randomUUID();

        assertThrows(
                MovieNotFoundException.class,
                () -> movieService.getMovieById(
                        movieId,
                        "Movie not found"
                )
        );
    }


    @Test
    void getAllMoviesPageable_shouldReturnMovies() {

        createMovie("Movie One", 2000);
        createMovie("Movie Two", 2001);
        createMovie("Movie Three", 2002);

        Page<Movie> result =
                movieService.getAllMoviesPageable(
                        PageRequest.of(0, 10)
                );

        assertEquals(
                3,
                result.getTotalElements()
        );

        assertEquals(
                3,
                result.getContent().size()
        );
    }


    @Test
    void getMovieByKeyword_whenSearchTypeIsId_shouldFindMovie() {

        Movie movie = createMovie(
                "Interstellar",
                2014
        );

        Page<Movie> result =
                movieService.getMovieByKeyword(
                        movie.getId().toString(),
                        PageRequest.of(0, 10),
                        "id"
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                movie.getId(),
                result.getContent().get(0).getId()
        );
    }


    @Test
    void getMovieByKeyword_whenInvalidId_shouldReturnEmptyPage() {

        Page<Movie> result =
                movieService.getMovieByKeyword(
                        "not-a-valid-uuid",
                        PageRequest.of(0, 10),
                        "id"
                );

        assertTrue(result.isEmpty());
    }


    @Test
    void search_shouldFilterByYear() {

        createMovie(
                "Movie 2020",
                2020
        );

        createMovie(
                "Movie 2021",
                2021
        );

        createMovie(
                "Movie 2022",
                2022
        );

        Page<Movie> result = movieService.search(
                null,
                2021,
                null,
                null,
                "ALL",
                PageRequest.of(0, 10)
        );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "Movie 2021",
                result.getContent().get(0).getTitle()
        );
    }


    @Test
    void search_shouldFilterByGenre() {

        Movie actionMovie = createMovie(
                "Action Movie",
                2020,
                new ArrayList<>(List.of(Genre.ACTION)),
                new ArrayList<>(List.of(Country.USA))
        );

        createMovie(
                "Drama Movie",
                2020,
                new ArrayList<>(List.of(Genre.DRAMA)),
                new ArrayList<>(List.of(Country.USA))
        );

        Page<Movie> result = movieService.search(
                null,
                null,
                Genre.ACTION,
                null,
                "ALL",
                PageRequest.of(0, 10)
        );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                actionMovie.getId(),
                result.getContent().get(0).getId()
        );
    }


    @Test
    void search_shouldFilterByCountry() {

        Movie usaMovie = createMovie(
                "USA Movie",
                2020,
                new ArrayList<>(List.of(Genre.DRAMA)),
                new ArrayList<>(List.of(Country.USA))
        );

        createMovie(
                "French Movie",
                2020,
                new ArrayList<>(List.of(Genre.DRAMA)),
                new ArrayList<>(List.of(Country.FRANCE))
        );

        Page<Movie> result = movieService.search(
                null,
                null,
                null,
                Country.USA,
                "ALL",
                PageRequest.of(0, 10)
        );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                usaMovie.getId(),
                result.getContent().get(0).getId()
        );
    }


    @Test
    void search_shouldReturnOnlyNotDeletedMovies() {

        Movie activeMovie = createMovie(
                "Active Movie",
                2020
        );

        Movie deletedMovie = createMovie(
                "Deleted Movie",
                2021
        );

        deletedMovie.setDeleted(true);

        movieRepository.flush();

        Page<Movie> result = movieService.search(
                null,
                null,
                null,
                null,
                "ALL",
                PageRequest.of(0, 10)
        );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                activeMovie.getId(),
                result.getContent().get(0).getId()
        );
    }


    @Test
    void getMoviesByPublisher_shouldReturnOnlyPublisherMovies() {

        User publisher = createUser(
                "moviePublisher",
                Role.USER
        );

        User otherPublisher = createUser(
                "otherPublisher",
                Role.USER
        );

        Movie firstMovie = createMovieWithPublisher(
                "First Movie",
                publisher
        );

        Movie secondMovie = createMovieWithPublisher(
                "Second Movie",
                publisher
        );

        createMovieWithPublisher(
                "Other User Movie",
                otherPublisher
        );

        movieRepository.flush();

        Page<Movie> result =
                movieService.getMoviesByPublisher(
                        publisher.getId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(
                2,
                result.getTotalElements()
        );

        assertTrue(
                result.getContent()
                        .stream()
                        .allMatch(movie ->
                                movie.getPublisher() != null
                                        && movie.getPublisher()
                                        .getId()
                                        .equals(publisher.getId())
                        )
        );

        assertTrue(
                result.getContent()
                        .stream()
                        .anyMatch(movie ->
                                movie.getId()
                                        .equals(firstMovie.getId())
                        )
        );

        assertTrue(
                result.getContent()
                        .stream()
                        .anyMatch(movie ->
                                movie.getId()
                                        .equals(secondMovie.getId())
                        )
        );
    }


    @Test
    void getMovieEditDetails_shouldReturnMovieData() {

        Actor actor = createActor(
                "Brad",
                "Pitt"
        );

        Movie movie = Movie.builder()
                .title("Fight Club")
                .year(1999)
                .durationMinutes(139)
                .studio("Fox")
                .genre(new ArrayList<>(
                        List.of(Genre.DRAMA)
                ))
                .country(new ArrayList<>(
                        List.of(Country.USA)
                ))
                .actors(new ArrayList<>(
                        List.of(actor)
                ))
                .description("Fight Club description")
                .director("David Fincher")
                .posterUrl("poster.jpg")
                .rating(8.8)
                .deleted(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        movie = movieRepository.save(movie);

        movieRepository.flush();

        EditMovieDetails result =
                movieService.getMovieEditDetails(
                        movie.getId()
                );

        assertEquals(
                movie.getId(),
                result.getMovieId()
        );

        assertEquals(
                "Fight Club",
                result.getTitle()
        );

        assertEquals(
                1999,
                result.getYear()
        );

        assertEquals(
                "David Fincher",
                result.getDirector()
        );

        assertEquals(
                "poster.jpg",
                result.getPosterUrl()
        );

        assertEquals(
                "Fight Club description",
                result.getDescription()
        );

        assertEquals(
                "Fox",
                result.getStudio()
        );

        assertEquals(
                139,
                result.getDuration()
        );

        assertEquals(
                8.8,
                result.getRating()
        );

        assertEquals(
                List.of(actor.getId()),
                result.getActorsIds()
        );
    }


    @Test
    void getMovieOptions_shouldReturnGenresCountriesAndActors() {

        Actor actor = createActor(
                "Morgan",
                "Freeman"
        );

        var result =
                movieService.getMovieOptions();

        assertNotNull(result);

        assertNotNull(result.getGenres());

        assertNotNull(result.getCountries());

        assertNotNull(
                result.getActorsFullNameAndId()
        );

        assertEquals(
                Genre.values().length,
                result.getGenres().size()
        );

        assertEquals(
                Country.values().length,
                result.getCountries().size()
        );

        assertEquals(
                1,
                result.getActorsFullNameAndId().size()
        );

        assertEquals(
                "Morgan Freeman",
                result.getActorsFullNameAndId()
                        .get(0)
                        .getFullName()
        );

        assertEquals(
                actor.getId(),
                result.getActorsFullNameAndId()
                        .get(0)
                        .getId()
        );
    }


    private Actor createActor(
            String firstName,
            String lastName
    ) {

        Actor actor = Actor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .birthDate(
                        LocalDate.of(1980, 1, 1)
                )
                .age(45)
                .pictureUrl("actor.jpg")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return actorRepository.save(actor);
    }


    private User createUser(
            String username,
            Role role
    ) {

        User user = User.builder()
                .username(username)
                .password("password")
                .email(username + "@test.com")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .upDateOn(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }


    private Movie createMovie(
            String title,
            Integer year
    ) {

        return createMovie(
                title,
                year,
                new ArrayList<>(
                        List.of(Genre.DRAMA)
                ),
                new ArrayList<>(
                        List.of(Country.USA)
                )
        );
    }


    private Movie createMovie(
            String title,
            Integer year,
            List<Genre> genres,
            List<Country> countries
    ) {

        Movie movie = Movie.builder()
                .title(title)
                .year(year)
                .durationMinutes(120)
                .studio("Test Studio")
                .genre(new ArrayList<>(genres))
                .country(new ArrayList<>(countries))
                .description("Test description")
                .director("Test Director")
                .rating(8.0)
                .deleted(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return movieRepository.save(movie);
    }


    private Movie createMovieWithPublisher(
            String title,
            User publisher
    ) {

        Movie movie = Movie.builder()
                .title(title)
                .year(2020)
                .durationMinutes(120)
                .studio("Test Studio")
                .genre(new ArrayList<>(
                        List.of(Genre.DRAMA)
                ))
                .country(new ArrayList<>(
                        List.of(Country.USA)
                ))
                .description("Test description")
                .director("Test Director")
                .rating(8.0)
                .deleted(false)
                .publisher(publisher)
                .postedBy(publisher.getUsername())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return movieRepository.save(movie);
    }
}