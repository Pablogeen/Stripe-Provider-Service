package com.rey.Stripe_Provider_Service.controller;

import com.rey.Stripe_Provider_Service.Service.StripeServiceInterface;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stripe")
@Slf4j
@RequiredArgsConstructor
public class StripeController {

    private final StripeServiceInterface stripeService;

    @PostMapping("create-order")
    public String createStripeOrder(@Valid @RequestBody StripeRequestDto requestDto) {
        log.info("Request made to create Order: {}", requestDto);
        String createOrderResponse = stripeService.createStripeOrderRequest(requestDto);
        log.info("Create Order has been processed successfully: {}",createOrderResponse);
                return createOrderResponse;

    }

}
