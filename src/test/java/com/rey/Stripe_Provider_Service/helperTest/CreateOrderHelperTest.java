package com.rey.Stripe_Provider_Service.helperTest;

import com.rey.Stripe_Provider_Service.config.StripeProperties;
import com.rey.Stripe_Provider_Service.constants.Constant;
import com.rey.Stripe_Provider_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;
import com.rey.Stripe_Provider_Service.exception.StripeProviderException;
import com.rey.Stripe_Provider_Service.helper.CreateOrderHelper;
import com.rey.Stripe_Provider_Service.helper.StripeErrorHandler;
import com.rey.Stripe_Provider_Service.http.HttpRequest;
import com.rey.Stripe_Provider_Service.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderHelperTest {

    @Mock
    private StripeProperties stripeProperties;

    @Mock
    private JsonUtil jsonUtil;

    @Mock
    private StripeErrorHandler stripeErrorHandler;

    @InjectMocks
    private CreateOrderHelper createOrderHelper;

    private StripeRequestDto requestDto;
    private StripeResponseDto responseDto;
    private String apiKey;
    private String createOrderUrl;

    @BeforeEach
    void setUp() {
        // Setup test data
        apiKey = "sk_test_123456789";
        createOrderUrl = "https://api.stripe.com/v1/payment_intents";

        requestDto = new StripeRequestDto();
        requestDto.setAmount(new BigDecimal("100.00"));
        requestDto.setCurrency("usd");

        responseDto = new StripeResponseDto();
        responseDto.setId("pi_3T2C4CAb9G6FKXYx");
        responseDto.setStatus("requires_payment_method");
        responseDto.setClientSecret("pi_3T2C4CAb9G6FKXYx_secret_abc123");
    }

    // ==================== prepareHttpRequest Tests ====================

    @Test
    void testPrepareHttpRequest_Success() {
        // Arrange
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(HttpMethod.POST, result.getHttpMethod());
        assertEquals(createOrderUrl, result.getUrl());

        // Verify headers
        HttpHeaders headers = result.getHttpHeaders();
        assertNotNull(headers);
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
        assertTrue(headers.containsKey("Authorization"));

        // Verify body
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertNotNull(body);
        assertEquals("100.00", body.getFirst(Constant.AMOUNT));
        assertEquals("usd", body.getFirst(Constant.CURRENCY));
        assertEquals("true", body.getFirst(Constant.AUTOMATIC_PAYMENT_METHOD));

        // Verify method calls
        verify(stripeProperties, times(1)).getApiKey();
        verify(stripeProperties, times(1)).getCreateOrderUrl();
    }

    @Test
    void testPrepareHttpRequest_WithDifferentAmount() {
        // Arrange
        requestDto.setAmount(new BigDecimal("250.50"));
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertEquals("250.50", body.getFirst(Constant.AMOUNT));
    }

    @Test
    void testPrepareHttpRequest_WithDifferentCurrency() {
        // Arrange
        requestDto.setCurrency("eur");
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertEquals("eur", body.getFirst(Constant.CURRENCY));
    }

    @Test
    void testPrepareHttpRequest_VerifyBasicAuthHeader() {
        // Arrange
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        HttpHeaders headers = result.getHttpHeaders();
        String authHeader = headers.getFirst("Authorization");
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Basic"));
    }

    @Test
    void testPrepareHttpRequest_VerifyContentType() {
        // Arrange
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, result.getHttpHeaders().getContentType());
    }

    @Test
    void testPrepareHttpRequest_VerifyHttpMethod() {
        // Arrange
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        assertEquals(HttpMethod.POST, result.getHttpMethod());
    }

    @Test
    void testPrepareHttpRequest_VerifyAutomaticPaymentMethodsEnabled() {
        // Arrange
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertEquals("true", body.getFirst(Constant.AUTOMATIC_PAYMENT_METHOD));
    }

    @Test
    void testPrepareHttpRequest_WithLargeAmount() {
        // Arrange
        requestDto.setAmount(new BigDecimal("999999.99"));
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertEquals("999999.99", body.getFirst(Constant.AMOUNT));
    }

    @Test
    void testPrepareHttpRequest_WithSmallAmount() {
        // Arrange
        requestDto.setAmount(new BigDecimal("0.50"));
        when(stripeProperties.getApiKey()).thenReturn(apiKey);
        when(stripeProperties.getCreateOrderUrl()).thenReturn(createOrderUrl);

        // Act
        HttpRequest result = createOrderHelper.prepareHttpRequest(requestDto);

        // Assert
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertEquals("0.50", body.getFirst(Constant.AMOUNT));
    }

    // ==================== toCreateOrderResponse Tests ====================

    @Test
    void testToCreateOrderResponse_Success() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\",\"status\":\"succeeded\",\"client_secret\":\"pi_123_secret\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeResponseDto.class))
                .thenReturn(responseDto);

        // Act
        StripeResponseDto result = createOrderHelper.toCreateOrderResponse(httpResponse);

        // Assert
        assertNotNull(result);
        assertEquals("pi_3T2C4CAb9G6FKXYx", result.getId());
        assertEquals("requires_payment_method", result.getStatus());
        assertEquals("pi_3T2C4CAb9G6FKXYx_secret_abc123", result.getClientSecret());

        verify(jsonUtil, times(1)).convertJsonStringToJavaObject(responseBody, StripeResponseDto.class);
        verifyNoInteractions(stripeErrorHandler);
    }

    @Test
    void testToCreateOrderResponse_SuccessWithCreatedStatus() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\",\"status\":\"succeeded\",\"client_secret\":\"pi_123_secret\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.CREATED);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeResponseDto.class))
                .thenReturn(responseDto);

        // Act
        StripeResponseDto result = createOrderHelper.toCreateOrderResponse(httpResponse);

        // Assert
        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
    }

    @Test
    void testToCreateOrderResponse_NullResponse() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeResponseDto.class))
                .thenReturn(null);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getHttpStatus());
    }

    @Test
    void testToCreateOrderResponse_NullId() {
        // Arrange
        String responseBody = "{\"status\":\"succeeded\",\"client_secret\":\"pi_123_secret\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        StripeResponseDto responseWithNullId = new StripeResponseDto();
        responseWithNullId.setId(null);
        responseWithNullId.setStatus("succeeded");
        responseWithNullId.setClientSecret("pi_123_secret");

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeResponseDto.class))
                .thenReturn(responseWithNullId);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testToCreateOrderResponse_NullStatus() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\",\"client_secret\":\"pi_123_secret\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        StripeResponseDto responseWithNullStatus = new StripeResponseDto();
        responseWithNullStatus.setId("pi_123");
        responseWithNullStatus.setStatus(null);
        responseWithNullStatus.setClientSecret("pi_123_secret");

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeResponseDto.class))
                .thenReturn(responseWithNullStatus);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testToCreateOrderResponse_NullClientSecret() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\",\"status\":\"succeeded\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        StripeResponseDto responseWithNullSecret = new StripeResponseDto();
        responseWithNullSecret.setId("pi_123");
        responseWithNullSecret.setStatus("succeeded");
        responseWithNullSecret.setClientSecret(null);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeResponseDto.class))
                .thenReturn(responseWithNullSecret);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testToCreateOrderResponse_400BadRequest() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Invalid request\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);

        doThrow(new StripeProviderException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(), "Invalid request", HttpStatus.BAD_REQUEST))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
        verifyNoInteractions(jsonUtil);
    }

    @Test
    void testToCreateOrderResponse_401Unauthorized() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Invalid API key\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);

        doThrow(new StripeProviderException(ErrorCodeEnum.API_ERROR.getErrorCode(),
                ErrorCodeEnum.API_ERROR.getErrorMessage(), HttpStatus.UNAUTHORIZED))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> createOrderHelper.toCreateOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.API_ERROR.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateOrderResponse_402PaymentRequired() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Amount too small\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.PAYMENT_REQUIRED);

        doThrow(new StripeProviderException(ErrorCodeEnum.CARD_ERROR.getErrorCode(),
                ErrorCodeEnum.CARD_ERROR.getErrorCode(), HttpStatus.PAYMENT_REQUIRED))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(StripeProviderException.class,
                () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.CARD_ERROR.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateOrderResponse_404NotFound() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Resource not found\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);

        doThrow(new StripeProviderException(ErrorCodeEnum.RESOURCE_MISSING.getErrorCode(),
                ErrorCodeEnum.RESOURCE_MISSING.getErrorMessage(),  HttpStatus.NOT_FOUND))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.RESOURCE_MISSING.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateOrderResponse_429RateLimit() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Too many requests\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.TOO_MANY_REQUESTS);

        doThrow(new StripeProviderException(ErrorCodeEnum.RATE_LIMIT_ERROR.getErrorCode(),
                ErrorCodeEnum.RATE_LIMIT_ERROR.getErrorMessage(), HttpStatus.TOO_MANY_REQUESTS))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.RATE_LIMIT_ERROR.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateOrderResponse_500InternalServerError() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Internal server error\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);

        doThrow(new StripeProviderException(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(),
                ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateOrderResponse_503ServiceUnavailable() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Service unavailable\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.SERVICE_UNAVAILABLE);

        doThrow(new StripeProviderException(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(),
                ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(), HttpStatus.SERVICE_UNAVAILABLE))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> createOrderHelper.toCreateOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateOrderResponse_UnexpectedStatusCode() {
        // Arrange - 3xx redirect
        String responseBody = "Redirect";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.MOVED_PERMANENTLY);

        // Act & Assert
        StripeProviderException exception = assertThrows(StripeProviderException.class,
                () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getHttpStatus());
        verifyNoInteractions(stripeErrorHandler);
        verifyNoInteractions(jsonUtil);
    }

    @Test
    void testToCreateOrderResponse_VerifyErrorHandlerCalled() {
        // Arrange
        ResponseEntity<String> httpResponse = new ResponseEntity<>("{\"error\":{}}", HttpStatus.BAD_REQUEST);

        doThrow(new StripeProviderException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(), ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
                HttpStatus.BAD_REQUEST))
                .when(stripeErrorHandler).handleStripeError(any());

        // Act & Assert
        assertThrows(StripeProviderException.class, () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        verify(stripeErrorHandler).handleStripeError(argThat(response ->
                response.getStatusCode() == HttpStatus.BAD_REQUEST
        ));
    }

    @Test
    void testToCreateOrderResponse_VerifyJsonUtilNotCalledOn4xxError() {
        // Arrange
        ResponseEntity<String> httpResponse = new ResponseEntity<>("{\"error\":{}}", HttpStatus.BAD_REQUEST);

        doThrow(new StripeProviderException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
                ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(), HttpStatus.BAD_REQUEST))
                .when(stripeErrorHandler).handleStripeError(any());

        // Act & Assert
        assertThrows(StripeProviderException.class,
                () -> createOrderHelper.toCreateOrderResponse(httpResponse));

        verifyNoInteractions(jsonUtil);
    }
}