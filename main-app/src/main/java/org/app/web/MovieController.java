package org.app.web;

import jakarta.validation.Valid;
import org.app.actor.service.ActorService;
import org.app.movie.model.Movie;
import org.app.movie.service.MovieService;
import org.app.reviewclient.ReviewClient;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.dto.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private static final int REVIEWS_PAGE_SIZE = 10;

    private final UserService userService;
    private final ActorService actorService;
    private final MovieService movieService;
    private final ReviewClient reviewClient;

    public MovieController(UserService userService, ActorService actorService, MovieService movieService, ReviewClient reviewClient) {
        this.userService = userService;
        this.actorService = actorService;
        this.movieService = movieService;
        this.reviewClient = reviewClient;
    }

    @GetMapping("/new")
    public ModelAndView getAddMoviePage() {

        MovieOptionsDto movieOptionsDto = movieService.getMovieOptions();

        ModelAndView modelAndView = new ModelAndView("add-movies");

        modelAndView.addObject("movieDto", new CreateMovieRequest());
        modelAndView.addObject("movieOptions", movieOptionsDto);


        return modelAndView;
    }

    @PostMapping("/new")
    public ModelAndView addMovie(Principal principal,
                                 @Valid @ModelAttribute("movieDto") CreateMovieRequest createMovieRequest,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        MovieOptionsDto movieOptionsDto = movieService.getMovieOptions();

        if (bindingResult.hasErrors()) {

            ModelAndView modelAndView = new ModelAndView("add-movies");
            modelAndView.addObject("movieOptions", movieOptionsDto);

            return modelAndView;
        }

        User user = userService.getUserByUsernameOrEmail(principal.getName());

        movieService.addMovie(createMovieRequest, user.getUsername(), user.getRole());

        redirectAttributes.addFlashAttribute(
                "successMessage", "Movie has been successfully added!");

        return new ModelAndView("redirect:/movies/new");
    }

    @GetMapping("/details/{movieId}")
    public ModelAndView getDetails(@PathVariable UUID movieId,
                                   @RequestParam(defaultValue = "0") int page) {

        Movie movie = movieService.getMovie(movieId);

        CustomPageDto<ViewReviewsAndCommentsDto> reviewsPage = reviewClient.getReviewMovie(movieId, page, REVIEWS_PAGE_SIZE);

        ModelAndView modelAndView = new ModelAndView("movie-details");

        modelAndView.addObject("movie", movie);
        modelAndView.addObject("review", reviewsPage.getContent());
        modelAndView.addObject("currentPage", reviewsPage.getCurrentPage());
        modelAndView.addObject("totalPages", reviewsPage.getTotalPages());
        modelAndView.addObject("pageSize", REVIEWS_PAGE_SIZE);
        modelAndView.addObject("totalReviews", reviewsPage.getTotalElements());
        modelAndView.addObject("createReviewDto", new CreateReviewDto());
        modelAndView.addObject("createCommentDto", new CreateReviewDto());

        return modelAndView;
    }

    @GetMapping("/actors")
    public ModelAndView getAddActorPage() {

        ModelAndView modelAndView = new ModelAndView("add-actors");
        modelAndView.addObject("actor", new AddActorDto());

        return modelAndView;
    }

    @PostMapping("/actors")
    public ModelAndView addActor(@Valid @ModelAttribute("actor") AddActorDto addActorDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            return new ModelAndView("add-actors");
        }

        actorService.addActor(addActorDto);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Actor has been successfully added!");

        return new ModelAndView("redirect:/movies/actors");
    }

    @GetMapping("/{id}/edit")
    public ModelAndView getMovieEditPage(@PathVariable("id") UUID movieId,
                                         RedirectAttributes redirectAttributes,
                                         @RequestParam(name = "source") String source) {

        if (movieId == null) {

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Movie not found!");

            return new ModelAndView("redirect:/edit-movie");
        }

        EditMovieDetails movieDto = movieService.getMovieEditDetails(movieId);

        MovieOptionsDto movieOptionsDto = movieService.getMovieOptions();

        ModelAndView modelAndView = new ModelAndView("edit-movie");
        modelAndView.addObject("movieDto", movieDto);
        modelAndView.addObject("movieId", movieId);
        modelAndView.addObject("movieOptions", movieOptionsDto);
        modelAndView.addObject("source", source);

        return modelAndView;
    }

    @PostMapping("/{id}/edit")
    public ModelAndView editMovie(Principal principal,
                                  @PathVariable(name = "id") UUID movieId,
                                  @Valid EditMovieDetails movieDetails, BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  @RequestParam(name = "source", required = false) String source) {

        if (bindingResult.hasErrors()) {

            ModelAndView modelAndView = new ModelAndView("edit-movie");

            modelAndView.addObject("source", source);

            return modelAndView;
        }

        User user = userService.getUserByUsernameOrEmail(principal.getName());
        Role userRole = user.getRole();

        Movie movie = movieService.getMovie(movieId);

        if (userRole != Role.ADMIN && userRole != Role.SUPER_ADMIN
                && !movie.getPublisher().getId().equals(user.getId())) {

            return new ModelAndView("redirect:/error");
        }

        movieService.editMovie(movieDetails, movieId);

        redirectAttributes.addFlashAttribute("successMessage",
                "Successfully edit movie.");

        if (source.equals("admin")) {
            return new ModelAndView("redirect:/admin/movies");
        }

        return new ModelAndView("redirect:/home/my-profile");
    }


}
