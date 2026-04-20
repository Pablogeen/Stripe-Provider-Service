package com.rey.Stripe_Provider_Service.serviceTest;

import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderResponse;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import com.rey.Stripe_Provider_Service.helper.ConfirmOrderHelper;
import com.rey.Stripe_Provider_Service.helper.CreateOrderHelper;
import com.rey.Stripe_Provider_Service.http.HttpRequest;
import com.rey.Stripe_Provider_Service.http.HttpServiceEngine;
import com.rey.Stripe_Provider_Service.serviceImpl.StripeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeServiceImplTest {

    @Mock
    private CreateOrderHelper createOrderHelper;

    @Mock
    private ConfirmOrderHelper confirmOrderHelper;

    @Mock
    private HttpServiceEngine httpServiceEngine;

    @InjectMocks
    private StripeServiceImpl stripeService;

    private StripeRequestDto stripeRequestDto;
    private StripeConfirmOrderRequest confirmOrderRequest;
    private HttpRequest httpRequest;
    private ResponseEntity<String> httpResponse;
    private StripeResponseDto stripeResponseDto;
    private StripeConfirmOrderResponse confirmOrderResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        stripeRequestDto = new StripeRequestDto();
        stripeRequestDto.setAmount(new BigDecimal("100.00"));
        stripeRequestDto.setCurrency("usd");

        confirmOrderRequest = new StripeConfirmOrderRequest();
        confirmOrderRequest.setReturn_url("https://example.com/success");

        httpRequest = new HttpRequest();
        httpRequest.setUrl("https://api.stripe.com/v1/payment_intents");
        httpRequest.setHttpMethod(HttpMethod.POST);

        httpResponse = new ResponseEntity<>(
                "{\"id\":\"pi_123\",\"status\":\"succeeded\",\"client_secret\":\"pi_123_secret\"}",
                HttpStatus.OK
        );

        stripeResponseDto = new StripeResponseDto();
        stripeResponseDto.setId("pi_123");
        stripeResponseDto.setStatus("succeeded");
        stripeResponseDto.setClientSecret("pi_123_secret_abc");

        confirmOrderResponse = new StripeConfirmOrderResponse();
        confirmOrderResponse.setId("pi_123");
        confirmOrderResponse.setStatus("succeeded");
    }

    // ==================== createStripeOrderRequest Tests ====================

    @Test
    void testCreateStripeOrderRequest_Success() {
        // Arrange
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        StripeResponseDto result = stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("pi_123", result.getId());
        assertEquals("succeeded", result.getStatus());
        assertEquals("pi_123_secret_abc", result.getClientSecret());

        // Verify interactions
        verify(createOrderHelper, times(1)).prepareHttpRequest(stripeRequestDto);
        verify(httpServiceEngine, times(1)).makeHttpCall(httpRequest);
        verify(createOrderHelper, times(1)).toCreateOrderResponse(httpResponse);
        verifyNoMoreInteractions(createOrderHelper, httpServiceEngine);
        verifyNoInteractions(confirmOrderHelper);
    }

    @Test
    void testCreateStripeOrderRequest_WithDifferentAmount() {
        // Arrange
        stripeRequestDto.setAmount(new BigDecimal("250.50"));
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        StripeResponseDto result = stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        assertNotNull(result);
        verify(createOrderHelper).prepareHttpRequest(argThat(dto ->
                dto.getAmount().compareTo(new BigDecimal("250.50")) == 0));
    }

    @Test
    void testCreateStripeOrderRequest_WithDifferentCurrency() {
        // Arrange
        stripeRequestDto.setCurrency("eur");
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        StripeResponseDto result = stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        assertNotNull(result);
        verify(createOrderHelper).prepareHttpRequest(argThat(dto ->
                dto.getCurrency().equals("eur")
        ));
    }

    @Test
    void testCreateStripeOrderRequest_VerifyHelperPrepareCalled() {
        // Arrange
        when(createOrderHelper.prepareHttpRequest(any(StripeRequestDto.class))).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(any(HttpRequest.class))).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(any(ResponseEntity.class))).thenReturn(stripeResponseDto);

        // Act
        stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        verify(createOrderHelper).prepareHttpRequest(argThat(dto ->
                dto.getCurrency().equals("usd") &&
                        dto.getAmount().compareTo(new BigDecimal("100.00")) == 0 ));
    }

    @Test
    void testCreateStripeOrderRequest_VerifyHttpCallMade() {
        // Arrange
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        verify(httpServiceEngine).makeHttpCall(argThat(req ->
                req.getUrl().equals("https://api.stripe.com/v1/payment_intents") &&
                        req.getHttpMethod() == org.springframework.http.HttpMethod.POST
        ));
    }

    @Test
    void testCreateStripeOrderRequest_VerifyResponseMappingCalled() {
        // Arrange
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        verify(createOrderHelper).toCreateOrderResponse(argThat(response ->
                response.getStatusCode() == HttpStatus.OK &&
                        response.getBody().contains("pi_123")));
    }

    @Test
    void testCreateStripeOrderRequest_AllStepsExecutedInOrder() {
        // Arrange
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert - Verify order of execution
        var inOrder = inOrder(createOrderHelper, httpServiceEngine);
        inOrder.verify(createOrderHelper).prepareHttpRequest(any(StripeRequestDto.class));
        inOrder.verify(httpServiceEngine).makeHttpCall(any(HttpRequest.class));
        inOrder.verify(createOrderHelper).toCreateOrderResponse(any(ResponseEntity.class));
    }

    @Test
    void testCreateStripeOrderRequest_WithMinimumAmount() {
        // Arrange
        stripeRequestDto.setAmount(new BigDecimal("0.50"));
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        StripeResponseDto result = stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        assertNotNull(result);
        verify(createOrderHelper).prepareHttpRequest(stripeRequestDto);
    }

    @Test
    void testCreateStripeOrderRequest_WithLargeAmount() {
        // Arrange
        stripeRequestDto.setAmount(new BigDecimal("999999.99"));
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        StripeResponseDto result = stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        assertNotNull(result);
        verify(createOrderHelper).prepareHttpRequest(stripeRequestDto);
    }

    // ==================== confirmOrderRequest Tests ====================

    @Test
    void testConfirmOrderRequest_Success() {
        // Arrange
        String orderId = "pi_123";
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        StripeConfirmOrderResponse result = stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals("pi_123", result.getId());
        assertEquals("succeeded", result.getStatus());

        // Verify interactions
        verify(confirmOrderHelper, times(1)).prepareHttpRequest(orderId, confirmOrderRequest);
        verify(httpServiceEngine, times(1)).makeHttpCall(httpRequest);
        verify(confirmOrderHelper, times(1)).toCreateConfirmOrderResponse(httpResponse);
        verifyNoMoreInteractions(confirmOrderHelper, httpServiceEngine);
        verifyNoInteractions(createOrderHelper);
    }

    @Test
    void testConfirmOrderRequest_WithDifferentOrderId() {
        // Arrange
        String orderId = "pi_different_456";
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        StripeConfirmOrderResponse result = stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        assertNotNull(result);
        verify(confirmOrderHelper).prepareHttpRequest(eq(orderId), any(StripeConfirmOrderRequest.class));
    }

    @Test
    void testConfirmOrderRequest_WithDifferentReturnUrl() {
        // Arrange
        String orderId = "pi_123";
        confirmOrderRequest.setReturn_url("https://different.com/callback");
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        StripeConfirmOrderResponse result = stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        assertNotNull(result);
        verify(confirmOrderHelper).prepareHttpRequest(eq(orderId), argThat(req ->
                req.getReturn_url().equals("https://different.com/callback")
        ));
    }

    @Test
    void testConfirmOrderRequest_VerifyHelperPrepareCalled() {
        // Arrange
        String orderId = "pi_123";
        when(confirmOrderHelper.prepareHttpRequest(anyString(), any(StripeConfirmOrderRequest.class)))
                .thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(any(HttpRequest.class))).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(any(ResponseEntity.class)))
                .thenReturn(confirmOrderResponse);

        // Act
        stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        verify(confirmOrderHelper).prepareHttpRequest(
                eq("pi_123"),
                argThat(req -> req.getReturn_url().equals("https://example.com/success"))
        );
    }

    @Test
    void testConfirmOrderRequest_VerifyHttpCallMade() {
        // Arrange
        String orderId = "pi_123";
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        verify(httpServiceEngine).makeHttpCall(any(HttpRequest.class));
    }

    @Test
    void testConfirmOrderRequest_VerifyResponseMappingCalled() {
        // Arrange
        String orderId = "pi_123";
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        verify(confirmOrderHelper).toCreateConfirmOrderResponse(argThat(response ->
                response.getStatusCode() == HttpStatus.OK
        ));
    }

    @Test
    void testConfirmOrderRequest_AllStepsExecutedInOrder() {
        // Arrange
        String orderId = "pi_123";
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert - Verify order of execution
        var inOrder = inOrder(confirmOrderHelper, httpServiceEngine);
        inOrder.verify(confirmOrderHelper).prepareHttpRequest(anyString(), any(StripeConfirmOrderRequest.class));
        inOrder.verify(httpServiceEngine).makeHttpCall(any(HttpRequest.class));
        inOrder.verify(confirmOrderHelper).toCreateConfirmOrderResponse(any(ResponseEntity.class));
    }

    @Test
    void testConfirmOrderRequest_WithLongOrderId() {
        // Arrange
        String longOrderId = "pi_3T2C4CAb9G6FKXYx1H12KKQY_very_long_id";
        when(confirmOrderHelper.prepareHttpRequest(longOrderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        StripeConfirmOrderResponse result = stripeService.confirmOrderRequest(longOrderId, confirmOrderRequest);

        // Assert
        assertNotNull(result);
        verify(confirmOrderHelper).prepareHttpRequest(eq(longOrderId), any());
    }

    @Test
    void testConfirmOrderRequest_WithSpecialCharactersInOrderId() {
        // Arrange
        String orderIdWithSpecialChars = "pi_123-ABC_test";
        when(confirmOrderHelper.prepareHttpRequest(orderIdWithSpecialChars, confirmOrderRequest))
                .thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        StripeConfirmOrderResponse result = stripeService.confirmOrderRequest(
                orderIdWithSpecialChars,
                confirmOrderRequest
        );

        // Assert
        assertNotNull(result);
        verify(confirmOrderHelper).prepareHttpRequest(eq(orderIdWithSpecialChars), any());
    }

    // ==================== Integration Tests ====================

    @Test
    void testBothMethods_CreateThenConfirm() {
        // Arrange
        String orderId = "pi_123";

        // Setup for create order
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Setup for confirm order
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        StripeResponseDto createResult = stripeService.createStripeOrderRequest(stripeRequestDto);
        StripeConfirmOrderResponse confirmResult = stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        assertNotNull(createResult);
        assertNotNull(confirmResult);

        // Verify both helpers were called
        verify(createOrderHelper, times(1)).prepareHttpRequest(any(StripeRequestDto.class));
        verify(confirmOrderHelper, times(1)).prepareHttpRequest(anyString(), any(StripeConfirmOrderRequest.class));
        verify(httpServiceEngine, times(2)).makeHttpCall(any(HttpRequest.class));
    }

    @Test
    void testCreateStripeOrderRequest_VerifyNoInteractionWithConfirmHelper() {
        // Arrange
        when(createOrderHelper.prepareHttpRequest(stripeRequestDto)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(createOrderHelper.toCreateOrderResponse(httpResponse)).thenReturn(stripeResponseDto);

        // Act
        stripeService.createStripeOrderRequest(stripeRequestDto);

        // Assert
        verifyNoInteractions(confirmOrderHelper);
    }

    @Test
    void testConfirmOrderRequest_VerifyNoInteractionWithCreateHelper() {
        // Arrange
        String orderId = "pi_123";
        when(confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest)).thenReturn(httpRequest);
        when(httpServiceEngine.makeHttpCall(httpRequest)).thenReturn(httpResponse);
        when(confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)).thenReturn(confirmOrderResponse);

        // Act
        stripeService.confirmOrderRequest(orderId, confirmOrderRequest);

        // Assert
        verifyNoInteractions(createOrderHelper);
    }
}