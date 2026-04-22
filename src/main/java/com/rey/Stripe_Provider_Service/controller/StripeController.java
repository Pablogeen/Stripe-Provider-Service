package com.rey.Stripe_Provider_Service.controller;

import com.rey.Stripe_Provider_Service.service.StripeServiceInterface;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderResponse;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Stripe Payments", description = "Endpoints for interaction with Stripe payment endpoints")
public class StripeController {

    private final StripeServiceInterface stripeService;

    @Operation(
            summary = "Create Stripe Order",
            description = "Creates a new payment order using Stripe"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping("create-order/")
    public ResponseEntity<StripeResponseDto> createStripeOrder(@Valid @RequestBody
                                                                   StripeRequestDto requestDto) {
        log.info("Request made to create Order: {}", requestDto);
        StripeResponseDto createOrderResponse = stripeService.createStripeOrderRequest(requestDto);
        log.info("Create Order has been processed successfully: {}", createOrderResponse);
        return new ResponseEntity<>(createOrderResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Confirm Stripe Order",
            description = "Confirms payment for an existing Stripe order using the provided orderId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or orderId"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("{orderId}/confirm-order")
    public ResponseEntity<StripeConfirmOrderResponse> confirmOrder(
                            @Parameter(description = "Unique identifier of the order", required = true)
                            @PathVariable String orderId,
                            @Valid @RequestBody StripeConfirmOrderRequest orderRequest) {
        log.info("Request made to confirm Order for orderId: {}", orderId);
        StripeConfirmOrderResponse confirmOrderResponse =
                stripeService.confirmOrderRequest(orderId, orderRequest);
        log.info("Payment have been confirmed: {}", confirmOrderResponse);

        return new ResponseEntity<>(confirmOrderResponse, HttpStatus.OK);
    }
}