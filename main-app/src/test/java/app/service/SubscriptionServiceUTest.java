package app.service;

import org.app.config.SubscriptionProperties;
import org.app.exception.CardValidationException;
import org.app.exception.UserAlreadyHasSubscriptionException;
import org.app.exception.UserNotFoundException;
import org.app.notification.service.NotificationService;
import org.app.subscription.model.Subscription;
import org.app.subscription.repository.SubscriptionRepository;
import org.app.subscription.service.SubscriptionService;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.app.web.dto.PurchaseSubscriptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceUTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionProperties subscriptionProperties;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SubscriptionProperties.SubscriptionPlan subscriptionPlan;

    private SubscriptionService subscriptionService;

    private User user;

    private PurchaseSubscriptionDto dto;

    @BeforeEach
    void setUp() {

        subscriptionService = new SubscriptionService(
                subscriptionRepository,
                userRepository,
                subscriptionProperties,
                notificationService
        );

        user = User.builder()
                .username("john")
                .email("john@gmail.com")
                .password("password")
                .role(Role.USER)
                .build();

        dto = PurchaseSubscriptionDto.builder()
                .planCode("MONTHLY")
                .cardNumber("1234567812345678")
                .expiry("12/28")
                .cvv("123")
                .build();
    }

    @Test
    void addPlan_validData_createsSubscriptionAndNotification() {

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        when(subscriptionProperties.getPlans())
                .thenReturn(List.of(subscriptionPlan));

        when(subscriptionPlan.getCode())
                .thenReturn("MONTHLY");

        when(subscriptionPlan.getPrice())
                .thenReturn(new BigDecimal("9.99"));

        when(subscriptionPlan.getPeriod())
                .thenReturn(30);

        subscriptionService.addPlan(dto, "john");

        ArgumentCaptor<Subscription> subscriptionCaptor =
                ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionRepository).save(subscriptionCaptor.capture());

        Subscription savedSubscription = subscriptionCaptor.getValue();

        assertEquals("MONTHLY", savedSubscription.getPlanCode());
        assertEquals(new BigDecimal("9.99"), savedSubscription.getPrice());
        assertEquals(30, savedSubscription.getPeriod());
        assertTrue(savedSubscription.isActive());

        assertNotNull(savedSubscription.getStartDate());
        assertNotNull(savedSubscription.getExpirationDate());

        assertEquals(savedSubscription, user.getSubscription());

        verify(userRepository).save(user);

        verify(notificationService).createNotification(
                eq(user),
                anyString(),
                eq("SUBSCRIPTION_PURCHASE")
        );
    }

    @Test
    void addPlan_invalidCardNumber_throwsException() {

        dto.setCardNumber("123");

        assertThrows(
                CardValidationException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void addPlan_nullCardNumber_throwsException() {

        dto.setCardNumber(null);

        assertThrows(
                CardValidationException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void addPlan_invalidExpiry_throwsException() {

        dto.setExpiry("13/28");

        assertThrows(
                CardValidationException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void addPlan_nullExpiry_throwsException() {

        dto.setExpiry(null);

        assertThrows(
                CardValidationException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void addPlan_invalidCvv_throwsException() {

        dto.setCvv("12");

        assertThrows(
                CardValidationException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void addPlan_nullCvv_throwsException() {

        dto.setCvv(null);

        assertThrows(
                CardValidationException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void addPlan_userDoesNotExist_throwsException() {

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verify(userRepository)
                .findByUsernameOrEmail("john", "john");

        verifyNoInteractions(subscriptionRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void addPlan_userAlreadyHasActiveSubscription_throwsException() {

        Subscription existingSubscription = Subscription.builder()
                .planCode("YEARLY")
                .active(true)
                .expirationDate(LocalDateTime.now().plusDays(100))
                .build();

        user.setSubscription(existingSubscription);

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        UserAlreadyHasSubscriptionException exception =
                assertThrows(
                        UserAlreadyHasSubscriptionException.class,
                        () -> subscriptionService.addPlan(dto, "john")
                );

        assertTrue(exception.getMessage().contains("You already have an active plan"));

        verifyNoInteractions(subscriptionRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void addPlan_userHasInactiveSubscription_createsNewSubscription() {

        Subscription existingSubscription = Subscription.builder()
                .planCode("OLD")
                .active(false)
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        user.setSubscription(existingSubscription);

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        when(subscriptionProperties.getPlans())
                .thenReturn(List.of(subscriptionPlan));

        when(subscriptionPlan.getCode())
                .thenReturn("MONTHLY");

        when(subscriptionPlan.getPrice())
                .thenReturn(new BigDecimal("9.99"));

        when(subscriptionPlan.getPeriod())
                .thenReturn(30);

        subscriptionService.addPlan(dto, "john");

        verify(subscriptionRepository).save(any(Subscription.class));
        verify(userRepository).save(user);
        verify(notificationService).createNotification(
                eq(user),
                anyString(),
                eq("SUBSCRIPTION_PURCHASE")
        );
    }

    @Test
    void addPlan_invalidPlanCode_throwsException() {

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        when(subscriptionProperties.getPlans())
                .thenReturn(List.of(subscriptionPlan));

        when(subscriptionPlan.getCode())
                .thenReturn("YEARLY");

        assertThrows(
                RuntimeException.class,
                () -> subscriptionService.addPlan(dto, "john")
        );

        verify(subscriptionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void addPlan_caseInsensitivePlanCode_findsPlan() {

        dto.setPlanCode("monthly");

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        when(subscriptionProperties.getPlans())
                .thenReturn(List.of(subscriptionPlan));

        when(subscriptionPlan.getCode())
                .thenReturn("MONTHLY");

        when(subscriptionPlan.getPrice())
                .thenReturn(new BigDecimal("9.99"));

        when(subscriptionPlan.getPeriod())
                .thenReturn(30);

        subscriptionService.addPlan(dto, "john");

        ArgumentCaptor<Subscription> captor =
                ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionRepository).save(captor.capture());

        assertEquals("MONTHLY", captor.getValue().getPlanCode());
    }

    @Test
    void addPlan_setsExpirationDateBasedOnPeriod() {

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        when(subscriptionProperties.getPlans())
                .thenReturn(List.of(subscriptionPlan));

        when(subscriptionPlan.getCode())
                .thenReturn("MONTHLY");

        when(subscriptionPlan.getPrice())
                .thenReturn(new BigDecimal("9.99"));

        when(subscriptionPlan.getPeriod())
                .thenReturn(30);

        LocalDateTime before = LocalDateTime.now();

        subscriptionService.addPlan(dto, "john");

        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<Subscription> captor =
                ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionRepository).save(captor.capture());

        Subscription savedSubscription = captor.getValue();

        assertNotNull(savedSubscription.getStartDate());

        assertFalse(savedSubscription.getExpirationDate().isAfter(
                after.plusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0)
        ));

        assertTrue(savedSubscription.getExpirationDate().isAfter(
                before.plusDays(30).minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        ));
    }

    @Test
    void addPlan_notificationContainsSubscriptionDates() {

        when(userRepository.findByUsernameOrEmail("john", "john"))
                .thenReturn(Optional.of(user));

        when(subscriptionProperties.getPlans())
                .thenReturn(List.of(subscriptionPlan));

        when(subscriptionPlan.getCode())
                .thenReturn("MONTHLY");

        when(subscriptionPlan.getPrice())
                .thenReturn(new BigDecimal("9.99"));

        when(subscriptionPlan.getPeriod())
                .thenReturn(30);

        subscriptionService.addPlan(dto, "john");

        ArgumentCaptor<String> messageCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(notificationService).createNotification(
                eq(user),
                messageCaptor.capture(),
                eq("SUBSCRIPTION_PURCHASE")
        );

        String message = messageCaptor.getValue();

        assertTrue(message.contains("Successful subscription purchase!"));
        assertTrue(message.contains("Started on:"));
        assertTrue(message.contains("Expires on:"));
    }
}