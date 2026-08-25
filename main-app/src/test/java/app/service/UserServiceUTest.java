package app.service;

import org.app.exception.UnauthorizedRoleChangeException;
import org.app.exception.UserNotFoundException;
import org.app.exception.ValidationFailedException;
import org.app.notification.service.NotificationService;
import org.app.security.AuthenticationMetadata;
import org.app.subscription.model.Subscription;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.app.user.service.UserService;
import org.app.web.dto.RegisterRequest;
import org.app.web.dto.UpdateProfileRequest;
import org.app.web.dto.UserHeaderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {

        userService = new UserService(
                userRepository,
                passwordEncoder,
                notificationService
        );

        user = User.builder()
                .id(UUID.randomUUID())
                .username("john123")
                .email("john@gmail.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Smith")
                .phoneNumber("0888123456")
                .birthDate(LocalDate.of(1990, 5, 15))
                .role(Role.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .build();
    }

    @Test
    void register_validRequest_createsUserAndNotification() {

        RegisterRequest request = RegisterRequest.builder()
                .username("john123")
                .email("john@gmail.com")
                .password("password123")
                .confirmPassword("password123")
                .firstName("John")
                .lastName("Smith")
                .build();

        when(userRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        userService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("john123", savedUser.getUsername());
        assertEquals("john@gmail.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Smith", savedUser.getLastName());
        assertEquals(Role.USER, savedUser.getRole());
        assertTrue(savedUser.isActive());

        assertNotNull(savedUser.getCreatedOn());
        assertNotNull(savedUser.getUpDateOn());

        assertEquals(
                "https://cdn-icons-png.flaticon.com/512/847/847969.png",
                savedUser.getPictureUrl()
        );

        verify(notificationService).createNotification(
                eq(savedUser),
                eq("Welcome john123 to CineSpectrum! We are glad to have you here."),
                eq("WELCOME")
        );
    }

    @Test
    void register_passwordsDoNotMatch_throwsValidationFailedException() {

        RegisterRequest request = RegisterRequest.builder()
                .username("john123")
                .email("john@gmail.com")
                .password("password123")
                .confirmPassword("different123")
                .firstName("John")
                .lastName("Smith")
                .build();

        when(userRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ValidationFailedException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any());
        verify(notificationService, never())
                .createNotification(any(), anyString(), anyString());
    }

    @Test
    void register_usernameAlreadyExists_throwsValidationFailedException() {

        RegisterRequest request = RegisterRequest.builder()
                .username("john123")
                .email("john@gmail.com")
                .password("password123")
                .confirmPassword("password123")
                .firstName("John")
                .lastName("Smith")
                .build();

        when(userRepository.findByUsername("john123"))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ValidationFailedException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any());
        verify(notificationService, never())
                .createNotification(any(), anyString(), anyString());
    }

    @Test
    void changeUserRole_whenTargetRoleIsSuperAdmin_throwsException() {

        UUID userId = UUID.randomUUID();

        User admin = User.builder()
                .username("admin")
                .role(Role.SUPER_ADMIN)
                .build();

        User userToChange = User.builder()
                .username("john")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(userToChange));

        when(userRepository.findByUsernameOrEmail("admin", "admin"))
                .thenReturn(Optional.of(admin));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserRole(
                        "admin",
                        Role.SUPER_ADMIN,
                        userId
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsValidationFailedException() {

        RegisterRequest request = RegisterRequest.builder()
                .username("john123")
                .email("john@gmail.com")
                .password("password123")
                .confirmPassword("password123")
                .firstName("John")
                .lastName("Smith")
                .build();

        when(userRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                ValidationFailedException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any());
        verify(notificationService, never())
                .createNotification(any(), anyString(), anyString());
    }

    @Test
    void updateUserProfile_validRequest_updatesUser() {

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Mike")
                .lastName("Johnson")
                .phoneNumber("0888999999")
                .birthDate(LocalDate.of(1985, 7, 9))
                .pictureUrl("https://example.com/picture.jpg")
                .build();

        when(userRepository.findByUsernameOrEmail("john123", "john123"))
                .thenReturn(Optional.of(user));

        userService.updateUserProfile("john123", request);

        assertEquals("Mike", user.getFirstName());
        assertEquals("Johnson", user.getLastName());
        assertEquals("0888999999", user.getPhoneNumber());
        assertEquals(
                LocalDate.of(1985, 7, 9),
                user.getBirthDate()
        );
        assertEquals(
                "https://example.com/picture.jpg",
                user.getPictureUrl()
        );

        assertNotNull(user.getUpDateOn());

        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfile_userNotFound_throwsException() {

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Mike")
                .lastName("Johnson")
                .build();

        when(userRepository.findByUsernameOrEmail("unknown", "unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUserProfile("unknown", request)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_userExists_returnsUser() {

        UUID id = user.getId();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertSame(user, result);

        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_userDoesNotExist_throwsException() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(id)
        );
    }

    @Test
    void isCanAddMovie_admin_returnsTrue() {

        user.setRole(Role.ADMIN);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        boolean result =
                userService.isCanAddMovie(user.getId());

        assertTrue(result);
    }

    @Test
    void isCanAddMovie_superAdmin_returnsTrue() {

        user.setRole(Role.SUPER_ADMIN);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        boolean result =
                userService.isCanAddMovie(user.getId());

        assertTrue(result);
    }

    @Test
    void isCanAddMovie_userWithoutSubscription_returnsFalse() {

        user.setRole(Role.USER);
        user.setSubscription(null);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        boolean result =
                userService.isCanAddMovie(user.getId());

        assertFalse(result);
    }

    @Test
    void isCanAddMovie_userWithActiveSubscription_returnsTrue() {

        Subscription subscription = Subscription.builder()
                .active(true)
                .expirationDate(LocalDateTime.now().plusDays(10))
                .build();

        user.setRole(Role.USER);
        user.setSubscription(subscription);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        boolean result =
                userService.isCanAddMovie(user.getId());

        assertTrue(result);
    }

    @Test
    void isCanAddMovie_userWithExpiredSubscription_returnsFalse() {

        Subscription subscription = Subscription.builder()
                .active(true)
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        user.setRole(Role.USER);
        user.setSubscription(subscription);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        boolean result =
                userService.isCanAddMovie(user.getId());

        assertFalse(result);
    }

    @Test
    void getAllUsersPageable_returnsUsersFromRepository() {

        Pageable pageable = Pageable.ofSize(10);
        UUID currentUserId = UUID.randomUUID();

        Page<User> expected =
                new PageImpl<>(List.of(user));

        when(userRepository.findAllByIdNot(
                pageable,
                currentUserId
        )).thenReturn(expected);

        Page<User> result =
                userService.getAllUsersPageable(
                        pageable,
                        currentUserId
                );

        assertSame(expected, result);

        verify(userRepository)
                .findAllByIdNot(pageable, currentUserId);
    }

    @Test
    void getUserByKeyWord_validUUID_usesIdSearch() {

        UUID keywordId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        Pageable pageable = Pageable.ofSize(10);

        Page<User> expected =
                new PageImpl<>(List.of(user));

        when(userRepository.findByIdAndIdNot(
                keywordId,
                currentUserId,
                pageable
        )).thenReturn(expected);

        Page<User> result =
                userService.getUserByKeyWord(
                        keywordId.toString(),
                        pageable,
                        currentUserId
                );

        assertSame(expected, result);

        verify(userRepository)
                .findByIdAndIdNot(
                        keywordId,
                        currentUserId,
                        pageable
                );

        verify(userRepository, never())
                .findByUsernameAndIdNot(
                        anyString(),
                        any(),
                        any()
                );
    }

    @Test
    void getUserByKeyWord_invalidUUID_usesUsernameSearch() {

        UUID currentUserId = UUID.randomUUID();

        Pageable pageable = Pageable.ofSize(10);

        Page<User> expected =
                new PageImpl<>(List.of(user));

        when(userRepository.findByUsernameAndIdNot(
                "john123",
                pageable,
                currentUserId
        )).thenReturn(expected);

        Page<User> result =
                userService.getUserByKeyWord(
                        "john123",
                        pageable,
                        currentUserId
                );

        assertSame(expected, result);

        verify(userRepository)
                .findByUsernameAndIdNot(
                        "john123",
                        pageable,
                        currentUserId
                );
    }

    @Test
    void changeUserRole_adminChangesUserToUser_successfully() {

        User admin = User.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        User userToChange = User.builder()
                .id(UUID.randomUUID())
                .username("john123")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userToChange.getId()))
                .thenReturn(Optional.of(userToChange));

        when(userRepository.findByUsernameOrEmail(
                "admin",
                "admin"
        )).thenReturn(Optional.of(admin));

        userService.changeUserRole(
                "admin",
                Role.USER,
                userToChange.getId()
        );

        assertEquals(Role.USER, userToChange.getRole());
        assertNotNull(userToChange.getUpDateOn());

        verify(userRepository).save(userToChange);
    }

    @Test
    void changeUserRole_userCannotChangeRole_throwsException() {

        User normalUser = User.builder()
                .username("john")
                .role(Role.USER)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("target")
                .role(Role.USER)
                .build();

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        when(userRepository.findByUsernameOrEmail(
                "john",
                "john"
        )).thenReturn(Optional.of(normalUser));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserRole(
                        "john",
                        Role.ADMIN,
                        target.getId()
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserRole_adminCannotPromoteUserToAdmin_throwsException() {

        User admin = User.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("target")
                .role(Role.USER)
                .build();

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        when(userRepository.findByUsernameOrEmail(
                "admin",
                "admin"
        )).thenReturn(Optional.of(admin));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserRole(
                        "admin",
                        Role.ADMIN,
                        target.getId()
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserRole_cannotAssignSuperAdmin_throwsException() {

        User admin = User.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("target")
                .role(Role.USER)
                .build();

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        when(userRepository.findByUsernameOrEmail(
                "admin",
                "admin"
        )).thenReturn(Optional.of(admin));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserRole(
                        "admin",
                        Role.SUPER_ADMIN,
                        target.getId()
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserRole_cannotChangeSuperAdmin_throwsException() {

        User superAdmin = User.builder()
                .username("super")
                .role(Role.ADMIN)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("target")
                .role(Role.SUPER_ADMIN)
                .build();

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        when(userRepository.findByUsernameOrEmail(
                "super",
                "super"
        )).thenReturn(Optional.of(superAdmin));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserRole(
                        "super",
                        Role.USER,
                        target.getId()
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserActivationStatus_adminDeactivatesUser() {

        User admin = User.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .role(Role.USER)
                .isActive(true)
                .build();

        when(userRepository.findByUsernameOrEmail(
                "admin",
                "admin"
        )).thenReturn(Optional.of(admin));

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        userService.changeUserActivationStatus(
                "admin",
                target.getId()
        );

        assertFalse(target.isActive());

        verify(userRepository).save(target);
    }

    @Test
    void changeUserActivationStatus_adminActivatesUser() {

        User admin = User.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .role(Role.USER)
                .isActive(false)
                .build();

        when(userRepository.findByUsernameOrEmail(
                "admin",
                "admin"
        )).thenReturn(Optional.of(admin));

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        userService.changeUserActivationStatus(
                "admin",
                target.getId()
        );

        assertTrue(target.isActive());

        verify(userRepository).save(target);
    }

    @Test
    void changeUserActivationStatus_normalUserCannotChangeStatus() {

        User normalUser = User.builder()
                .username("john")
                .role(Role.USER)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("target")
                .role(Role.USER)
                .isActive(true)
                .build();

        when(userRepository.findByUsernameOrEmail(
                "john",
                "john"
        )).thenReturn(Optional.of(normalUser));

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserActivationStatus(
                        "john",
                        target.getId()
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserActivationStatus_adminCannotChangeSuperAdmin() {

        User admin = User.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("super")
                .role(Role.SUPER_ADMIN)
                .isActive(true)
                .build();

        when(userRepository.findByUsernameOrEmail(
                "admin",
                "admin"
        )).thenReturn(Optional.of(admin));

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserActivationStatus(
                        "admin",
                        target.getId()
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserActivationStatus_adminCannotChangeAnotherAdmin() {

        User admin = User.builder()
                .username("admin1")
                .role(Role.ADMIN)
                .build();

        User target = User.builder()
                .id(UUID.randomUUID())
                .username("admin2")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        when(userRepository.findByUsernameOrEmail(
                "admin1",
                "admin1"
        )).thenReturn(Optional.of(admin));

        when(userRepository.findById(target.getId()))
                .thenReturn(Optional.of(target));

        assertThrows(
                UnauthorizedRoleChangeException.class,
                () -> userService.changeUserActivationStatus(
                        "admin1",
                        target.getId()
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void getCurrentProfileData_userExists_returnsCorrectData() {

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        UpdateProfileRequest result =
                userService.getCurrentProfileData(user.getId());

        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(user.getBirthDate(), result.getBirthDate());
        assertEquals(user.getPictureUrl(), result.getPictureUrl());
    }

    @Test
    void getCurrentProfileData_userDoesNotExist_throwsException() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getCurrentProfileData(id)
        );
    }

    @Test
    void getUserByUsername_userExists_returnsUser() {

        when(userRepository.findByUsername("john123"))
                .thenReturn(Optional.of(user));

        User result =
                userService.getUserByUsername("john123");

        assertSame(user, result);
    }

    @Test
    void getUserByUsername_userDoesNotExist_throwsException() {

        when(userRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByUsername("john123")
        );
    }

    @Test
    void getUserByUsernameOrEmail_userExists_returnsUser() {

        when(userRepository.findByUsernameOrEmail(
                "john123",
                "john123"
        )).thenReturn(Optional.of(user));

        User result =
                userService.getUserByUsernameOrEmail("john123");

        assertSame(user, result);
    }

    @Test
    void getUserByUsernameOrEmail_userDoesNotExist_throwsException() {

        when(userRepository.findByUsernameOrEmail(
                "unknown",
                "unknown"
        )).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByUsernameOrEmail("unknown")
        );
    }

    @Test
    void loadUserByUsername_userExists_returnsAuthenticationMetadata() {

        when(userRepository.findByUsernameOrEmail(
                "john123",
                "john123"
        )).thenReturn(Optional.of(user));

        UserDetails result =
                userService.loadUserByUsername("john123");

        assertInstanceOf(
                AuthenticationMetadata.class,
                result
        );

        assertEquals("john123", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    void loadUserByUsername_userDoesNotExist_throwsException() {

        when(userRepository.findByUsernameOrEmail(
                "unknown",
                "unknown"
        )).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown")
        );
    }

    @Test
    void isAdmin_admin_returnsTrue() {

        user.setRole(Role.ADMIN);

        assertTrue(userService.isAdmin(user));
    }

    @Test
    void isAdmin_superAdmin_returnsTrue() {

        user.setRole(Role.SUPER_ADMIN);

        assertTrue(userService.isAdmin(user));
    }

    @Test
    void isAdmin_normalUser_returnsFalse() {

        user.setRole(Role.USER);

        assertFalse(userService.isAdmin(user));
    }

    @Test
    void isCanAddReviewAndComment_withoutSubscription_returnsFalse() {

        user.setSubscription(null);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertFalse(
                userService.isCanAddReviewAndComment(user.getId())
        );
    }

    @Test
    void isCanAddReviewAndComment_withValidSubscription_returnsTrue() {

        Subscription subscription = Subscription.builder()
                .expirationDate(LocalDateTime.now().plusDays(10))
                .active(true)
                .build();

        user.setSubscription(subscription);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertTrue(
                userService.isCanAddReviewAndComment(user.getId())
        );
    }

    @Test
    void isCanAddReviewAndComment_withExpiredSubscription_returnsFalse() {

        Subscription subscription = Subscription.builder()
                .expirationDate(LocalDateTime.now().minusDays(1))
                .active(true)
                .build();

        user.setSubscription(subscription);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertFalse(
                userService.isCanAddReviewAndComment(user.getId())
        );
    }

    @Test
    void getUserHeaderDto_userExists_returnsCorrectDto() {

        Subscription subscription = Subscription.builder()
                .planCode("MONTHLY")
                .active(true)
                .expirationDate(LocalDateTime.now().plusDays(10))
                .build();

        user.setSubscription(subscription);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        UserHeaderDto result =
                userService.getUserHeaderDto(user.getId());

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getPictureUrl(), result.getPictureUrl());
        assertEquals(user.getRole(), result.getRole());
        assertEquals(user.getCreatedOn(), result.getCreatedOn());
        assertEquals(subscription, result.getSubscription());

        assertTrue(result.isCanAddMovie());
        assertTrue(result.isCanAddReviewAndComment());
    }

    @Test
    void getUserHeaderDto_userDoesNotExist_throwsException() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserHeaderDto(id)
        );
    }

    @Test
    void getUsersBySubscriptionExpirationDate_returnsUsers() {

        LocalDateTime start =
                LocalDateTime.of(2026, 8, 25, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 8, 25, 23, 59);

        List<User> expected = List.of(user);

        when(userRepository.findBySubscriptionExpirationDateBetween(
                start,
                end
        )).thenReturn(expected);

        List<User> result =
                userService.getUsersBySubscriptionExpirationDate(
                        start,
                        end
                );

        assertSame(expected, result);

        verify(userRepository)
                .findBySubscriptionExpirationDateBetween(
                        start,
                        end
                );
    }
}