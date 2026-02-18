package com.rey.Stripe_Provider_Service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class StripeConfirmOrderRequest {

    @NotBlank(message = "Return URL is required")
    @URL(message = "Return URL must be a valid URL")
    private String return_url;
}
