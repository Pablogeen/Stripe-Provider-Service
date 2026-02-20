package com.rey.Stripe_Provider_Service.helperTest;

import com.rey.Stripe_Provider_Service.config.StripeProperties;
import com.rey.Stripe_Provider_Service.constants.Constant;
import com.rey.Stripe_Provider_Service.constants.ErrorCodeEnum;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderRequest;
import com.rey.Stripe_Provider_Service.dto.StripeConfirmOrderResponse;
import com.rey.Stripe_Provider_Service.exception.StripeProviderException;
import com.rey.Stripe_Provider_Service.helper.ConfirmOrderHelper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderHelperTest {

    @Mock
    private StripeProperties stripeProperties;

    @Mock
    private JsonUtil jsonUtil;

    @Mock
    private StripeErrorHandler stripeErrorHandler;

    @InjectMocks
    private ConfirmOrderHelper confirmOrderHelper;

    private StripeConfirmOrderRequest confirmOrderRequest;
    private StripeConfirmOrderResponse confirmOrderResponse;
    private String orderId;
    private String apiKey;
    private String confirmOrderUrl;

    @BeforeEach
    void setUp() {
        // Setup test data
        orderId = "pi_3T2C4CAb9G6FKXYx";
        apiKey = "sk_test_123456789";
        confirmOrderUrl = "https://api.stripe.com/v1/payment_intents/%s/confirm";

        confirmOrderRequest = new StripeConfirmOrderRequest();
        confirmOrderRequest.setReturn_url("https://example.com/success");

        confirmOrderResponse = new StripeConfirmOrderResponse();
        confirmOrderResponse.setId("pi_3T2C4CAb9G6FKXYx");
        confirmOrderResponse.setStatus("succeeded");
    }

    // ==================== prepareHttpRequest Tests ====================

    @Test
    void testPrepareHttpRequest_Success() {
        // Arrange
        when(stripeProperties.getConfirmOrderUrl()).thenReturn(confirmOrderUrl);
        when(stripeProperties.getApiKey()).thenReturn(apiKey);

        // Act
        HttpRequest result = confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpMethod.POST, result.getHttpMethod());
        assertEquals("https://api.stripe.com/v1/payment_intents/pi_3T2C4CAb9G6FKXYx/confirm", result.getUrl());

        // Verify headers
        HttpHeaders headers = result.getHttpHeaders();
        assertNotNull(headers);
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
        assertTrue(headers.containsKey("Authorization"));

        // Verify body
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertNotNull(body);
        assertEquals(Constant.PM_VISA_CARD, body.getFirst(Constant.PAYMENT_METHOD));
        assertEquals("https://example.com/success", body.getFirst(Constant.RETURN_URL));

        // Verify method calls
        verify(stripeProperties, times(1)).getConfirmOrderUrl();
        verify(stripeProperties, times(1)).getApiKey();
    }

    @Test
    void testPrepareHttpRequest_WithDifferentOrderId() {
        // Arrange
        String differentOrderId = "pi_different_123";
        when(stripeProperties.getConfirmOrderUrl()).thenReturn(confirmOrderUrl);
        when(stripeProperties.getApiKey()).thenReturn(apiKey);

        // Act
        HttpRequest result = confirmOrderHelper.prepareHttpRequest(differentOrderId, confirmOrderRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getUrl().contains(differentOrderId));
        assertEquals("https://api.stripe.com/v1/payment_intents/pi_different_123/confirm", result.getUrl());
    }

    @Test
    void testPrepareHttpRequest_WithDifferentReturnUrl() {
        // Arrange
        String differentReturnUrl = "https://different.com/callback";
        confirmOrderRequest.setReturn_url(differentReturnUrl);
        when(stripeProperties.getConfirmOrderUrl()).thenReturn(confirmOrderUrl);
        when(stripeProperties.getApiKey()).thenReturn(apiKey);

        // Act
        HttpRequest result = confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest);

        // Assert
        assertNotNull(result);
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
        assertEquals(differentReturnUrl, body.getFirst(Constant.RETURN_URL));
    }

    @Test
    void testPrepareHttpRequest_VerifyBasicAuthHeader() {
        // Arrange
        when(stripeProperties.getConfirmOrderUrl()).thenReturn(confirmOrderUrl);
        when(stripeProperties.getApiKey()).thenReturn(apiKey);

        // Act
        HttpRequest result = confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest);

        // Assert
        HttpHeaders headers = result.getHttpHeaders();
        assertTrue(headers.containsKey("Authorization"));
        String authHeader = headers.getFirst("Authorization");
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Basic"));
    }

    @Test
    void testPrepareHttpRequest_VerifyContentType() {
        // Arrange
        when(stripeProperties.getConfirmOrderUrl()).thenReturn(confirmOrderUrl);
        when(stripeProperties.getApiKey()).thenReturn(apiKey);

        // Act
        HttpRequest result = confirmOrderHelper.prepareHttpRequest(orderId, confirmOrderRequest);

        // Assert
        HttpHeaders headers = result.getHttpHeaders();
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, headers.getContentType());
    }

    // ==================== toCreateConfirmOrderResponse Tests ====================

    @Test
    void testToCreateConfirmOrderResponse_Success() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\",\"status\":\"succeeded\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeConfirmOrderResponse.class))
                .thenReturn(confirmOrderResponse);

        // Act
        StripeConfirmOrderResponse result = confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse);

        // Assert
        assertNotNull(result);
        assertEquals("pi_3T2C4CAb9G6FKXYx", result.getId());
        assertEquals("succeeded", result.getStatus());

        verify(jsonUtil, times(1)).convertJsonStringToJavaObject(responseBody, StripeConfirmOrderResponse.class);
        verifyNoInteractions(stripeErrorHandler);
    }

    @Test
    void testToCreateConfirmOrderResponse_SuccessWithCreatedStatus() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\",\"status\":\"succeeded\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.CREATED);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeConfirmOrderResponse.class))
                .thenReturn(confirmOrderResponse);

        // Act
        StripeConfirmOrderResponse result = confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse);

        // Assert
        assertNotNull(result);
        assertEquals("pi_3T2C4CAb9G6FKXYx", result.getId());
        assertEquals("succeeded", result.getStatus());
    }

    @Test
    void testToCreateConfirmOrderResponse_NullResponse() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\",\"status\":\"succeeded\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeConfirmOrderResponse.class))
                .thenReturn(null);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getHttpStatus());
    }

    @Test
    void testToCreateConfirmOrderResponse_NullId() {
        // Arrange
        String responseBody = "{\"status\":\"succeeded\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        StripeConfirmOrderResponse responseWithNullId = new StripeConfirmOrderResponse();
        responseWithNullId.setId(null);
        responseWithNullId.setStatus("succeeded");

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeConfirmOrderResponse.class))
                .thenReturn(responseWithNullId);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testToCreateConfirmOrderResponse_NullStatus() {
        // Arrange
        String responseBody = "{\"id\":\"pi_123\"}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);

        StripeConfirmOrderResponse responseWithNullStatus = new StripeConfirmOrderResponse();
        responseWithNullStatus.setId("pi_123");
        responseWithNullStatus.setStatus(null);

        when(jsonUtil.convertJsonStringToJavaObject(responseBody, StripeConfirmOrderResponse.class))
                .thenReturn(responseWithNullStatus);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
    }

    //@Test
    //void testToCreateConfirmOrderResponse_4xxError() {
//        // Arrange
//        String responseBody = "{\"error\":{\"message\":\"Invalid request\"}}";
//        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
//
//        doNothing().when(stripeErrorHandler).handleStripeError(httpResponse);
//        doThrow(new StripeProviderException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
//                        ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(), HttpStatus.BAD_REQUEST))
//                .when(stripeErrorHandler).handleStripeError(httpResponse);
//
//        // Act & Assert
//        StripeProviderException exception = assertThrows(
//                StripeProviderException.class,
//                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
//        );
//
//        assertEquals(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(), exception.getErrorCode());
//        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
//        verifyNoInteractions(jsonUtil);
   // }

    @Test
    void testToCreateConfirmOrderResponse_401Error() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Invalid API key\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);

        doThrow(new StripeProviderException(ErrorCodeEnum.API_ERROR.getErrorCode(),
                    ErrorCodeEnum.API_ERROR.getErrorMessage(), HttpStatus.UNAUTHORIZED))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.API_ERROR.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateConfirmOrderResponse_402Error() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Card declined\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.PAYMENT_REQUIRED);

        doThrow(new StripeProviderException(ErrorCodeEnum.CARD_ERROR.getErrorCode(),
                ErrorCodeEnum.CARD_ERROR.getErrorMessage(), HttpStatus.PAYMENT_REQUIRED))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.CARD_ERROR.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateConfirmOrderResponse_5xxError() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Internal server error\"}}";
        ResponseEntity<String> httpResponse =
                new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);

        doThrow(new StripeProviderException(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(),
                ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse));

        assertEquals(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateConfirmOrderResponse_503ServiceUnavailable() {
        // Arrange
        String responseBody = "{\"error\":{\"message\":\"Service unavailable\"}}";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.SERVICE_UNAVAILABLE);

        doThrow(new StripeProviderException(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(),
                ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorMessage(), HttpStatus.SERVICE_UNAVAILABLE))
                .when(stripeErrorHandler).handleStripeError(httpResponse);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.STRIPE_SERVICE_UNAVAILABLE.getErrorCode(), exception.getErrorCode());
        verify(stripeErrorHandler, times(1)).handleStripeError(httpResponse);
    }

    @Test
    void testToCreateConfirmOrderResponse_UnexpectedStatusCode() {
        // Arrange - 3xx redirect (not 2xx, 4xx, or 5xx)
        String responseBody = "Redirect";
        ResponseEntity<String> httpResponse = new ResponseEntity<>(responseBody, HttpStatus.MOVED_PERMANENTLY);

        // Act & Assert
        StripeProviderException exception = assertThrows(
                StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse)
        );

        assertEquals(ErrorCodeEnum.STRIPE_UNKNOWN_ERROR.getErrorCode(), exception.getErrorCode());
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getHttpStatus());
        verifyNoInteractions(stripeErrorHandler);
        verifyNoInteractions(jsonUtil);
    }

    @Test
    void testToCreateConfirmOrderResponse_VerifyErrorHandlerCalled() {
        // Arrange
        ResponseEntity<String> httpResponse = new ResponseEntity<>("{\"error\":{}}", HttpStatus.BAD_REQUEST);

        doThrow(new StripeProviderException(ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
                ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(), HttpStatus.BAD_REQUEST))
                .when(stripeErrorHandler).handleStripeError(any());

        // Act & Assert
        assertThrows(StripeProviderException.class,
                () -> confirmOrderHelper.toCreateConfirmOrderResponse(httpResponse));

        verify(stripeErrorHandler).handleStripeError(argThat(response ->
                response.getStatusCode() == HttpStatus.BAD_REQUEST
        ));
    }
}