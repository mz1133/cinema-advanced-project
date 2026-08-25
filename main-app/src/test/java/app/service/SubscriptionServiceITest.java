package app.service;

import org.app.Application;
import org.app.config.SubscriptionProperties;

import org.app.subscription.repository.SubscriptionRepository;
import org.app.subscription.service.SubscriptionService;
import org.app.user.model.Role;
import org.app.user.model.User;
import org.app.user.repository.UserRepository;
import org.app.web.dto.PurchaseSubscriptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = Application.class)
class SubscriptionServiceITest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionProperties subscriptionProperties;

    private User testUser;

    @BeforeEach
    void setUp() {
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("pesho")
                .password("password123")
                .email("pesho@example.com")
                .role(Role.USER)
                .createdOn(LocalDateTime.now())
                .isActive(true)
                .build();

        testUser = userRepository.save(testUser);

        SubscriptionProperties.SubscriptionPlan plan = new SubscriptionProperties.SubscriptionPlan();
        plan.setCode("PREMIUM");
        plan.setPrice(BigDecimal.valueOf(9.99));
        plan.setPeriod(30);
        plan.setActive(true);

        subscriptionProperties.getPlans().clear();
        subscriptionProperties.getPlans().add(plan);
    }

    @Test
    void addPlan_shouldSucceedWhenValidData() {
        PurchaseSubscriptionDto dto = PurchaseSubscriptionDto.builder()
                .planCode("PREMIUM")
                .cardNumber("1234567812345678")
                .expiry("12/28")
                .cvv("123")
                .build();

        subscriptionService.addPlan(dto, "pesho");

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotNull(updatedUser.getSubscription());
        assertEquals("PREMIUM", updatedUser.getSubscription().getPlanCode());
        assertTrue(updatedUser.getSubscription().isActive());
    }


}