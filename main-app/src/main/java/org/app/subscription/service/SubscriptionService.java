package org.app.subscription.service;

import org.app.config.SubscriptionProperties;
import org.app.exception.AlreadyHavePlanException;
import org.app.exception.UserNotFoundException;
import org.app.subscription.model.Subscription;
import org.app.subscription.repository.SubscriptionRepository;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final static String ERROR_MESSAGE_USER_NOT_FOUD = "User not found";
    private final static String ERROR_MESSAGE_SUBSCRIPTION_PLAN_NOT_FOUD = "Subscription plan not found";
    private final static String ERROR_MESSAGE_ALREADY_HAVE_PLAN = "You already have an active plan valid until: %s";

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionProperties subscriptionProperties;


    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository, SubscriptionProperties subscriptionProperties) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.subscriptionProperties = subscriptionProperties;
    }

    @Transactional
    public void addPlan(UUID userId, String planCode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ERROR_MESSAGE_USER_NOT_FOUD));

        isHaveAlreadySubscription(user);

        Subscription newSubscription = buildSubscription(getSubscriptionPlan(planCode));

        subscriptionRepository.save(newSubscription);

        user.setSubscription(newSubscription);

        userRepository.save(user);
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

        if (user.getSubscription() != null) {
            throw new AlreadyHavePlanException(ERROR_MESSAGE_ALREADY_HAVE_PLAN
                    .formatted(user.getSubscription().getExpirationDate()));
        }
    }


}
