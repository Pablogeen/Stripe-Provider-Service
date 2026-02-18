package com.rey.Stripe_Provider_Service.http;

import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Data
public class HttpRequest {

    private HttpMethod httpMethod;
    private String url;
    private HttpHeaders httpHeaders;
    private Object body;
}
