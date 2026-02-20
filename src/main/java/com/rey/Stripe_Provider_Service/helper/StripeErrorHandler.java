package com.rey.Stripe_Provider_Service.helper;

import com.rey.Stripe_Provider_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.dto.StripeErrorResponse;
import com.rey.Stripe_Provider_Service.exception.StripeProviderException;
import com.rey.Stripe_Provider_Service.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeErrorHandler {

    private final JsonUtil jsonUtil;

    public void handleStripeError(ResponseEntity<String> httpResponse) {

        HttpStatusCode statusCode = httpResponse.getStatusCode();
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode.value());
        String responseBody = httpResponse.getBody();
        log.error("Stripe error response - Status: {}, Body: {}", statusCode, responseBody);

        try {
            StripeErrorResponse errorResponse =
                    jsonUtil.convertJsonStringToJavaObject(responseBody, StripeErrorResponse.class);

            // Check if error object exists
            if (errorResponse == null || errorResponse.getError() == null) {
                throw new StripeProviderException(
                        ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(),
                        ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorMessage(),
                        httpStatus);
            }

            StripeErrorResponse.StripeError error = errorResponse.getError();

            // Get error code and message
            String errorCode = getErrorCode(error, statusCode);
            String errorMessage = getErrorMessage(error);
            log.error("Stripe Error - Type: {}, Code: {}, Message: {}",
                    error.getType(), errorCode, errorMessage);

            // Throw exception - will be caught by GlobalExceptionHandler
            throw new StripeProviderException(
                    errorCode,
                    errorMessage,
                    httpStatus  );

        } catch (StripeProviderException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Stripe error response", e);
            throw new StripeProviderException(
                    ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(),
                    ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String getErrorCode(StripeErrorResponse.StripeError error, HttpStatusCode statusCode) {
        // Use Stripe's error code if available
        if (error.getCode() != null && !error.getCode().isEmpty()) {
            return String.valueOf(switch (error.getCode()) {
                case "payment_intent_unexpected_state" -> ErrorCodeEnum.PAYMENT_INTENT_UNEXPECTED_STATE.getErrorCode();
               case "resource_missing" -> ErrorCodeEnum.RESOURCE_MISSING.getErrorCode();
                case "api_error" -> ErrorCodeEnum.API_ERROR.getErrorCode();
                case "authentication_error" -> ErrorCodeEnum.AUTHENTICATION_ERROR.getErrorCode();
                case "rate_limit_error" -> ErrorCodeEnum.RATE_LIMIT_ERROR.getErrorCode();
                default -> mapStatusToErrorCode(statusCode);
            });
        }

        // Fallback: map by error type
        if (error.getType() != null) {
            return String.valueOf(switch (error.getType()) {
                case "card_error" -> ErrorCodeEnum.CARD_ERROR.getErrorCode();
                case "invalid_request_error" -> ErrorCodeEnum.INVALID_REQUEST.getErrorCode();
                case "api_error" -> ErrorCodeEnum.API_ERROR.getErrorCode();
                case "authentication_error" -> ErrorCodeEnum.AUTHENTICATION_ERROR.getErrorCode();
                case "rate_limit_error" -> ErrorCodeEnum.RATE_LIMIT_ERROR.getErrorCode();
                case "resource_missing" -> ErrorCodeEnum.RESOURCE_MISSING.getErrorCode();
                default -> mapStatusToErrorCode(statusCode);
            });
        }

        // Final fallback: map by status code
        return String.valueOf(mapStatusToErrorCode(statusCode));
    }

    private String getErrorMessage(StripeErrorResponse.StripeError error) {
        // Maintain Stripe's error message because it is user-friendly
        if (error.getMessage() != null && !error.getMessage().isEmpty()) {
            return error.getMessage();
        }

        // Fallback message
        return ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorMessage();
    }

    private HttpStatus mapStatusToErrorCode(HttpStatusCode statusCode) {
        int status = statusCode.value();
        return switch (status) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 402 -> HttpStatus.PAYMENT_REQUIRED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 429 -> HttpStatus.REQUEST_TIMEOUT;
            case 500, 502, 503, 504 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }
}