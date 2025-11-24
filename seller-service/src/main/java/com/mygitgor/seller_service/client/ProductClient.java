package com.mygitgor.seller_service.client;

import com.mygitgor.seller_service.dto.client.CreateProductRequest;
import com.mygitgor.seller_service.dto.client.ProductDto;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {
    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8086/api/products}")
    private String productServiceUrl;

    @Value("${internal.auth.token}")
    private String internalToken;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductBySellerIdFallback")
    @Retry(name = "productService", fallbackMethod = "getProductBySellerIdFallback")
    @RateLimiter(name = "productService")
    public List<ProductDto> getProductBySellerId(String sellerId){
        String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                .path("/seller/{sellerId}")
                .buildAndExpand(sellerId)
                .toUriString();

        ResponseEntity<ProductDto[]> response = restTemplate.exchange(
                url,HttpMethod.GET, createHttpEntity(null), ProductDto[].class
        );

        log.debug("Retrieved products for seller: {}", sellerId);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "createProductFallback")
    @Retry(name = "productService", fallbackMethod = "createProductFallback")
    public ProductDto createProduct(CreateProductRequest request, String sellerId){
        String url = productServiceUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Service-Auth", internalToken);
        headers.set("X-Seller-Id", sellerId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateProductRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, ProductDto.class
        );

        log.info("Created product for seller: {}", sellerId);
        return response.getBody();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "updateProductFallback")
    @Retry(name = "productService", fallbackMethod = "updateProductFallback")
    public ProductDto updateProduct(String productId, ProductDto productDto){
        String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                .path("/{productId}")
                .buildAndExpand(productId)
                .toUriString();

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                url, HttpMethod.PUT, createHttpEntity(productDto), ProductDto.class
        );

        log.debug("Updated product: {}", productId);
        return response.getBody();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "deleteProductFallback")
    @Retry(name = "productService", fallbackMethod = "deleteProductFallback")
    public Boolean deleteProduct(String productId) {
        String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                .path("/{productId}")
                .buildAndExpand(productId)
                .toUriString();

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                createHttpEntity(null),
                Void.class
        );
        log.info("Deleted product: {}", productId);
        return response.getStatusCode().is2xxSuccessful();

    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "productService", fallbackMethod = "getProductByIdFallback")
    public ProductDto getProductById(String productId) {
        String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                .path("/{productId}")
                .buildAndExpand(productId)
                .toUriString();

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                url,HttpMethod.GET, createHttpEntity(null), ProductDto.class
        );
        log.debug("Retrieved product: {}", productId);
        return response.getBody();
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Service-Auth", internalToken);
        return new HttpEntity<>(body, headers);
    }

    private List<ProductDto> getProductBySellerIdFallback(String sellerId, Exception e) {
        log.warn("Using fallback for seller products: {}, error: {}", sellerId, e.getMessage());
        return Collections.emptyList();
    }

    private ProductDto createProductFallback(CreateProductRequest request, String sellerId, Exception e) {
        log.warn("Using fallback for product creation, seller: {}, error: {}", sellerId, e.getMessage());
        throw new RuntimeException("Product service unavailable for creation");
    }

    private ProductDto updateProductFallback(String productId, ProductDto productDto, Exception e) {
        log.warn("Using fallback for product update: {}, error: {}", productId, e.getMessage());
        throw new RuntimeException("Product service unavailable for update");
    }

    private Boolean deleteProductFallback(String productId, Exception e) {
        log.warn("Using fallback for product deletion: {}, error: {}", productId, e.getMessage());
        return false;
    }

    private ProductDto getProductByIdFallback(String productId, Exception e) {
        log.warn("Using fallback for product: {}, error: {}", productId, e.getMessage());
        throw new RuntimeException("Product service unavailable for product: " + productId);
    }
}
