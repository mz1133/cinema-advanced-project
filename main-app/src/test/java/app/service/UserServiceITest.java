package app.service;

import org.app.Application;
import org.app.exception.UnauthorizedRoleChangeException;
import org.app.exception.ValidationFailedException;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.app.user.service.UserService;
import org.app.web.dto.RegisterRequest;
import org.app.web.dto.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = Application.class)
class UserServiceITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldRegisterSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .confirmPassword("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        userService.register(request);

        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(Role.USER, savedUser.getRole());
        assertTrue(savedUser.isActive());
    }

    @Test
    void register_shouldThrowValidationFailedExceptionWhenPasswordsDoNotMatch() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .confirmPassword("differentpassword")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThrows(ValidationFailedException.class, () -> {
            userService.register(request);
        });
    }

    @Test
    void updateUserProfile_shouldUpdateDetails() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedpassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("UpdatedJohn")
                .lastName("UpdatedDoe")
                .phoneNumber("0888123456")
                .birthDate(LocalDate.of(1995, 5, 15))
                .pictureUrl("https://example.com/pic.png")
                .build();

        userService.updateUserProfile("testuser", request);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("UpdatedJohn", updated.getFirstName());
        assertEquals("UpdatedDoe", updated.getLastName());
        assertEquals("0888123456", updated.getPhoneNumber());
        assertEquals(LocalDate.of(1995, 5, 15), updated.getBirthDate());
    }

    @Test
    void isCanAddMovie_shouldReturnTrueForAdmin() {
        User admin = User.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("encodedpassword")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        admin = userRepository.save(admin);

        boolean canAdd = userService.isCanAddMovie(admin.getId());
        assertTrue(canAdd);
    }

    @Test
    void isCanAddMovie_shouldReturnFalseWhenNoSubscription() {
        User user = User.builder()
                .username("normaluser")
                .email("user@example.com")
                .password("encodedpassword")
                .firstName("Normal")
                .lastName("User")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        boolean canAdd = userService.isCanAddMovie(user.getId());
        assertFalse(canAdd);
    }

    @Test
    void changeUserRole_shouldChangeRoleWhenAdminHasPermission() {
        User admin = User.builder()
                .username("superadmin")
                .email("super@example.com")
                .password("encodedpassword")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.SUPER_ADMIN)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        admin = userRepository.save(admin);

        User targetUser = User.builder()
                .username("targetuser")
                .email("target@example.com")
                .password("encodedpassword")
                .firstName("Target")
                .lastName("User")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        targetUser = userRepository.save(targetUser);

        userService.changeUserRole("superadmin", Role.ADMIN, targetUser.getId());

        User updated = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(Role.ADMIN, updated.getRole());
    }

    @Test
    void changeUserRole_shouldThrowUnauthorizedRoleChangeException() {
        User normalUser = User.builder()
                .username("regularuser")
                .email("regular@example.com")
                .password("encodedpassword")
                .firstName("Regular")
                .lastName("User")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        normalUser = userRepository.save(normalUser);

        User targetUser = User.builder()
                .username("targetuser")
                .email("target@example.com")
                .password("encodedpassword")
                .firstName("Target")
                .lastName("User")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        UUID id = userRepository.save(targetUser).getId();

        assertThrows(UnauthorizedRoleChangeException.class, () -> {
            userService.changeUserRole("regularuser", Role.ADMIN, id);
        });
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        User user = User.builder()
                .username("loginuser")
                .email("login@example.com")
                .password("encodedpassword")
                .firstName("Login")
                .lastName("User")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(user);

        UserDetails userDetails = userService.loadUserByUsername("loginuser");
        assertNotNull(userDetails);
        assertEquals("loginuser", userDetails.getUsername());
    }
    @Test
    void changeUserActivationStatus_shouldThrowExceptionWhenUserIsNotAdmin() {
        User regularUser = User.builder()
                .username("regularuser")
                .email("regular@example.com")
                .password("encodedpassword")
                .firstName("Regular")
                .lastName("User")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(regularUser);

        User targetUser = User.builder()
                .username("targetuser")
                .email("target@example.com")
                .password("encodedpassword")
                .firstName("Target")
                .lastName("User")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(targetUser);

        assertThrows(UnauthorizedRoleChangeException.class, () -> {
            userService.changeUserActivationStatus("regularuser", targetUser.getId());
        });
    }

    @Test
    void changeUserActivationStatus_shouldThrowExceptionWhenAdminTriesToChangeSuperAdmin() {
        User adminUser = User.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("encodedpassword")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(adminUser);

        User superAdminUser = User.builder()
                .username("superadminuser")
                .email("super@example.com")
                .password("encodedpassword")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.SUPER_ADMIN)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(superAdminUser);

        assertThrows(UnauthorizedRoleChangeException.class, () -> {
            userService.changeUserActivationStatus("adminuser", superAdminUser.getId());
        });
    }

    @Test
    void changeUserActivationStatus_shouldThrowExceptionWhenAdminTriesToChangeAnotherAdmin() {
        User adminUser1 = User.builder()
                .username("admin1")
                .email("admin1@example.com")
                .password("encodedpassword")
                .firstName("Admin")
                .lastName("One")
                .role(Role.ADMIN)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(adminUser1);

        User adminUser2 = User.builder()
                .username("admin2")
                .email("admin2@example.com")
                .password("encodedpassword")
                .firstName("Admin")
                .lastName("Two")
                .role(Role.ADMIN)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(adminUser2);

        assertThrows(UnauthorizedRoleChangeException.class, () -> {
            userService.changeUserActivationStatus("admin1", adminUser2.getId());
        });
    }
}