package com.rey.Stripe_Provider_Service.controller;

import com.rey.Stripe_Provider_Service.Service.StripeServiceInterface;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/")
@Slf4j
@RequiredArgsConstructor
public class StripeController {

    private final StripeServiceInterface stripeService;

    @PostMapping("create-order/")
    public ResponseEntity<StripeResponseDto> createStripeOrder(@Valid @RequestBody StripeRequestDto requestDto) {
        log.info("Request made to create Order: {}", requestDto);
        StripeResponseDto createOrderResponse = stripeService.createStripeOrderRequest(requestDto);
        log.info("Create Order has been processed successfully: {}",createOrderResponse);
        return new ResponseEntity<>(createOrderResponse, HttpStatus.OK);

    }

}
