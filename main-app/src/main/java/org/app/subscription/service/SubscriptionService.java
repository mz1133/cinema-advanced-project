package org.app.subscription.service;

import lombok.extern.slf4j.Slf4j;
import org.app.config.SubscriptionProperties;
import org.app.exception.UserAlreadyHasSubscriptionException;
import org.app.exception.CardValidationException;
import org.app.exception.UserNotFoundException;
import org.app.notification.service.NotificationService;
import org.app.subscription.model.Subscription;
import org.app.subscription.repository.SubscriptionRepository;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.app.web.dto.PurchaseSubscriptionDto;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Slf4j
@Service
public class SubscriptionService {

    private final static String ERROR_MESSAGE_USER_NOT_FOUD = "User not found";
    private final static String ERROR_MESSAGE_SUBSCRIPTION_PLAN_NOT_FOUD = "Subscription plan not found";
    private final static String ERROR_MESSAGE_ALREADY_HAVE_PLAN = "You already have an active plan valid until: %s";
    private final static String NOTIFICATION_MESSAGE_FOR_SUCCESS_PURCHASE_SUBSCRIPTION_PLAN = "Successful subscription purchase! Started on: %s, Expires on: %s";
    private final static String TYPE_NOTIFICATION_NAME_TITLE = "SUBSCRIPTION_PURCHASE";

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionProperties subscriptionProperties;
    private final NotificationService  notificationService;


    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository, SubscriptionProperties subscriptionProperties, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.subscriptionProperties = subscriptionProperties;
        this.notificationService = notificationService;
    }

    @Transactional
    public void addPlan(PurchaseSubscriptionDto dto, String usernameOrEmail) {

        if (dto.getCardNumber() == null || !dto.getCardNumber().matches("^[0-9]{16}$")) {
            throw new CardValidationException("Card number must contain exactly 16 digits.");
        }

        if (dto.getExpiry() == null || !dto.getExpiry().matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            throw new CardValidationException("Invalid expiration date format (MM/YY).");
        }

        if (dto.getCvv() == null || !dto.getCvv().matches("^[0-9]{3,4}$")) {
            throw new CardValidationException("CVV must contain 3 or 4 digits.");
        }

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException(ERROR_MESSAGE_USER_NOT_FOUD));

        isHaveAlreadySubscription(user);

        Subscription newSubscription = buildSubscription(getSubscriptionPlan(dto.getPlanCode()));

        subscriptionRepository.save(newSubscription);

        user.setSubscription(newSubscription);

        userRepository.save(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


        String formattedStart = newSubscription.getStartDate().format(formatter);



        String message = String.format(NOTIFICATION_MESSAGE_FOR_SUCCESS_PURCHASE_SUBSCRIPTION_PLAN,
                formattedStart, newSubscription.getExpirationDate());

        notificationService.createNotification(user, message, TYPE_NOTIFICATION_NAME_TITLE);

        log.info("Subscription plan id: {%s} saved successfully to user username:{%s}".formatted(newSubscription.getId(), user.getUsername()));

    }

    @NonNull
    private SubscriptionProperties.SubscriptionPlan getSubscriptionPlan(String planCode) {

        SubscriptionProperties.SubscriptionPlan subPlan = subscriptionProperties
                .getPlans()
                .stream()
                .filter(p -> p.getCode().equalsIgnoreCase(planCode))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(ERROR_MESSAGE_SUBSCRIPTION_PLAN_NOT_FOUD));
        return subPlan;
    }

    private Subscription buildSubscription(SubscriptionProperties.SubscriptionPlan subPlan) {

        return Subscription.builder()
                .price(subPlan.getPrice())
                .startDate(LocalDateTime.now())
                .planCode(subPlan.getCode())
                .active(true)
                .expirationDate(
                        LocalDateTime.now()
                                .plusDays(subPlan.getPeriod())
                                .withHour(0)
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0))
                .period(subPlan.getPeriod())
                .build();
    }

    private static void isHaveAlreadySubscription(User user) {

        if (user.getSubscription() != null && user.getSubscription().isActive()) {
            throw new UserAlreadyHasSubscriptionException(ERROR_MESSAGE_ALREADY_HAVE_PLAN
                    .formatted(user.getSubscription().getExpirationDate()));
        }
    }






}
