package org.app.web;


import jakarta.validation.Valid;
import org.app.event.events.CommentDeletedByAdminEvent;
import org.app.event.events.ReviewDeleteEvent;
import org.app.movie.model.Movie;
import org.app.movie.service.MovieService;

import org.app.reviewclient.ReviewClient;
import org.app.user.model.User;
import org.app.user.service.UserService;

import org.app.web.dto.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/review")
public class ReviewController {

    private static final int REVIEWS_PAGE_SIZE = 10;


    private final ReviewClient reviewClient;
    private final UserService userService;
    private final MovieService movieService;
    private final ApplicationEventPublisher publisher;

    public ReviewController(ReviewClient reviewClient, UserService userService, MovieService movieService, ApplicationEventPublisher publisher) {
        this.reviewClient = reviewClient;
        this.userService = userService;
        this.movieService = movieService;
        this.publisher = publisher;

    }


    @PostMapping("/{movieId}")
    public ModelAndView createReview(@PathVariable("movieId") UUID movieId,
                                     @Valid @ModelAttribute("createReviewDto") CreateReviewDto createReviewDto,
                                     BindingResult bindingResult,
                                     Principal principal,
                                     ModelMap model) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("movie-details", model);


            modelAndView.addObject("movie", movieService.getMovie(movieId));


            CustomPageDto<ViewReviewsAndCommentsDto> reviewsPage = reviewClient.getReviewMovie(movieId, 0, 10);


            modelAndView.addObject("review", reviewsPage.getContent());


            modelAndView.addObject("createCommentDto", new CreateCommentDto());

            return modelAndView;
        }

        User use = userService.getUserByUsername(principal.getName());
        Movie movie = movieService.getMovie(movieId);

        createReviewDto.setPublisherId(use.getId());
        createReviewDto.setPublisherUsername(use.getUsername());
        createReviewDto.setMovieId(movieId);
        createReviewDto.setMovieTitle(movie.getTitle());
        createReviewDto.setDeleted(false);

        reviewClient.createReview(createReviewDto);


        return new ModelAndView("redirect:/movies/details/" + movieId + "#reviews-list");


    }

    @PostMapping("/{reviewId}/comments")
    public ModelAndView createComment(@PathVariable(name = "reviewId") UUID reviewId,
                                      @RequestParam(name = "movieId") UUID movieId,
                                      @Valid @ModelAttribute("createCommentDto") CreateCommentDto createCommentDto,
                                      BindingResult bindingResult,
                                      Principal principal,
                                      @RequestParam(defaultValue = "0") int page) {

        if (bindingResult.hasErrors()) {
            Movie movie = movieService.getMovie(movieId);

            CustomPageDto<ViewReviewsAndCommentsDto> reviewsPage = reviewClient.getReviewMovie(movieId, page, REVIEWS_PAGE_SIZE);

            ModelAndView modelAndView = new ModelAndView("movie-details");

            modelAndView.addObject("movie", movie);
            modelAndView.addObject("review", reviewsPage.getContent());
            modelAndView.addObject("currentPage", reviewsPage.getCurrentPage());
            modelAndView.addObject("pageSize", REVIEWS_PAGE_SIZE);
            modelAndView.addObject("totalReviews", reviewsPage.getTotalElements());
            modelAndView.addObject("createReviewDto", new CreateReviewDto());

            return modelAndView;
        }


        User user = userService.getUserByUsername(principal.getName());

        createCommentDto.setPublisherId(user.getId());
        createCommentDto.setPublisherUsername(user.getUsername());
        createCommentDto.setReviewId(reviewId);

        reviewClient.createComment(createCommentDto);

        return new ModelAndView("redirect:/movies/details/" + movieId + "#comments-" + reviewId);

    }


    @GetMapping("/my-reviews")
    public ModelAndView getMyReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) String movieTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {


        User user = userService.getUserByUsername(principal.getName());


        CustomPageDto<AdminReviewDto> pageData = reviewClient.getUserReviews(
                user.getId(), keyword, movieId, movieTitle, page, size
        );


        ModelAndView modelAndView = new ModelAndView("my-reviews");
        modelAndView.addObject("pageData", pageData);
        modelAndView.addObject("keyword", keyword);
        modelAndView.addObject("movieId", movieId);
        modelAndView.addObject("movieTitle", movieTitle);
        modelAndView.addObject("pageSize", size);
        modelAndView.addObject("reviewEditDto", new EditReviewDto());

        return modelAndView;
    }

    @PostMapping("/my-reviews/{id}")
    public String deleteMyReview(@PathVariable UUID id, Principal principal,  RedirectAttributes redirectAttributes) {

        User user = userService.getUserByUsername(principal.getName());
        boolean isAdmin = userService.isAdmin(user);


        reviewClient.deleteReview(id, isAdmin);

        redirectAttributes.addFlashAttribute("successMessage", "Review deleted successfully!");

        return "redirect:/review/my-reviews";
    }

    @PostMapping("/my-reviews/{id}/restore")
    public String restoreMyReview(@PathVariable UUID id,  RedirectAttributes redirectAttributes) {

        reviewClient.restoreReview(id);

        redirectAttributes.addFlashAttribute("successMessage", "Review restore successfully!");


        return "redirect:/review/my-reviews";
    }

    @PostMapping("/my-reviews/edit/{id}")
    public ModelAndView updateReview(@PathVariable UUID id,
                                     @Valid @ModelAttribute("reviewEditDto") EditReviewDto reviewEditDto,
                                     BindingResult bindingResult,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) UUID movieId,
                                     @RequestParam(required = false) String movieTitle,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {


        if (bindingResult.hasErrors()) {
            User user = userService.getUserByUsername(principal.getName());

            CustomPageDto<AdminReviewDto> pageData = reviewClient.getUserReviews(
                    user.getId(), keyword, movieId, movieTitle, page, size
            );

            ModelAndView modelAndView = new ModelAndView("my-reviews");
            modelAndView.addObject("pageData", pageData);
            modelAndView.addObject("keyword", keyword);
            modelAndView.addObject("movieId", movieId);
            modelAndView.addObject("movieTitle", movieTitle);
            modelAndView.addObject("pageSize", size);


            modelAndView.addObject("failedReviewId", id);

            return modelAndView;
        }


        reviewClient.updateReview(id, reviewEditDto);


        redirectAttributes.addFlashAttribute("successMessage", "Review successfully updated!");


        return new ModelAndView("redirect:/review/my-reviews");


    }


    @PostMapping("/review/{id}")
    public ModelAndView deleteReview(@PathVariable UUID id,
                                     Principal principal,
                                     @Valid ReviewDeleteNotification reviewDeleteNotification,
                                     BindingResult bindingResult,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) UUID movieId,
                                     @RequestParam(required = false) String publisherUsername,
                                     @RequestParam(required = false) String movieTitle,
                                     @PageableDefault(size = 10) Pageable pageable,
                                     RedirectAttributes redirectAttributes) {


        if (bindingResult.hasErrors()) {

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
            modelAndView.addObject("deleteCommentDto", new DeleteCommentDto());


            return modelAndView;
        }


        User admin = userService.getUserByUsername(principal.getName());

        boolean isAdmin = userService.isAdmin(admin);

        if (!isAdmin) {
            return new ModelAndView("redirect:/home");
        }


        reviewClient.deleteReview(id, isAdmin);

        User user = userService.getUserById(reviewDeleteNotification.getPublisherId());


        ReviewDeleteEvent event = new ReviewDeleteEvent();
        event.setUser(user);
        event.setReasonMessage(reviewDeleteNotification.getReason());

        publisher.publishEvent(event);

        redirectAttributes.addFlashAttribute("success", "Review was successfully deleted!");


        return new ModelAndView("redirect:/admin/reviews");
    }

    @PostMapping("/comments/delete")
    public ModelAndView deleteComment(Principal principal,
                                      @Valid DeleteCommentDto deleteCommentDto,
                                      BindingResult bindingResult,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) UUID movieId,
                                      @RequestParam(required = false) String publisherUsername,
                                      @RequestParam(required = false) String movieTitle,
                                      @PageableDefault(size = 10) Pageable pageable,
                                      RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

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

            return modelAndView;
        }

        User admin = userService.getUserByUsername(principal.getName());

        if (!userService.isAdmin(admin)) {
            return new ModelAndView("redirect:/home");
        }

        User user = userService.getUserById(deleteCommentDto.getPublisherId());


        reviewClient.deleteComment(deleteCommentDto);

        CommentDeletedByAdminEvent event = new CommentDeletedByAdminEvent();

        event.setCommentId(deleteCommentDto.getCommentId());
        event.setReviewId(deleteCommentDto.getReviewId());
        event.setReason(deleteCommentDto.getReason());
        event.setPublisherUsername(user.getUsername());
        event.setUser(user);

        publisher.publishEvent(event);

        redirectAttributes.addFlashAttribute("success", "Comment was successfully deleted!");

        return new ModelAndView("redirect:/admin/reviews");
    }


}
