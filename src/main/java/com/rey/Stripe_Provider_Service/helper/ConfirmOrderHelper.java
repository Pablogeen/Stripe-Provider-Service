package com.rey.Stripe_Provider_Service.helper;

import com.rey.Stripe_Provider_Service.Constants.Constant;
import com.rey.Stripe_Provider_Service.http.HttpRequest;
import com.rey.Stripe_Provider_Service.config.StripeProperties;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmOrderHelper {

    private final StripeProperties stripeProperties;


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
}
