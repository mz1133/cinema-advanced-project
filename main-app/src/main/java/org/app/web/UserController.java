package org.app.web;

import jakarta.validation.Valid;
import org.app.config.SubscriptionProperties;
import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.app.movie.model.ReleaseYear;
import org.app.movie.service.MovieService;
import org.app.subscription.service.SubscriptionService;
import org.app.user.service.UserService;
import org.app.web.dto.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;


@Controller
@RequestMapping("/home")
public class UserController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionProperties subscriptionProperties;
    private final MovieService movieService;

    public UserController(UserService userService, SubscriptionService subscriptionService, SubscriptionProperties subscriptionProperties, MovieService movieService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.subscriptionProperties = subscriptionProperties;
        this.movieService = movieService;
    }

    @GetMapping
    public ModelAndView getHomePage(@SessionAttribute(name = "userId", required = false) UUID id,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) Genre genre,
                                    @RequestParam(required = false) Country country,
                                    @RequestParam(required = false) String sort,
                                    @PageableDefault(size = 20) Pageable pageable) {

        Page<Movie> movies = movieService.search(keyword, year, genre, country, sort, pageable);

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("movies", movies);
        modelAndView.addObject("genres", Genre.values());
        modelAndView.addObject("releaseYears", ReleaseYear.values());
        modelAndView.addObject("countries", Country.values());

        return modelAndView;
    }

    @GetMapping("/my-profile")
    public ModelAndView getMyProfile(@SessionAttribute(name = "userId", required = false) UUID id,
                                     @PageableDefault(size = 5) Pageable pageable) {


        Page<Movie> movies = movieService.getMoviesByPublisher(id, pageable);

        UpdateProfileRequest updateProfileRequest = userService.getCurrentProfileData(id);

        ModelAndView modelAndView = new ModelAndView("my-profile");
        modelAndView.addObject("updateProfileRequest", updateProfileRequest);
        modelAndView.addObject("movies", movies);

        return modelAndView;
    }

    @PostMapping("/my-profile/update")
    public ModelAndView updateMyProfile(@SessionAttribute(name = "userId", required = false) UUID id,
                                        @Valid UpdateProfileRequest updateProfileRequest,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {


        if (bindingResult.hasErrors()) {
            return new ModelAndView("my-profile");
        }

        userService.updateUserProfile(id, updateProfileRequest);

        redirectAttributes.addFlashAttribute("successMessage",
                "Profile updated successfully");

        return new ModelAndView("redirect:/home/my-profile");
    }

    @GetMapping("/subscriptions")
    public ModelAndView getSubscription(@SessionAttribute(name = "userId", required = false) UUID id) {

        if (id == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView modelAndView = new ModelAndView("subscriptions");
        modelAndView.addObject("plans", subscriptionProperties.getPlans());

        return modelAndView;
    }

    @PostMapping("/subscriptions/purchase")
    public ModelAndView makePurchase(@SessionAttribute(name = "userId", required = false) UUID userId,
                                     @RequestParam String planCode,
                                     RedirectAttributes redirectAttributes) {

        if (planCode == null) {
            return new ModelAndView("redirect:/subscriptions");
        }

        subscriptionService.addPlan(userId, planCode);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Plan added successfully!");

        return new ModelAndView("redirect:/home/subscriptions");
    }


}
