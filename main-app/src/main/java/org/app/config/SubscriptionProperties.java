package org.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Data
@Configuration
@ConfigurationProperties(prefix = "subscriptions-plans")
public class SubscriptionProperties {

    private List<SubscriptionPlan> plans = new ArrayList<>();

    @Data
    public static class SubscriptionPlan {

        private String code;

        private BigDecimal price;

        private Integer period;

        private boolean active;

    }
}
