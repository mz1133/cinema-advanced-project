package org.app.web;
import jakarta.servlet.http.HttpServletResponse;
import org.app.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ValidationFailedException.class)
    public String handleRegistrationErrors(ValidationFailedException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorsMessages", e.getErrorsMessages());

        return "redirect:/register";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return "redirect:/login";
    }

    @ExceptionHandler(AlreadyHavePlanException.class)
    public String handleAlreadyHavePlanException(AlreadyHavePlanException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return "redirect:/home/subscriptions";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, RedirectAttributes redirectAttributes, HttpServletResponse response) {

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        redirectAttributes.addFlashAttribute("errorStatusCode", response.getStatus());

        return "redirect:/error";
    }

    @ExceptionHandler(ChangeUserRoleException.class)
    public String handleChangeUserRoleException(ChangeUserRoleException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return "redirect:/admin/users";
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public String handleMovieNotFoundException(MovieNotFoundException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return "redirect:/admin/movies";
    }


}
