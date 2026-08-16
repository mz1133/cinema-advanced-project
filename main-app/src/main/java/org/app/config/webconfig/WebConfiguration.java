package org.app.config.webconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final UserStateInterceptor userStateInterceptor;

    public WebConfiguration(UserStateInterceptor userStateInterceptor) {
        this.userStateInterceptor = userStateInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(userStateInterceptor)
                .addPathPatterns("/**")

                .excludePathPatterns("/css/**",
                        "/js/**",
                        "/images/**",
                        "/",
                        "/login",
                        "/register/**",
                        "/about",
                        "/favicon.ico",
                        "/movies/details/**",
                        "/error");
    }
}
