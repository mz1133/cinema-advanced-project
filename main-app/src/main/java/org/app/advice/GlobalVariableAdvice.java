package org.app.advice;


import org.app.user.model.User;
import org.app.user.service.UserService;
import org.app.web.dto.UserHeaderDto;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;


@ControllerAdvice
public class GlobalVariableAdvice {


    private final UserService userService;

    public GlobalVariableAdvice(UserService userService) {
        this.userService = userService;
    }


    @ModelAttribute("currentUser")
    public UserHeaderDto currentUser(Principal principal) {

        if (principal == null) {
            return null;
        }

        User user = userService.getUserByUsernameOrEmail(principal.getName());

        if (user == null) {
            return null;
        }

        return userService.getUserHeaderDto(user.getId());

    }

}

