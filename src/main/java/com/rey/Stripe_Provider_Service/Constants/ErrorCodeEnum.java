package com.rey.Stripe_Provider_Service.Constants;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

    GENERIC_ERROR("1000","Ooops, Something went wrong!!!"),
    INVALID_REQUEST("1001","Request is Invalid");
    private String errorCode;
    private String errorMessage;

    ErrorCodeEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
