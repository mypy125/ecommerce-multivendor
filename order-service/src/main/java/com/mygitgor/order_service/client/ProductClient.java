package com.mygitgor.order_service.client;

import com.mygitgor.order_service.dto.clientDto.ProductDto;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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
    public ProductDto getProductById(String id){
            String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUriString();
            ResponseEntity<ProductDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, createHttpEntity(null), ProductDto.class
            );
            log.debug("Retrieved product: {}", id);
            return response.getBody();
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Service-Auth", internalToken);
        return new HttpEntity<>(body, headers);
    }

    private ProductDto getProductByIdFallback(String id, Exception e) {
        log.warn("Using fallback for product: {}, error: {}", id, e.getMessage());
        ProductDto fallbackProduct = new ProductDto();
        fallbackProduct.setId(UUID.fromString(id));
        fallbackProduct.setDescription("Product unavailable");
        fallbackProduct.setInStock(false);
        return fallbackProduct;
    }
}
