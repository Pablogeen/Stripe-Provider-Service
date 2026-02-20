package com.rey.Stripe_Provider_Service.helper;

import com.rey.Stripe_Provider_Service.config.StripeProperties;
import com.rey.Stripe_Provider_Service.constants.Constant;
import com.rey.Stripe_Provider_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import com.rey.Stripe_Provider_Service.exception.StripeProviderException;
import com.rey.Stripe_Provider_Service.http.HttpRequest;
import com.rey.Stripe_Provider_Service.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderHelper {

    private final StripeProperties stripeProperties;
    private final JsonUtil jsonUtil;
    private final StripeErrorHandler stripeErrorHandler;

    public HttpRequest prepareHttpRequest(StripeRequestDto requestDto) {

        String apiKey = stripeProperties.getApiKey();
        log.info("Api key: {}", apiKey);
        String createOrderUrl = stripeProperties.getCreateOrderUrl();
        log.info("Stripe Url: {}", createOrderUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(Constant.AMOUNT, String.valueOf(requestDto.getAmount()));
        body.add(Constant.CURRENCY, requestDto.getCurrency());
        body.add(Constant.AUTOMATIC_PAYMENT_METHOD, "true");

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpMethod(HttpMethod.POST);
        httpRequest.setHttpHeaders(headers);
        httpRequest.setBody(body);
        httpRequest.setUrl(createOrderUrl);

        log.info("Prepared http Request: {}", httpRequest);
        return httpRequest;
    }

    public StripeResponseDto toCreateOrderResponse(ResponseEntity<String> httpResponse) {
        log.info("Preparing response");

        //success cases
        if (httpResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Request has been a success");

            StripeResponseDto orderResponse =
                    jsonUtil.convertJsonStringToJavaObject(httpResponse.getBody(), StripeResponseDto.class);
            log.info("Converted jsonString to java object: {}", orderResponse);

            if (orderResponse != null &&
                    orderResponse.getId() != null &&
                    orderResponse.getStatus() != null &&
                    orderResponse.getClientSecret() != null) {
                log.info("Order created successfully with PAYER_ACTION_REQUIRED status: {}", orderResponse);
                return orderResponse;
            } else {
                log.error("Order creation failed or incomplete details received OrderResponse: {}", orderResponse);
            }

        }

        //Failure Cases
        if (httpResponse.getStatusCode().is4xxClientError() ||
                httpResponse.getStatusCode().is5xxServerError()) {
            stripeErrorHandler.handleStripeError(httpResponse);
            log.info("Handled Stripe Exception");
        }


        //Retry/Tiemout Cases
        throw new StripeProviderException(
                ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(),
                ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorMessage(),
                HttpStatus.BAD_GATEWAY
        );

    }
}


        // In your service/helper class



