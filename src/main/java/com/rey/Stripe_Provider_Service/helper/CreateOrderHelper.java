package com.rey.Stripe_Provider_Service.helper;

import com.rey.Stripe_Provider_Service.Constants.Constant;
import com.rey.Stripe_Provider_Service.Http.HttpRequest;
import com.rey.Stripe_Provider_Service.config.StripeProperties;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
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
public class CreateOrderHelper {

    private final StripeProperties stripeProperties;

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
}
