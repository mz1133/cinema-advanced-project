package org.app.web;


import jakarta.validation.Valid;

import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.app.movie.model.ReleaseYear;
import org.app.movie.service.MovieService;

import org.app.user.service.UserService;
import org.app.web.dto.RegisterRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import org.springframework.web.servlet.ModelAndView;




@Controller
public class IndexController {

    private final UserService userService;
    private final MovieService movieService;

    public IndexController(UserService userService, MovieService movieService) {
        this.userService = userService;
        this.movieService = movieService;
    }

    @GetMapping("/")
    public ModelAndView getIndexPage( @PageableDefault(size = 20) Pageable pageable,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Genre genre,
                                      @RequestParam(required = false) Country country,
                                      @RequestParam(required = false) String sort) {

        Page<Movie> movies = movieService.search(keyword, year, genre, country, sort, pageable);

        ModelAndView modelAndView = new ModelAndView("index");

        modelAndView.addObject("movies", movies);
        modelAndView.addObject("genres", Genre.values());
        modelAndView.addObject("releaseYears", ReleaseYear.values());
        modelAndView.addObject("countries", Country.values());

        return modelAndView;
    }

    @GetMapping("/register/form")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest,
                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {return new ModelAndView("register");}

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String error) {

        ModelAndView modelAndView = new ModelAndView("login");

        if(error != null) {
            modelAndView.addObject("error", "Invalid username or password!");
        }

        return modelAndView;
    }

    @GetMapping("/about")
    public String getAboutPage() {return "about";}




}
