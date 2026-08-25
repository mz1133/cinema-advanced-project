package app.service;

import org.app.actor.model.Actor;
import org.app.actor.repository.ActorRepository;
import org.app.actor.service.ActorService;
import org.app.exception.MovieNotFoundException;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.app.movie.repository.MovieRepository;
import org.app.movie.service.MovieService;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.dto.ActorForm;
import org.app.web.dto.CreateMovieRequest;
import org.app.web.dto.EditMovieDetails;
import org.app.web.dto.MovieOptionsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceUTest {

    @Mock
    private ActorService actorService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private UserService userService;

    @Mock
    private User user;

    @InjectMocks
    private MovieService movieService;

    @Test
    void addMovie_happyPath() {

        String username = "john";
        Role role = Role.ADMIN;

        UUID actorId = UUID.randomUUID();

        Actor actor = Actor.builder()
                .id(actorId)
                .build();

        List<UUID> actorIds = List.of(actorId);
        List<Actor> actors = List.of(actor);

        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("The Matrix")
                .genres(List.of(Genre.ACTION))
                .year(1999)
                .director("Lana Wachowski")
                .posterUrl("poster.jpg")
                .countries(List.of(Country.USA))
                .description("Movie description")
                .studio("Warner Bros.")
                .duration(136)
                .rating(8.7)
                .actorsIds(actorIds)
                .build();

        when(userService.getUserByUsername(username))
                .thenReturn(user);

        when(user.getUsername())
                .thenReturn(username);

        when(actorRepository.findAllById(actorIds))
                .thenReturn(actors);

        movieService.addMovie(request, username, role);

        ArgumentCaptor<Movie> movieCaptor =
                ArgumentCaptor.forClass(Movie.class);

        verify(movieRepository).save(movieCaptor.capture());

        Movie savedMovie = movieCaptor.getValue();

        assertNotNull(savedMovie);
        assertEquals("The Matrix", savedMovie.getTitle());
        assertEquals(List.of(Genre.ACTION), savedMovie.getGenre());
        assertEquals(1999, savedMovie.getYear());
        assertEquals("Lana Wachowski", savedMovie.getDirector());
        assertEquals("poster.jpg", savedMovie.getPosterUrl());
        assertEquals(List.of(Country.USA), savedMovie.getCountry());
        assertEquals("Movie description", savedMovie.getDescription());
        assertEquals("Warner Bros.", savedMovie.getStudio());
        assertEquals(136, savedMovie.getDurationMinutes());
        assertEquals(8.7, savedMovie.getRating());
        assertEquals(actors, savedMovie.getActors());

        assertFalse(savedMovie.isDeleted());
        assertEquals("cine-catalog", savedMovie.getPostedBy());
        assertSame(user, savedMovie.getPublisher());

        assertNotNull(savedMovie.getCreatedOn());
        assertNotNull(savedMovie.getUpdatedOn());
        assertEquals(savedMovie.getCreatedOn(), savedMovie.getUpdatedOn());

        verify(userService).getUserByUsername(username);
        verify(actorRepository).findAllById(actorIds);
    }

    @Test
    void addMovie_superAdminRole_setsPostedByToCineCatalog() {

        String username = "admin";
        Role role = Role.SUPER_ADMIN;

        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("Movie")
                .genres(List.of(Genre.ACTION))
                .year(2020)
                .countries(List.of(Country.USA))
                .studio("Studio")
                .duration(120)
                .actorsIds(List.of(UUID.randomUUID()))
                .build();

        when(userService.getUserByUsername(username))
                .thenReturn(user);

        when(user.getUsername())
                .thenReturn(username);

        when(actorRepository.findAllById(request.getActorsIds()))
                .thenReturn(List.of());

        movieService.addMovie(request, username, role);

        ArgumentCaptor<Movie> movieCaptor =
                ArgumentCaptor.forClass(Movie.class);

        verify(movieRepository).save(movieCaptor.capture());

        assertEquals(
                "cine-catalog",
                movieCaptor.getValue().getPostedBy()
        );
    }

    @Test
    void addMovie_nonAdminRole_setsPostedByToUsername() {

        String username = "john";

        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("Movie")
                .genres(List.of(Genre.ACTION))
                .year(2020)
                .countries(List.of(Country.USA))
                .studio("Studio")
                .duration(120)
                .actorsIds(List.of(UUID.randomUUID()))
                .build();

        when(userService.getUserByUsername(username))
                .thenReturn(user);

        when(user.getUsername())
                .thenReturn(username);

        when(actorRepository.findAllById(request.getActorsIds()))
                .thenReturn(List.of());

        movieService.addMovie(request, username, Role.USER);

        ArgumentCaptor<Movie> movieCaptor =
                ArgumentCaptor.forClass(Movie.class);

        verify(movieRepository).save(movieCaptor.capture());

        assertEquals(
                username,
                movieCaptor.getValue().getPostedBy()
        );
    }

    @Test
    void getMovieByKeyword_searchByTitle_returnsMovies() {

        String keyword = "matrix";

        Pageable pageable = PageRequest.of(0, 10);

        Movie movie = Movie.builder()
                .title("The Matrix")
                .build();

        Page<Movie> expectedPage =
                new PageImpl<>(List.of(movie), pageable, 1);

        when(movieRepository.getFirstByTitle(keyword, pageable))
                .thenReturn(expectedPage);

        Page<Movie> result =
                movieService.getMovieByKeyword(
                        keyword,
                        pageable,
                        "title"
                );

        assertSame(expectedPage, result);

        verify(movieRepository)
                .getFirstByTitle(keyword, pageable);

        verify(movieRepository, never())
                .getFirstById(any(), any());
    }

    @Test
    void getMovieByKeyword_searchByTitle_caseInsensitive_returnsMovies() {

        String keyword = "matrix";

        Pageable pageable = PageRequest.of(0, 10);

        Page<Movie> expectedPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(movieRepository.getFirstByTitle(keyword, pageable))
                .thenReturn(expectedPage);

        Page<Movie> result =
                movieService.getMovieByKeyword(
                        keyword,
                        pageable,
                        " TITLE "
                );

        assertSame(expectedPage, result);

        verify(movieRepository)
                .getFirstByTitle(keyword, pageable);
    }

    @Test
    void getMovieByKeyword_searchById_returnsMovies() {

        UUID movieId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Movie> expectedPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(movieRepository.getFirstById(movieId, pageable))
                .thenReturn(expectedPage);

        Page<Movie> result =
                movieService.getMovieByKeyword(
                        movieId.toString(),
                        pageable,
                        "id"
                );

        assertSame(expectedPage, result);

        verify(movieRepository)
                .getFirstById(movieId, pageable);
    }

    @Test
    void getMovieByKeyword_invalidId_returnsEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Movie> result =
                movieService.getMovieByKeyword(
                        "invalid-uuid",
                        pageable,
                        "id"
                );

        assertTrue(result.isEmpty());

        verify(movieRepository, never())
                .getFirstById(any(), any());

        verify(movieRepository, never())
                .getFirstByTitle(any(), any());
    }

    @Test
    void getMovieByKeyword_unknownSearchType_returnsEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Movie> result =
                movieService.getMovieByKeyword(
                        "matrix",
                        pageable,
                        "director"
                );

        assertTrue(result.isEmpty());

        verify(movieRepository, never())
                .getFirstByTitle(any(), any());

        verify(movieRepository, never())
                .getFirstById(any(), any());
    }

    @Test
    void getAllMoviesPageable_happyPath() {

        Pageable pageable = PageRequest.of(0, 10);

        Movie movie = Movie.builder()
                .title("The Matrix")
                .build();

        Page<Movie> expectedPage =
                new PageImpl<>(List.of(movie), pageable, 1);

        when(movieRepository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<Movie> result =
                movieService.getAllMoviesPageable(pageable);

        assertSame(expectedPage, result);

        verify(movieRepository).findAll(pageable);
    }

    @Test
    void deleteMovie_happyPath_setsDeletedAndSavesMovie() {

        UUID movieId = UUID.randomUUID();

        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Matrix")
                .deleted(false)
                .build();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.of(movie));

        movieService.deleteMovie(movieId);

        assertTrue(movie.isDeleted());

        verify(movieRepository).findById(movieId);
        verify(movieRepository).save(movie);
    }

    @Test
    void deleteMovie_movieDoesNotExist_throwsException() {

        UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.empty());

        assertThrows(
                MovieNotFoundException.class,
                () -> movieService.deleteMovie(movieId)
        );

        verify(movieRepository).findById(movieId);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void restoreMovie_happyPath_setsDeletedToFalseAndSavesMovie() {

        UUID movieId = UUID.randomUUID();

        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Matrix")
                .deleted(true)
                .build();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.of(movie));

        movieService.restoreMovie(movieId);

        assertFalse(movie.isDeleted());

        verify(movieRepository).findById(movieId);
        verify(movieRepository).save(movie);
    }

    @Test
    void restoreMovie_movieDoesNotExist_throwsException() {

        UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.empty());

        assertThrows(
                MovieNotFoundException.class,
                () -> movieService.restoreMovie(movieId)
        );

        verify(movieRepository).findById(movieId);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void getMovieById_movieExists_returnsMovie() {

        UUID movieId = UUID.randomUUID();

        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Matrix")
                .build();

        String message = "Movie not found.";

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.of(movie));

        Movie result =
                movieService.getMovieById(movieId, message);

        assertSame(movie, result);

        verify(movieRepository).findById(movieId);
    }

    @Test
    void getMovieById_movieDoesNotExist_throwsException() {

        UUID movieId = UUID.randomUUID();

        String message = "Movie not found.";

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.empty());

        MovieNotFoundException exception =
                assertThrows(
                        MovieNotFoundException.class,
                        () -> movieService.getMovieById(movieId, message)
                );

        assertEquals(message, exception.getMessage());

        verify(movieRepository).findById(movieId);
    }

    @Test
    void getMovie_movieExists_returnsMovie() {

        UUID movieId = UUID.randomUUID();

        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Matrix")
                .build();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.of(movie));

        Movie result = movieService.getMovie(movieId);

        assertSame(movie, result);

        verify(movieRepository).findById(movieId);
    }

    @Test
    void getMovie_movieDoesNotExist_throwsException() {

        UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.empty());

        MovieNotFoundException exception =
                assertThrows(
                        MovieNotFoundException.class,
                        () -> movieService.getMovie(movieId)
                );

        assertEquals(
                "Movie not found.",
                exception.getMessage()
        );

        verify(movieRepository).findById(movieId);
    }

    @Test
    void search_happyPath_returnsMovies() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Movie> expectedPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(movieRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Movie> result = movieService.search(
                "matrix",
                1999,
                null,
                null,
                "newest",
                pageable
        );

        assertSame(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(movieRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        Pageable usedPageable = pageableCaptor.getValue();

        assertEquals(0, usedPageable.getPageNumber());
        assertEquals(10, usedPageable.getPageSize());
        assertEquals(
                Sort.by("createdOn").descending(),
                usedPageable.getSort()
        );
    }

    @Test
    void search_sortRatingDescending_usesRatingDescending() {

        Pageable pageable = PageRequest.of(1, 20);

        when(movieRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        movieService.search(
                null,
                null,
                null,
                null,
                "rating_desc",
                pageable
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(movieRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        assertEquals(
                Sort.by("rating").descending(),
                pageableCaptor.getValue().getSort()
        );
    }

    @Test
    void search_sortYearsDescending_usesYearDescending() {

        Pageable pageable = PageRequest.of(0, 10);

        when(movieRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        movieService.search(
                null,
                null,
                null,
                null,
                "years_desc",
                pageable
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(movieRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        assertEquals(
                Sort.by("year").descending(),
                pageableCaptor.getValue().getSort()
        );
    }

    @Test
    void search_sortTitleAscending_usesTitleAscending() {

        Pageable pageable = PageRequest.of(0, 10);

        when(movieRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        movieService.search(
                null,
                null,
                null,
                null,
                "title",
                pageable
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(movieRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        assertEquals(
                Sort.by("title").ascending(),
                pageableCaptor.getValue().getSort()
        );
    }

    @Test
    void search_sortAll_usesUnsortedPageable() {

        Pageable pageable = PageRequest.of(0, 10);

        when(movieRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        movieService.search(
                null,
                null,
                null,
                null,
                "ALL",
                pageable
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(movieRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        assertEquals(
                Sort.unsorted(),
                pageableCaptor.getValue().getSort()
        );
    }

    @Test
    void search_nullSort_usesUnsortedPageable() {

        Pageable pageable = PageRequest.of(0, 10);

        when(movieRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        movieService.search(
                null,
                null,
                null,
                null,
                null,
                pageable
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(movieRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        assertEquals(
                Sort.unsorted(),
                pageableCaptor.getValue().getSort()
        );
    }

    @Test
    void getMovieEditDetails_movieExists_returnsMovieDetails() {

        UUID movieId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Actor actor = Actor.builder()
                .id(actorId)
                .build();

        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Matrix")
                .genre(List.of(Genre.ACTION))
                .year(1999)
                .director("Lana Wachowski")
                .posterUrl("poster.jpg")
                .country(List.of(Country.USA))
                .description("Description")
                .studio("Warner Bros.")
                .durationMinutes(136)
                .rating(8.7)
                .actors(List.of(actor))
                .build();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.of(movie));

        EditMovieDetails result =
                movieService.getMovieEditDetails(movieId);

        assertNotNull(result);
        assertEquals(movieId, result.getMovieId());
        assertEquals("The Matrix", result.getTitle());
        assertEquals(List.of(Genre.ACTION), result.getGenres());
        assertEquals(1999, result.getYear());
        assertEquals("Lana Wachowski", result.getDirector());
        assertEquals("poster.jpg", result.getPosterUrl());
        assertEquals(List.of(Country.USA), result.getCountries());
        assertEquals("Description", result.getDescription());
        assertEquals("Warner Bros.", result.getStudio());
        assertEquals(136, result.getDuration());
        assertEquals(8.7, result.getRating());
        assertEquals(List.of(actorId), result.getActorsIds());

        verify(movieRepository).findById(movieId);
    }

    @Test
    void getMovieEditDetails_movieDoesNotExist_throwsException() {

        UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.empty());

        assertThrows(
                MovieNotFoundException.class,
                () -> movieService.getMovieEditDetails(movieId)
        );

        verify(movieRepository).findById(movieId);
    }

    @Test
    void editMovie_happyPath_updatesMovieAndSaves() {

        UUID movieId = UUID.randomUUID();

        UUID actorId = UUID.randomUUID();

        Actor actor = Actor.builder()
                .id(actorId)
                .build();

        Movie movie = Movie.builder()
                .id(movieId)
                .title("Old title")
                .build();

        List<UUID> actorIds = List.of(actorId);
        List<Actor> actors = List.of(actor);

        EditMovieDetails details = EditMovieDetails.builder()
                .title("New title")
                .genres(List.of(Genre.ACTION))
                .year(2020)
                .director("New Director")
                .posterUrl("new-poster.jpg")
                .countries(List.of(Country.USA))
                .description("New description")
                .studio("New Studio")
                .duration(120)
                .rating(9.1)
                .actorsIds(actorIds)
                .build();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.of(movie));

        when(actorRepository.findAllById(actorIds))
                .thenReturn(actors);

        movieService.editMovie(details, movieId);

        assertEquals("New title", movie.getTitle());
        assertEquals(List.of(Genre.ACTION), movie.getGenre());
        assertEquals(2020, movie.getYear());
        assertEquals("New Director", movie.getDirector());
        assertEquals("new-poster.jpg", movie.getPosterUrl());
        assertEquals(List.of(Country.USA), movie.getCountry());
        assertEquals("New description", movie.getDescription());
        assertEquals("New Studio", movie.getStudio());
        assertEquals(120, movie.getDurationMinutes());
        assertEquals(9.1, movie.getRating());
        assertEquals(actors, movie.getActors());

        assertNotNull(movie.getUpdatedOn());

        verify(movieRepository).findById(movieId);
        verify(actorRepository).findAllById(actorIds);
        verify(movieRepository).save(movie);
    }

    @Test
    void editMovie_movieDoesNotExist_throwsException() {

        UUID movieId = UUID.randomUUID();

        EditMovieDetails details = EditMovieDetails.builder()
                .title("New title")
                .build();

        when(movieRepository.findById(movieId))
                .thenReturn(Optional.empty());

        assertThrows(
                MovieNotFoundException.class,
                () -> movieService.editMovie(details, movieId)
        );

        verify(movieRepository).findById(movieId);

        verify(actorRepository, never())
                .findAllById(any());

        verify(movieRepository, never())
                .save(any(Movie.class));
    }

    @Test
    void getMovieOptions_happyPath_returnsOptions() {

        List<ActorForm> actors = List.of(
                new ActorForm(),
                new ActorForm()
        );

        when(actorService.getAllActorsFullNameAndId())
                .thenReturn(actors);

        MovieOptionsDto result =
                movieService.getMovieOptions();

        assertNotNull(result);

        assertEquals(
                List.of(Genre.values()),
                result.getGenres()
        );

        assertEquals(
                List.of(Country.values()),
                result.getCountries()
        );

        assertEquals(
                actors,
                result.getActorsFullNameAndId()
        );

        verify(actorService)
                .getAllActorsFullNameAndId();
    }

    @Test
    void getMoviesByPublisher_happyPath_returnsMovies() {

        UUID publisherId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);

        Movie movie = Movie.builder()
                .title("The Matrix")
                .build();

        Page<Movie> expectedPage =
                new PageImpl<>(List.of(movie), pageable, 1);

        when(movieRepository.findByPublisherId(
                publisherId,
                pageable
        )).thenReturn(expectedPage);

        Page<Movie> result =
                movieService.getMoviesByPublisher(
                        publisherId,
                        pageable
                );

        assertSame(expectedPage, result);

        verify(movieRepository)
                .findByPublisherId(publisherId, pageable);
    }
}