package org.app.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.app.movie.model.Country;
import org.app.movie.model.Genre;
import org.app.movie.model.Movie;
import org.app.movie.model.ReleaseYear;
import org.app.movie.service.MovieService;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.dto.LoginRequest;
import org.app.web.dto.RegisterRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;


@Controller
public class IndexController {

    private final UserService userService;
    private final MovieService movieService;

    public IndexController(UserService userService, MovieService movieService) {
        this.userService = userService;
        this.movieService = movieService;
    }

    @GetMapping("/")
    public ModelAndView getIndexPage(@SessionAttribute(value = "userId", required = false) UUID userId,
                                      @PageableDefault(size = 20) Pageable pageable,
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

        if(userId != null) {return  new ModelAndView("redirect:/home");}

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
    public ModelAndView getLoginPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("loginRequest", new LoginRequest());
        modelAndView.setViewName("login");

        return modelAndView;
    }

    @PostMapping("/login")
    public ModelAndView loginUser(@Valid LoginRequest loginRequest,
                                  BindingResult bindingResult,
                                  HttpSession session) {

        if (bindingResult.hasErrors()) {return new ModelAndView("login");}

        User user = userService.login(loginRequest);

        session.setAttribute("userId", user.getId());

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/about")
    public String getAboutPage() {return "about";}

    @PostMapping("/logout")
    public String getLogoutPage(HttpSession httpSession) {

        httpSession.invalidate();

        return "redirect:/";
    }


}
