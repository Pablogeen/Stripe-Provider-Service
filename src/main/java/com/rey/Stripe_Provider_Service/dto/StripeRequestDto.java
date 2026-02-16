package com.rey.Stripe_Provider_Service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StripeRequestDto {



    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Amount must have up to 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "Currency must be a 3-letter ISO-4217 code, uppercase or lowercase")
    private String currency;

}
