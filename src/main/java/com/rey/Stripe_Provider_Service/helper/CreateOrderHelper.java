package com.rey.Stripe_Provider_Service.helper;

import com.rey.Stripe_Provider_Service.Constants.Constant;
import com.rey.Stripe_Provider_Service.Constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.Exception.StripeProviderException;
import com.rey.Stripe_Provider_Service.http.HttpRequest;
import com.rey.Stripe_Provider_Service.config.StripeProperties;
import com.rey.Stripe_Provider_Service.dto.StripeErrorResponse;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
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

    public HttpRequest prepareHttpRequest(StripeRequestDto requestDto) {

        String apiKey = stripeProperties.getApiKey();
        log.info("Api key: {}",apiKey);
        String createOrderUrl = stripeProperties.getCreateOrderUrl();
        log.info("Stripe Url: {}",createOrderUrl);

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

        log.info("Prepared http Request: {}",httpRequest);
        return httpRequest;
    }

    public StripeResponseDto toCreateOrderResponse(ResponseEntity<String> httpResponse) {
        log.info("Preparing response");

        if (httpResponse.getStatusCode().is2xxSuccessful()){
            log.info("Request has been a success");

            StripeResponseDto orderResponse =
                        jsonUtil.convertJsonStringToJavaObject(httpResponse.getBody(), StripeResponseDto.class);
            log.info("Converted jsonString to java object: {}", orderResponse);

            if (orderResponse !=null &&
                        orderResponse.getId() != null &&
                                orderResponse.getStatus() != null &&
                                    orderResponse.getClientSecret() != null){
                log.info("Order created successfully with PAYER_ACTION_REQUIRED status: {}", orderResponse);
                return orderResponse;
            }else{
                log.error("Order creation failed or incomplete details received OrderResponse: {}", orderResponse);
            }

        }

        if(httpResponse.getStatusCode().is4xxClientError() ||
                                            httpResponse.getStatusCode().is5xxServerError()){
            log.error("Gotten a 4xx or a 5xx error");

            StripeErrorResponse errorResponse =
                    jsonUtil.convertJsonStringToJavaObject(httpResponse.getBody(), StripeErrorResponse.class);
            log.info("Converted error string to java object: {}",errorResponse);

                String errorCode = ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode();
                String errorMessage = errorResponse.getMessage();

                throw new StripeProviderException(
                        errorCode,
                        errorMessage,
                       HttpStatus.valueOf(
                               httpResponse.getStatusCode().value()
                       )
                );
        }

        throw new StripeProviderException(
                ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(),
                ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
