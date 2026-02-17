package com.rey.Stripe_Provider_Service.Constants;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

    GENERIC_ERROR("1000","Ooops, Something Went Wrong!!!"),
    INVALID_REQUEST("1001","INVALID REQUEST"),
    STRIPE_SERVICE_UNAVAILABLE("1002","STRIPE SERVICE UNAVAILABLE"),
    STRIPE_UNKNOWN_ERROR("1003","UNKNOWN STRIPE EXCEPTION");
    private String errorCode;
    private String errorMessage;

    ErrorCodeEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
