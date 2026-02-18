package com.rey.Stripe_Provider_Service.controller;

import com.rey.Stripe_Provider_Service.Service.StripeServiceInterface;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderResponse;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("{orderId}/confirm-order/")
    public StripeConfirmOrderResponse confirmOrder(@PathVariable String orderId,
                                                @RequestBody @Valid StripeConfirmOrderRequest orderRequest){
        log.info("Request made to confirm Order for orderId: {}", orderId);
        StripeConfirmOrderResponse confirmOrderResponse = stripeService.confirmOrderRequest(orderId, orderRequest);
        log.info("Payment have been confirmed: {}",confirmOrderResponse);
        return confirmOrderResponse;
    }

}
