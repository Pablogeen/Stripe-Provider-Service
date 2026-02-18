package com.rey.Stripe_Provider_Service.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class HttpServiceEngine{

    private final RestClient restClient;

    public ResponseEntity<String> makeHttpCall(HttpRequest request){

       ResponseEntity<String> httpResponse = restClient
                .method(request.getHttpMethod())
                .uri(request.getUrl())
                .headers(restClientHeader -> restClientHeader.addAll(request.getHttpHeaders()))
                .body(request.getBody())
                .retrieve()
                .toEntity(String.class);

          return httpResponse;
    }

}