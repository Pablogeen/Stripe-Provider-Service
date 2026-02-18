package com.rey.Stripe_Provider_Service.helper;

import com.rey.Stripe_Provider_Service.Constants.Constant;
import com.rey.Stripe_Provider_Service.Constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.Exception.StripeProviderException;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderResponse;
import com.rey.Stripe_Provider_Service.dto.StripeErrorResponse;
import com.rey.Stripe_Provider_Service.http.HttpRequest;
import com.rey.Stripe_Provider_Service.config.StripeProperties;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
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
public class ConfirmOrderHelper {

    private final StripeProperties stripeProperties;
    private final JsonUtil jsonUtil;


    public HttpRequest prepareHttpRequest(String orderId, StripeConfirmOrderRequest orderRequest) {

        String confirmOrderUrl = stripeProperties.getConfirmOrderUrl();
        log.info("Gotten the confirm Order Url from property file: {}",confirmOrderUrl);

        String modifiedUrl = String.format(confirmOrderUrl, orderId);
        log.info("Modified the confirmOrderUrl with the OrderId in the Path: {}",modifiedUrl);

        String apiKey = stripeProperties.getApiKey();
        log.info("Api Key successfully retrieved: {}",apiKey);


        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(Constant.PAYMENT_METHOD, Constant.PM_VISA_CARD);
        body.add(Constant.RETURN_URL, orderRequest.getReturn_url());

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpMethod(HttpMethod.POST);
        httpRequest.setHttpHeaders(headers);
        httpRequest.setBody(body);
        httpRequest.setUrl(modifiedUrl);

            return httpRequest;
    }

    public StripeConfirmOrderResponse toCreateConfirmOrderResponse(ResponseEntity<String> httpResponse) {

        //success cases
        if(httpResponse.getStatusCode().is2xxSuccessful()){
            log.info("Response was a success");

            StripeConfirmOrderResponse orderResponse  =
                    jsonUtil.convertJsonStringToJavaObject(httpResponse.getBody(), StripeConfirmOrderResponse.class);
            log.info("Converted Stripe response to Java Object: {}",orderResponse);

            if(orderResponse != null &&
                    orderResponse.getId() != null &&
                        orderResponse.getStatus() != null){
                log.info("Payment Confirmed successfully with SUCCEEDED status: {}", orderResponse);

                return orderResponse;
            }

        }

        //Failure Cases
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
                            httpResponse.getStatusCode().value()));
        }

        //Retry/Tiemout Cases
        throw new StripeProviderException(
                ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(),
                ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorMessage(),
                HttpStatus.BAD_GATEWAY
        );

    }
}
