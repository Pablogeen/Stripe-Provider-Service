package com.rey.Stripe_Provider_Service.controllerTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rey.Stripe_Provider_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.controller.StripeController;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderResponse;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import com.rey.Stripe_Provider_Service.exception.StripeProviderException;
import com.rey.Stripe_Provider_Service.service.StripeServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StripeController.class)
class StripeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StripeServiceInterface stripeService;

    private StripeRequestDto validRequestDto;
    private StripeResponseDto responseDto;
    private StripeConfirmOrderRequest confirmOrderRequest;
    private StripeConfirmOrderResponse confirmOrderResponse;

    @BeforeEach
    void setUp() {
        // Setup valid request DTO
        validRequestDto = new StripeRequestDto();
        validRequestDto.setAmount(new BigDecimal("100.00"));
        validRequestDto.setCurrency("usd");

        // Setup response DTO
        responseDto = new StripeResponseDto();
        responseDto.setId("pi_3T2C4CAb9G6FKXYx");
        responseDto.setStatus("requires_payment_method");
        responseDto.setClientSecret("pi_3T2C4CAb9G6FKXYx_secret_abc123");


        // Setup confirm order request
        confirmOrderRequest = new StripeConfirmOrderRequest();
        confirmOrderRequest.setReturn_url("https://example.com/success");

        // Setup confirm order response
        confirmOrderResponse = new StripeConfirmOrderResponse();
        confirmOrderResponse.setId("pi_3T2C4CAb9G6FKXYx");
        confirmOrderResponse.setStatus("succeeded");
    }

    // ==================== POST /api/v1/payments/create-order/ Tests ====================

    @Test
    void testCreateStripeOrder_Success() throws Exception {
        // Arrange
        when(stripeService.createStripeOrderRequest(any(StripeRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("pi_3T2C4CAb9G6FKXYx")))
                .andExpect(jsonPath("$.status", is("requires_payment_method")))
                .andExpect(jsonPath("$.client_secret", is("pi_3T2C4CAb9G6FKXYx_secret_abc123")));

        verify(stripeService, times(1)).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_WithMinimumAmount() throws Exception {
        // Arrange
        validRequestDto.setAmount(new BigDecimal("0.50"));
        when(stripeService.createStripeOrderRequest(any(StripeRequestDto.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("pi_3T2C4CAb9G6FKXYx")));

        verify(stripeService, times(1)).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_WithDifferentCurrency() throws Exception {
        // Arrange
        validRequestDto.setCurrency("eur");
        when(stripeService.createStripeOrderRequest(any(StripeRequestDto.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("pi_3T2C4CAb9G6FKXYx")));

        verify(stripeService, times(1)).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_ValidationError_NullAmount() throws Exception {
        // Arrange
        validRequestDto.setAmount(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())))
                .andExpect(jsonPath("$.errorMessage").exists());

        verify(stripeService, never()).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_ValidationError_AmountTooSmall() throws Exception {
        // Arrange
        validRequestDto.setAmount(new BigDecimal("0.01"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_ValidationError_AmountTooLarge() throws Exception {
        // Arrange
        validRequestDto.setAmount(new BigDecimal("9999999.99"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_ValidationError_InvalidCurrency() throws Exception {
        // Arrange
        validRequestDto.setCurrency("xy");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_ValidationError_BlankCurrency() throws Exception {
        // Arrange
        validRequestDto.setCurrency("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    @Test
    void testCreateStripeOrder_ServiceThrowsException() throws Exception {
        // Arrange
        when(stripeService.createStripeOrderRequest(any(StripeRequestDto.class)))
                .thenThrow(new StripeProviderException(
                        ErrorCodeEnum.API_ERROR.getErrorCode(),
                        ErrorCodeEnum.API_ERROR.getErrorMessage(),
                        HttpStatus.UNAUTHORIZED
                ));

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.API_ERROR.getErrorCode())))
                .andExpect(jsonPath("$.errorMessage", is(ErrorCodeEnum.API_ERROR.getErrorMessage())));

        verify(stripeService, times(1)).createStripeOrderRequest(any(StripeRequestDto.class));
    }

    // ==================== POST /api/v1/payments/{orderId}/confirm-order/ Tests ====================

    @Test
    void testConfirmOrder_Success() throws Exception {
        // Arrange
        String orderId = "pi_3T2C4CAb9G6FKXYx";
        when(stripeService.confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class)))
                .thenReturn(confirmOrderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("pi_3T2C4CAb9G6FKXYx")))
                .andExpect(jsonPath("$.status", is("succeeded")));

        verify(stripeService, times(1)).confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class));
    }

    @Test
    void testConfirmOrder_WithDifferentOrderId() throws Exception {
        // Arrange
        String orderId = "pi_different_123";
        when(stripeService.confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class)))
                .thenReturn(confirmOrderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        verify(stripeService, times(1)).confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class));
    }

    @Test
    void testConfirmOrder_WithLongOrderId() throws Exception {
        // Arrange
        String longOrderId = "pi_3T2C4CAb9G6FKXYx1H12KKQY_very_long_order_id";
        when(stripeService.confirmOrderRequest(eq(longOrderId), any(StripeConfirmOrderRequest.class)))
                .thenReturn(confirmOrderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", longOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andExpect(status().isOk());

        verify(stripeService).confirmOrderRequest(eq(longOrderId), any(StripeConfirmOrderRequest.class));
    }

    @Test
    void testConfirmOrder_ValidationError_InvalidReturnUrl() throws Exception {
        // Arrange
        String orderId = "pi_123";
        confirmOrderRequest.setReturn_url("invalid-url");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).confirmOrderRequest(anyString(), any(StripeConfirmOrderRequest.class));
    }

    @Test
    void testConfirmOrder_ValidationError_BlankReturnUrl() throws Exception {
        // Arrange
        String orderId = "pi_123";
        confirmOrderRequest.setReturn_url("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).confirmOrderRequest(anyString(), any(StripeConfirmOrderRequest.class));
    }

    @Test
    void testConfirmOrder_EmptyRequestBody() throws Exception {
        // Arrange
        String orderId = "pi_123";

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.INVALID_REQUEST.getErrorCode())));

        verify(stripeService, never()).confirmOrderRequest(anyString(), any(StripeConfirmOrderRequest.class));
    }


    @Test
    void testConfirmOrder_ServiceThrowsCardDeclinedException() throws Exception {
        // Arrange
        String orderId = "pi_123";
        when(stripeService.confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class)))
                .thenThrow(new StripeProviderException(
                        ErrorCodeEnum.CARD_ERROR.getErrorCode(),
                        ErrorCodeEnum.CARD_ERROR.getErrorMessage(),
                        HttpStatus.PAYMENT_REQUIRED));

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.CARD_ERROR.getErrorCode())))
                .andExpect(jsonPath("$.errorMessage", is(ErrorCodeEnum.CARD_ERROR.getErrorMessage())));

        verify(stripeService, times(1)).confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class));
    }

    @Test
    void testConfirmOrder_ServiceThrowsNotFoundException() throws Exception {
        // Arrange
        String orderId = "pi_nonexistent";
        when(stripeService.confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class)))
                .thenThrow(new StripeProviderException(
                        ErrorCodeEnum.RESOURCE_MISSING.getErrorCode(),
                        ErrorCodeEnum.RESOURCE_MISSING.getErrorMessage(),
                        HttpStatus.NOT_FOUND
                ));

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(ErrorCodeEnum.RESOURCE_MISSING.getErrorCode())))
                .andExpect(jsonPath("$.errorMessage", is(ErrorCodeEnum.RESOURCE_MISSING.getErrorMessage())));

        verify(stripeService, times(1)).confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class));
    }

    // ==================== Integration Tests ====================

    @Test
    void testCreateThenConfirm_FullFlow() throws Exception {
        // Arrange
        String orderId = responseDto.getId();
        when(stripeService.createStripeOrderRequest(any(StripeRequestDto.class))).thenReturn(responseDto);
        when(stripeService.confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class)))
                .thenReturn(confirmOrderResponse);

        // Act & Assert - Create Order
        mockMvc.perform(post("/api/v1/payments/create-order/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId)));

        // Act & Assert - Confirm Order
        mockMvc.perform(post("/api/v1/payments/{orderId}/confirm-order/", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("succeeded")));

        verify(stripeService, times(1)).createStripeOrderRequest(any(StripeRequestDto.class));
        verify(stripeService, times(1)).confirmOrderRequest(eq(orderId), any(StripeConfirmOrderRequest.class));
    }


}
