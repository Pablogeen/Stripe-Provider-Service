package com.rey.Stripe_Provider_Service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StripeErrorResponse {

    @JsonProperty("error")
    private StripeError error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StripeError {

        @JsonProperty("type")
        private String type;

        @JsonProperty("message")
        private String message;

        @JsonProperty("code")
        private String code;
    }
}