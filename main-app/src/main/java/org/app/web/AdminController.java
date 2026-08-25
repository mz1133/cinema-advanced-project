package org.app.web;

import org.app.movie.model.Movie;
import org.app.movie.service.MovieService;
import org.app.reviewclient.ReviewClient;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final static String MESSAGE_NOT_FOUND = "No results found.";

    private final UserService userService;
    private final MovieService movieService;
    private final ReviewClient reviewClient;

    public AdminController(UserService userService, MovieService movieService, ReviewClient reviewClient) {
        this.userService = userService;
        this.movieService = movieService;
        this.reviewClient = reviewClient;
    }

    @GetMapping("/users")
    public ModelAndView getManageUserPage(Principal principal,
                                          @RequestParam(name = "keyword", required = false) String keyword,
                                          @PageableDefault(size = 20) Pageable pageable) {

        User user = userService.getUserByUsernameOrEmail(principal.getName());
        UUID userid = user.getId();

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        Page<User> users;

        ModelAndView modelAndView = new ModelAndView("manage-users");

        if (keyword != null && !keyword.isEmpty()) {
            users = userService.getUserByKeyWord(keyword, pageable, userid);

            if (users.isEmpty()) {
                modelAndView.addObject("message", MESSAGE_NOT_FOUND);
            }

        } else {
            users = userService.getAllUsersPageable(pageable, userid);
        }

        modelAndView.addObject("users", users);
        modelAndView.addObject("roles", Role.values());
        modelAndView.addObject("keyword", keyword);

        return modelAndView;
    }

    @GetMapping("/movies")
    public ModelAndView getManageMoviesPage(Principal principal,
                                            @RequestParam(value = "keyword", required = false) String keyword,
                                            @RequestParam(value = "searchType", required = false) String searchType,
                                            @PageableDefault(size = 20) Pageable pageable) {

        User admin = userService.getUserByUsernameOrEmail(principal.getName());
        Role adminRole = admin.getRole();

        if (adminRole != Role.ADMIN && adminRole != Role.SUPER_ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView modelAndView = new ModelAndView("manage-movies");

        Page<Movie> movies = (keyword != null && !keyword.isBlank())
                && (searchType != null && !searchType.isBlank())
                ? movieService.getMovieByKeyword(keyword, pageable, searchType)
                : movieService.getAllMoviesPageable(pageable);

        if (movies.isEmpty()) {
            modelAndView.addObject("message", MESSAGE_NOT_FOUND);
        }

        modelAndView.addObject("movies", movies);
        modelAndView.addObject("keyword", keyword);
        modelAndView.addObject("searchType", searchType);

        return modelAndView;
    }

    @PostMapping("/users/role")
    public ModelAndView changeUserRole(Principal principal,
                                       @RequestParam("role") Role roleToChange,
                                       @RequestParam(value = "userToChangeRoleId", required = false) UUID userToChangeRoleId,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       RedirectAttributes redirectAttributes) {

        if (userToChangeRoleId == null) {
            return new ModelAndView("redirect:/admin/users");
        }

        userService.changeUserRole(principal.getName(), roleToChange, userToChangeRoleId);

        redirectAttributes.addAttribute("keyword", keyword);

        redirectAttributes.addFlashAttribute(
                "successChangeRole", "Successfully changed role for user.");

        return new ModelAndView("redirect:/admin/users");
    }

    @PatchMapping("/users/active")
    public ModelAndView makeUserInactive(Principal principal,
                                         @RequestParam(value = "userId", required = false) UUID userToChangeActive,
                                         RedirectAttributes redirectAttributes) {

        if (userToChangeActive == null) {
            return new ModelAndView("redirect:/admin/users");
        }

        String usernameForChangedActiveUser = userService.getUserById(userToChangeActive).getUsername();

        userService.changeUserActivationStatus(principal.getName(), userToChangeActive);

        redirectAttributes.addFlashAttribute("successChangeUserStatus",
                String.format("Successfully changed user: %s activation status.", usernameForChangedActiveUser));

        return new ModelAndView("redirect:/admin/users");

    }

    @DeleteMapping("/movies")
    public ModelAndView deleteMovie(Principal principal,
                                    @RequestParam("movieIdToDelete") UUID movieToDeleteId,
                                    RedirectAttributes redirectAttributes) {

        User admin = userService.getUserByUsernameOrEmail(principal.getName());
        Role adminRole = admin.getRole();

        if (adminRole != Role.ADMIN && adminRole != Role.SUPER_ADMIN) {
            return new ModelAndView("redirect:/home");
        }

        if (movieToDeleteId == null) {
            return new ModelAndView("redirect:/admin/movies");
        }

        movieService.deleteMovie(movieToDeleteId);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Successfully deleted movie.");

        return new ModelAndView("redirect:/admin/movies");

    }

    @PatchMapping("/movies")
    public ModelAndView restoreMovie(Principal principal,
                                     @RequestParam("movieIdToRestore") UUID movieToRestore,
                                     RedirectAttributes redirectAttributes) {

        User admin = userService.getUserByUsernameOrEmail(principal.getName());

        if (admin.getRole() != Role.ADMIN
                && admin.getRole() != Role.SUPER_ADMIN) {

            return new ModelAndView("redirect:/home");
        }

        if (movieToRestore == null) {
            return new ModelAndView("redirect:/admin/movies");
        }

        movieService.restoreMovie(movieToRestore);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Successfully restore movie.");

        return new ModelAndView("redirect:/admin/movies");

    }

    @GetMapping("/reviews")
    public ModelAndView getAllReviewsAndComments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) String publisherUsername,
            @RequestParam(required = false) String movieTitle,
            @PageableDefault(size = 10) Pageable pageable) {

        CustomPageDto<AdminReviewDto> reviews =
                reviewClient.getAllReviewsAndComments(
                        keyword,
                        movieId,
                        publisherUsername,
                        movieTitle,
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                );

        ModelAndView modelAndView = new ModelAndView("manage-reviews");

        modelAndView.addObject("pageData", reviews);
        modelAndView.addObject("pageSize", pageable.getPageSize());

        modelAndView.addObject("keyword", keyword);
        modelAndView.addObject("movieId", movieId);
        modelAndView.addObject("publisherUsername", publisherUsername);
        modelAndView.addObject("movieTitle", movieTitle);
        modelAndView.addObject("reviewDeleteNotification", new ReviewDeleteNotification());
        modelAndView.addObject("deleteCommentDto", new DeleteCommentDto());


        return modelAndView;
    }


}
