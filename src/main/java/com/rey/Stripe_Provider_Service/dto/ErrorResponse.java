package com.rey.Stripe_Provider_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ErrorResponse {

    private String errorCode;
    private String errorMessage;

    public ErrorResponse(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
