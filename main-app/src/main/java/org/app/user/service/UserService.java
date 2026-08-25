package org.app.user.service;

import lombok.extern.slf4j.Slf4j;
import org.app.exception.UnauthorizedRoleChangeException;
import org.app.exception.UserNotFoundException;
import org.app.exception.ValidationFailedException;
import org.app.notification.service.NotificationService;
import org.app.security.AuthenticationMetadata;
import org.app.subscription.model.Subscription;
import org.app.user.model.User;
import org.app.user.model.Role;
import org.app.user.repository.UserRepository;
import org.app.web.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UserService  implements UserDetailsService {

    private final static LocalDateTime LOCAL_DATE_TIME_NOW = LocalDateTime.now();
    private final static String ERROR_MESSAGE_USER_ALREADY_EXISTS = "User already exists";
    private final static String ERROR_MESSAGE_EMAIL_ALREADY_EXISTS = "Email already exists";
    private final static String ERROR_MESSAGE_PASSWORD_IS_NOT_MATCH = "Oops! Your password don't match";
    private final static String FIELD_NAME_USERNAME = "username";
    private final static String FIELD_NAME_EMAIL = "email";
    private final static String FIELD_NAME_PASSWORD = "password";
    private final static String ERROR_MESSAGE_INVALID_USERNAME_MAIL_OR_PASSWORD = "Invalid username/email or password";
    private final static String ERROR_MESSAGE_USER_NOT_FOUD = "User not found";
    private final static String DEFAULT_PICTURE_URL = "https://cdn-icons-png.flaticon.com/512/847/847969.png";
    private final static String ERROR_MESSAGE_CANNOT_CHANGE_ROLE = "You are not allowed to assign this role.";
    private final static String ERROR_MESSAGE_CANNOT_CHANGE_ACTIVE_STATUS = "You are not allowed to assign this active.";
    private final static String ERROR_MESSAGE_USER_NOT_FOUND = "User not found";
    private final static String WELCOME_MESSAGE = "Welcome %s to CineSpectrum! We are glad to have you here.";
    private static final String TYPE_WELCOME_NOTIFICATION_TEXT = "WELCOME";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public void register(RegisterRequest registerRequest) {

        Map<String, String> errorsMessages = new HashMap<>();

        isMatchPassword(registerRequest, errorsMessages);

        Optional<User> findUserByUsername = userRepository
                .findByUsername(registerRequest
                        .getUsername());

        isExistUserWithSameUsername(findUserByUsername, errorsMessages);

        Optional<User> findUserByEmail = userRepository
                .findByEmail(registerRequest
                        .getEmail());

        isAlreadyExistEmail(findUserByEmail, errorsMessages);

        throwIfHaveError(errorsMessages);

        User user = addUser(registerRequest);

        user.setPictureUrl(DEFAULT_PICTURE_URL);

        saveUser(user);

        notificationService.createNotification(
                user,
                WELCOME_MESSAGE.formatted(registerRequest.getUsername()),
                TYPE_WELCOME_NOTIFICATION_TEXT
        );

        log.info("User username:{%s}, id:{%s} has been successfully registered".formatted(user.getId(), user.getUsername()));
    }


    public void updateUserProfile(String usernameOrPassword, UpdateProfileRequest request) {

        User user = getUserByUsernameOrEmail(usernameOrPassword);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBirthDate(request.getBirthDate());
        user.setPictureUrl(request.getPictureUrl());
        user.setUpDateOn(LOCAL_DATE_TIME_NOW);

        saveUser(user);

        log.info("user:{%s} has been successfully updated profile".formatted(user.getUsername()));
    }

    public User getUserById(UUID id) {

        return userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(ERROR_MESSAGE_USER_NOT_FOUD));
    }



    public boolean isCanAddMovie(UUID id) {

        User user = getUserById(id);

        if (isUserAdmin(user)) {
            return true;
        }

        if (!hasSubscription(user.getSubscription())) {
            return false;
        }

        return user
                .getSubscription()
                .getExpirationDate()
                .isAfter(LOCAL_DATE_TIME_NOW);
    }

    public Page<User> getAllUsersPageable(Pageable pageable, UUID currentUserId) {

        return userRepository.findAllByIdNot(pageable, currentUserId);
    }

    public Page<User> getUserByKeyWord(String keyword, Pageable pageable, UUID currentUserId) {

        try {
            return findUserByKeywordUUID(keyword, pageable, currentUserId);

        } catch (IllegalArgumentException e) {
            return findUserByKeywordUsername(keyword, pageable, currentUserId);

        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());

            return Page.empty(pageable);
        }
    }

    public void changeUserRole(String usernameOrEmail, Role roleToChange, UUID id) {

        User userToChange = getUserById(id);
        User admin = getUserByUsernameOrEmail(usernameOrEmail);

        String currentRole = userToChange.getRole().toString();

        hasPermissionToChangeUserRole(roleToChange, admin, userToChange);

        userToChange.setRole(roleToChange);
        userToChange.setUpDateOn(LOCAL_DATE_TIME_NOW);

        saveUser(userToChange);

        log.info(String.format("CHANGE ROLE FOR USER USERNAME: %s, FROM: %s TO: %s",
                userToChange.getUsername(),
                currentRole,
                userToChange
                        .getRole()
                        .toString()));


    }

    public void changeUserActivationStatus(String adminUsernameOrEmail, UUID id) {

        User adminUser = getUserByUsernameOrEmail(adminUsernameOrEmail);

        User currentUser = getUserById(id);

        hasPermissionToChangeUserActivity(adminUser, currentUser);

        boolean currentUserStatus = currentUser.isActive();

        changeUserActivity(currentUser);

        saveUser(currentUser);

        loggedInSystemsToChangeUserActivity(currentUser, currentUserStatus);


    }

    private void saveUser(User currentUser) {
        userRepository.save(currentUser);
    }

    public UpdateProfileRequest getCurrentProfileData(UUID userId) {

        User user = getUserById(userId);

        return UpdateProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .pictureUrl(user.getPictureUrl())
                .build();
    }

    private User addUser(RegisterRequest registerRequest) {


        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role(Role.USER)
                .createdOn(LOCAL_DATE_TIME_NOW)
                .upDateOn(LOCAL_DATE_TIME_NOW)
                .isActive(true)
                .build();
    }

    private static void hasPermissionToChangeUserActivity(User adminUser, User currentUser) {

        if (adminUser.getRole() != Role.ADMIN && adminUser.getRole() != Role.SUPER_ADMIN) {

            throw new UnauthorizedRoleChangeException(ERROR_MESSAGE_CANNOT_CHANGE_ACTIVE_STATUS);
        }

        if (currentUser.getRole() == Role.SUPER_ADMIN || (currentUser.getRole() == Role.ADMIN
                && adminUser.getRole() == Role.ADMIN)) {

            throw new UnauthorizedRoleChangeException(ERROR_MESSAGE_CANNOT_CHANGE_ACTIVE_STATUS);
        }
    }

    private static void hasPermissionToChangeUserRole(Role roleToChange, User admin, User userToChange) {

        if (roleToChange == Role.SUPER_ADMIN || admin.getRole() == Role.USER
                || userToChange.getRole() == Role.SUPER_ADMIN) {

            throw new UnauthorizedRoleChangeException(ERROR_MESSAGE_CANNOT_CHANGE_ROLE);
        }

        if (admin.getRole() == Role.ADMIN && roleToChange == Role.ADMIN) {
            throw new UnauthorizedRoleChangeException(ERROR_MESSAGE_CANNOT_CHANGE_ROLE);

        }
    }

    private Page<User> findUserByKeywordUUID(String keyword, Pageable pageable, UUID currentUserId) {

        UUID keywordId = UUID.fromString(keyword);

        return userRepository.findByIdAndIdNot(keywordId, currentUserId, pageable);
    }

    private Page<User> findUserByKeywordUsername(String keyword, Pageable pageable, UUID currentUserId) {

        return userRepository.findByUsernameAndIdNot(keyword, pageable, currentUserId);
    }

    private static void changeUserActivity(User currentUser) {
        currentUser.setActive(!currentUser.isActive());

        loggedInSystemsToChangeUserActivity(currentUser, currentUser.isActive());
    }

    private static void loggedInSystemsToChangeUserActivity(User currentUser, boolean currentUserStatus) {

        log.info(String.format("CHANGE: activation status for user USERNAME: %s, FORM: %s TO: %s",
                currentUser.getUsername(),
                currentUserStatus, currentUser.isActive()));
    }

    private boolean hasSubscription(Subscription subscription) {

        return subscription != null;
    }

    private static void isMatchPassword(RegisterRequest registerRequest, Map<String, String> errorsMessages) {

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            errorsMessages.put(FIELD_NAME_PASSWORD, ERROR_MESSAGE_PASSWORD_IS_NOT_MATCH);
        }
    }

    private static void isExistUserWithSameUsername(Optional<User> findUserByUsername, Map<String, String> errorsMessages) {

        if (findUserByUsername.isPresent()) {
            errorsMessages.put(FIELD_NAME_USERNAME, ERROR_MESSAGE_USER_ALREADY_EXISTS);
        }
    }

    private static void isAlreadyExistEmail(Optional<User> findUserByEmail, Map<String, String> errorsMessages) {

        if (findUserByEmail.isPresent()) {
            errorsMessages.put(FIELD_NAME_EMAIL, ERROR_MESSAGE_EMAIL_ALREADY_EXISTS);
        }
    }

    private static void throwIfHaveError(Map<String, String> errorsMessages) {

        if (!errorsMessages.isEmpty()) {
            throw new ValidationFailedException(errorsMessages);
        }
    }


    private static boolean isUserAdmin(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN;
    }

    public User getUserByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(ERROR_MESSAGE_USER_NOT_FOUND));
    }

    public UserHeaderDto getUserHeaderDto(UUID uuid) {



        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException(ERROR_MESSAGE_USER_NOT_FOUND));



        return UserHeaderDto.builder()
                .id(user.getId())
                .pictureUrl(user.getPictureUrl())
                .username(user.getUsername())
                .isCanAddMovie(isCanAddMovie(uuid))
                .role(user.getRole())
                .createdOn(user.getCreatedOn())
                .subscription(user.getSubscription())
                .isCanAddReviewAndComment(isCanAddReviewAndComment(uuid))
                .build();
    }

    public boolean isCanAddReviewAndComment(UUID uuid) {

        User user = getUserById(uuid);

        if (!hasSubscription(user.getSubscription())) {
            return false;
        }

        return user
                .getSubscription()
                .getExpirationDate()
                .isAfter(LOCAL_DATE_TIME_NOW);


    }


    public User getUserByUsernameOrEmail(String usernameOrEmail) {

        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException(ERROR_MESSAGE_USER_NOT_FOUND));
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(ERROR_MESSAGE_USER_NOT_FOUND));


        return new AuthenticationMetadata(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isActive()
        );
    }

    public boolean isAdmin(User admin) {

        return admin.getRole() == Role.ADMIN || admin.getRole() == Role.SUPER_ADMIN;
    }

    public List<User> getUsersBySubscriptionExpirationDate(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return userRepository.findBySubscriptionExpirationDateBetween(startOfDay, endOfDay);
    }
}

