package com.rey.Stripe_Provider_Service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StripeProviderException extends RuntimeException{

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public StripeProviderException( String errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus=httpStatus;
    }


}
