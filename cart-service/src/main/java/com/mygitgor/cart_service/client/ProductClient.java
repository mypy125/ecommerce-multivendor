package com.mygitgor.cart_service.client;

import com.mygitgor.cart_service.dto.ProductDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
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

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {
    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8086/api/products}")
    private String productServiceUrl;

    @Value("${internal.auth.token}")
    private String internalToken;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @Retry(name = "productService", fallbackMethod = "getProductFallback")
    @RateLimiter(name = "productService")
    @Bulkhead(name = "productService")
    public ProductDto getProductById(UUID productId) {
        String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                .path("/{productId}")
                .buildAndExpand(productId)
                .toUriString();

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(null), ProductDto.class
        );
        log.debug("Retrieved product: {}", productId);
        return response.getBody();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "existsByIdFallback")
    @Retry(name = "productService", fallbackMethod = "existsByIdFallback")
    public boolean existsById(UUID productId) {
        String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                .path("/{productId}/exists")
                .buildAndExpand(productId)
                .toUriString();

        ResponseEntity<Boolean> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(null), Boolean.class
        );
        return Boolean.TRUE.equals(response.getBody());
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Service-Auth", internalToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private ProductDto getProductFallback(UUID productId, Exception e) {
        log.warn("Using fallback for product: {}, error: {}", productId, e.getMessage());
        throw new RuntimeException("Product service unavailable for product: " + productId);
    }

    private boolean existsByIdFallback(UUID productId, Exception e) {
        log.warn("Using fallback for product existence check: {}", productId);
        return false;
    }
}
