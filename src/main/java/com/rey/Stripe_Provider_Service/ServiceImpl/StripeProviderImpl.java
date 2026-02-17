package com.rey.Stripe_Provider_Service.ServiceImpl;

import com.rey.Stripe_Provider_Service.Http.HttpRequest;
import com.rey.Stripe_Provider_Service.Http.HttpServiceEngine;
import com.rey.Stripe_Provider_Service.Service.StripeServiceInterface;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import com.rey.Stripe_Provider_Service.helper.CreateOrderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeProviderImpl implements StripeServiceInterface {

    private final CreateOrderHelper stripeHelper;
    private final HttpServiceEngine httpServiceEngine;

    @Override
    public StripeResponseDto createStripeOrderRequest(StripeRequestDto requestDto) {

        HttpRequest httpRequest = stripeHelper.prepareHttpRequest(requestDto);
        log.info("Prepared httpRequest to make call to Stripe to create Order: {}",httpRequest);

        ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
        log.info("Call made to stripe for response");

        StripeResponseDto response = stripeHelper.toCreateOrderResponse(httpResponse);
        log.info("Handled Stripe response: {}",response);

        return response;
    }
}
