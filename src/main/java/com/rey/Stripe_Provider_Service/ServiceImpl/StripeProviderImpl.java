package com.rey.Stripe_Provider_Service.ServiceImpl;

import com.rey.Stripe_Provider_Service.Service.StripeServiceInterface;
import com.rey.Stripe_Provider_Service.dto.StripeRequestDto;
import org.springframework.stereotype.Service;

@Service
public class StripeProviderImpl implements StripeServiceInterface {


    @Override
    public String createStripeOrderRequest(StripeRequestDto requestDto) {


        return "";
    }
}
