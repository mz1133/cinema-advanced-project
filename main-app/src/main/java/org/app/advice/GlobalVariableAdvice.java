package org.app.advice;

import jakarta.servlet.http.HttpSession;
import org.app.user.service.UserService;
import org.app.web.dto.UserHeaderDto;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@ControllerAdvice
public class GlobalVariableAdvice {


    private final UserService userService;

    public GlobalVariableAdvice(UserService userService) {
        this.userService = userService;
    }


    @ModelAttribute("currentUser")
    public UserHeaderDto currentUser(HttpSession session) {

        UUID uuid = (UUID) session.getAttribute("userId");

        if(uuid == null) {
            return null;
        }

        return userService.getUserHeaderDto(uuid);

    }
}
