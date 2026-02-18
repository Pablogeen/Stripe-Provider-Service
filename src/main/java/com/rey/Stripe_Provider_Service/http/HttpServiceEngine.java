package com.rey.Stripe_Provider_Service.http;

import com.rey.Stripe_Provider_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.exception.StripeProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpServiceEngine {

    private final RestClient restClient;

    public ResponseEntity<String> makeHttpCall(HttpRequest request) {

        try {
            ResponseEntity<String> httpResponse = restClient
                    .method(request.getHttpMethod())
                    .uri(request.getUrl())
                    .headers(restClientHeader -> restClientHeader.addAll(request.getHttpHeaders()))
                    .body(request.getBody())
                    .retrieve()
                    .toEntity(String.class);

            return httpResponse;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error response received: {}", e.getMessage(), e);

            //If the error is gateway timeout or service unavailable, throw PaypalProviderError
            if (e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT || e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new StripeProviderException(
                        ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(),
                        ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(),
                        HttpStatus.SERVICE_UNAVAILABLE);
            }

            //return ResponseEntity with error details
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

            //Any response will belong here - No Timeout
        } catch (Exception e) {
            log.error("Exception while preparing form data: {}", e.getMessage(), e);

            throw new StripeProviderException(
                    ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(),
                    ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);

        }
    }
}
