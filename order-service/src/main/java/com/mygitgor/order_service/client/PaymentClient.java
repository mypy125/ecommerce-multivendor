package com.mygitgor.order_service.client;

import com.mygitgor.order_service.dto.OrderDto;
import com.mygitgor.order_service.dto.PaymentLinkResponse;
import com.mygitgor.order_service.dto.PaymentMethod;
import com.mygitgor.order_service.dto.clientDto.PaymentOrderDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClient {
    private final RestTemplate restTemplate;

    @Value("${payment.service.url:payment-service/api/payments}")
    private String paymentServiceUrl;

    @Value("${internal.auth.token}")
    private String internalToken;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaypalPaymentLinkFallback")
    @Retry(name = "paymentService", fallbackMethod = "createPaypalPaymentLinkFallback")
    @RateLimiter(name = "paymentService")
    public PaymentLinkResponse createPaypalPaymentLink(String userId, PaymentOrderDto paymentOrder){
        String url = UriComponentsBuilder.fromUriString(paymentServiceUrl)
                .path("/paypal/link")
                .toUriString();

        HttpHeaders customHeaders = new HttpHeaders();
        customHeaders.set("X-User-Id", userId);

        HttpEntity<PaymentOrderDto> request = new HttpEntity<>(paymentOrder, customHeaders);

        ResponseEntity<PaymentLinkResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, createHttpEntity(paymentOrder, customHeaders), PaymentLinkResponse.class
        );
        log.debug("Created PayPal payment link for user: {}", userId);
        return response.getBody();
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "createStripePaymentLinkFallback")
    @Retry(name = "paymentService", fallbackMethod = "createStripePaymentLinkFallback")
    @RateLimiter(name = "paymentService")
    public PaymentLinkResponse createStripePaymentLink(String userId, PaymentOrderDto paymentOrder){
         String url = UriComponentsBuilder.fromUriString(paymentServiceUrl)
                 .path("/stripe/link")
                 .toUriString();
         HttpHeaders customHeaders = new HttpHeaders();
         customHeaders.set("X-User-Id", userId);

         HttpEntity<PaymentOrderDto> request = new HttpEntity<>(paymentOrder, customHeaders);

         ResponseEntity<PaymentLinkResponse> response = restTemplate.exchange(
                 url, HttpMethod.POST, createHttpEntity(paymentOrder, customHeaders), PaymentLinkResponse.class
         );
         log.debug("Created Stripe payment link for user: {}", userId);
         return response.getBody();
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentOrderFallback")
    @Retry(name = "paymentService", fallbackMethod = "createPaymentOrderFallback")
    public PaymentOrderDto createPaymentOrder(String userId, Set<OrderDto> orders, PaymentMethod paymentMethod){
       String url = UriComponentsBuilder.fromUriString(paymentServiceUrl)
               .path("/orders")
               .toUriString();

       Map<String, Object> requestBody = new HashMap<>();
       requestBody.put("userId", userId);
       requestBody.put("orders", orders);
       requestBody.put("paymentMethod", paymentMethod);

       ResponseEntity<PaymentOrderDto> response = restTemplate.exchange(
               url, HttpMethod.POST, createHttpEntity(requestBody), PaymentOrderDto.class
       );
       log.debug("Created payment order for user: {}", userId);
       return response.getBody();
    }

    private <T> HttpEntity<T> createHttpEntity(T body, HttpHeaders customHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Service-Auth", internalToken);
        if (customHeaders != null) {
            headers.putAll(customHeaders);
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new HttpEntity<>(body, headers);
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        return createHttpEntity(body, null);
    }

    private PaymentLinkResponse createPaypalPaymentLinkFallback(String userId, PaymentOrderDto paymentOrder, Exception e) {
        log.warn("Using fallback for PayPal payment link creation for user: {}, error: {}", userId, e.getMessage());
        throw new RuntimeException("Payment service unavailable for PayPal payments");
    }

    private PaymentLinkResponse createStripePaymentLinkFallback(String userId, PaymentOrderDto paymentOrder, Exception e) {
        log.warn("Using fallback for Stripe payment link creation for user: {}, error: {}", userId, e.getMessage());
        throw new RuntimeException("Payment service unavailable for Stripe payments");
    }

    private PaymentOrderDto createPaymentOrderFallback(String userId, Set<OrderDto> orders, PaymentMethod paymentMethod, Exception e) {
        log.warn("Using fallback for payment order creation for user: {}, error: {}", userId, e.getMessage());
        throw new RuntimeException("Payment service unavailable for order creation");
    }

}
