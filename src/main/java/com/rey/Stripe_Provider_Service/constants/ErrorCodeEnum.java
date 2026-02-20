package com.rey.Stripe_Provider_Service.constants;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

    GENERIC_ERROR("1000","Ooops, Something Went Wrong!!!"),
    INVALID_REQUEST("1001","INVALID REQUEST"),
    STRIPE_SERVICE_UNAVAILABLE("1002","STRIPE SERVICE UNAVAILABLE"),
    STRIPE_UNKNOWN_ERROR("1003","UNKNOWN PROBLEM WHILE PROCESSING PAYMENT"),
    CARD_ERROR("1004","CARD_ERROR"),
    API_ERROR("1005","API_ERROR"),
    RATE_LIMIT_ERROR("1006","RATE_LIMIT_ERROR"),
    AUTHENTICATION_ERROR("1007","AUTHENTICATION_ERROR"),
    RESOURCE_MISSING("1008","RESOURCE_MISSING"),
    INVALID_REQUEST_ERROR("1009","INVALID_REQUEST_ERROR"),
    PAYMENT_INTENT_UNEXPECTED_STATE("1010","Payment_intent_unexpected_state");
    private String errorCode;
    private String errorMessage;

    ErrorCodeEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
