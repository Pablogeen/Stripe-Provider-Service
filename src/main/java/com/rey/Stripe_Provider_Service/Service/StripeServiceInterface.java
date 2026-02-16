package com.rey.Stripe_Provider_Service.Service;

import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;

public interface StripeServiceInterface {
    String createStripeOrderRequest(StripeRequestDto requestDto);
}
