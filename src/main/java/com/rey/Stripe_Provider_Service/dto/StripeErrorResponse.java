package com.rey.Stripe_Provider_Service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StripeErrorResponse {


    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;
}
