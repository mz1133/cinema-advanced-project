package org.app.config.DataInitializer;

import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class InitSuperAdminUser implements ApplicationRunner {

    private final static String ADMIN_USERNAME = "sadmin123";
    private final static String ADMIN_PASSWORD = "sadmin123";
    private final static String ADMIN_EMAIL = "sadmin123@gmail.com";
    private final static String ADMIN_FIRST_NAME = "Ivan";
    private final static String ADMIN_LAST_NAME = "Petrov";
    private final static LocalDateTime LOCAL_DATE_TIME_NOW = LocalDateTime.now();
    private final static String DEFAULT_PICTURE_URL = "https://cdn-icons-png.flaticon.com/512/847/847969.png";


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitSuperAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {

            User admin = buildUserSuperAdminRole();

            userRepository.save(admin);
        }
    }

    @NonNull
    private User buildUserSuperAdminRole() {


        return User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .email(ADMIN_EMAIL)
                .firstName(ADMIN_FIRST_NAME)
                .lastName(ADMIN_LAST_NAME)
                .isActive(true)
                .pictureUrl(DEFAULT_PICTURE_URL)
                .role(Role.SUPER_ADMIN)
                .createdOn(LOCAL_DATE_TIME_NOW)
                .upDateOn(LOCAL_DATE_TIME_NOW)
                .build();
    }
}
