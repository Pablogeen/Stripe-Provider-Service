package com.rey.Stripe_Provider_Service.Service;

import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import com.rey.Stripe_Provider_Service.dto.StripeResponseDto;

public interface StripeServiceInterface {
    StripeResponseDto createStripeOrderRequest(StripeRequestDto requestDto);
}
