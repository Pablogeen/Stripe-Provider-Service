package com.rey.Stripe_Provider_Service.ServiceImpl;

import com.rey.Stripe_Provider_Service.http.HttpRequest;
import com.rey.Stripe_Provider_Service.http.HttpServiceEngine;
import com.rey.Stripe_Provider_Service.Service.StripeServiceInterface;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import com.rey.Stripe_Provider_Service.helper.ConfirmOrderHelper;
import com.rey.Stripe_Provider_Service.helper.CreateOrderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeProviderImpl implements StripeServiceInterface {

    private final CreateOrderHelper createOrderHelper;
    private final ConfirmOrderHelper confirmOrderHelper;
    private final HttpServiceEngine httpServiceEngine;

    @Override
    public StripeResponseDto createStripeOrderRequest(StripeRequestDto requestDto) {

        HttpRequest httpRequest = createOrderHelper.prepareHttpRequest(requestDto);
        log.info("Prepared httpRequest to make call to Stripe to create Order: {}",httpRequest);

        ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
        log.info("Call made to stripe for response");

        StripeResponseDto response = createOrderHelper.toCreateOrderResponse(httpResponse);
        log.info("Handled Stripe response: {}",response);

        return response;
    }

    @Override
    public String confirmOrderRequest(String orderId, StripeConfirmOrderRequest orderRequest) {

        HttpRequest httpRequest = confirmOrderHelper.prepareHttpRequest(orderId,orderRequest);
        log.info("Prepared httpRequest to confirm order {}",httpRequest);

        ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);

        return httpResponse.getBody();
    }
}
