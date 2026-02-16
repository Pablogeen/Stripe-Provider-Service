package com.rey.Stripe_Provider_Service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stripe")
@Data
public class StripeProperties {
    private String apiKey;
    private String createOrderUrl;
}