package com.mygitgor.order_service.client;

import com.mygitgor.order_service.dto.clientDto.CartDto;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class CartClient {
    private final RestTemplate restTemplate;

    @Value("${cart.service.url:http://localhost:8086/api/carts}")
    private String cartServiceUrl;

    @Value("${internal.auth.token}")
    private String internalToken;

    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartByUserIdFallback")
    @Retry(name = "cartService", fallbackMethod = "getCartByUserIdFallback")
    @RateLimiter(name = "cartService")
    public CartDto getCartByUserId(String userId){
        String url = UriComponentsBuilder.fromUriString(cartServiceUrl)
                .path("/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();
        ResponseEntity<CartDto> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(null), CartDto.class
        );
        log.debug("Retrieved cart for user: {}", userId);
        return response.getBody();
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "clearCartFallback")
    @Retry(name = "cartService", fallbackMethod = "clearCartFallback")
    public void clearCart(String cartId){
            String url = UriComponentsBuilder.fromUriString(cartServiceUrl)
                    .path("/{cartId}/clear")
                    .buildAndExpand(cartId)
                    .toUriString();
            restTemplate.exchange(
                    url, HttpMethod.POST, createHttpEntity(null), Void.class
            );
            log.debug("Cleared cart: {}", cartId);
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Service-Auth", internalToken);
        return new HttpEntity<>(body, headers);
    }

    private CartDto getCartByUserIdFallback(String userId, Exception e) {
        log.warn("Using fallback for cart retrieval for user: {}, error: {}", userId, e.getMessage());
        return new CartDto();
    }

    private void clearCartFallback(String cartId, Exception e) {
        log.warn("Using fallback for clearing cart: {}, error: {}", cartId, e.getMessage());
    }
}
