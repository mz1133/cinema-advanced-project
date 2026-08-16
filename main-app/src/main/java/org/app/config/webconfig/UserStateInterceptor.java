package org.app.config.webconfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.app.user.model.User;
import org.app.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class UserStateInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public UserStateInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        UUID userId = (UUID) session.getAttribute("userId");

        User user = userService.getUserById(userId);

        if (!user.isActive()) {
            session.invalidate();
            response.sendRedirect("/login");
            return false;

        }

        return true;

    }
}
