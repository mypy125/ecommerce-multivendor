package com.mygitgor.seller_service.client;

import com.mygitgor.seller_service.dto.client.order.OrderDto;
import com.mygitgor.seller_service.dto.client.order.OrderStatus;
import com.mygitgor.seller_service.dto.client.order.UpdateOrderStatusRequest;
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

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClient {
    private final RestTemplate restTemplate;

    @Value("${order.service.url:http://localhost:8085/api/orders}")
    private String orderServiceUrl;

    @Value("${internal.auth.token}")
    private String internalToken;

    @CircuitBreaker(name = "orderService", fallbackMethod = "getSellerOrdersFallback")
    @Retry(name = "orderService", fallbackMethod = "getSellerOrdersFallback")
    @RateLimiter(name = "orderService")
    public List<OrderDto> getSellerOrders(String sellerId) {
        String url = UriComponentsBuilder.fromHttpUrl(orderServiceUrl)
                .path("/seller/{sellerId}")
                .buildAndExpand(sellerId)
                .toUriString();
        ResponseEntity<OrderDto[]> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(null), OrderDto[].class
        );
        log.debug("Retrieved orders for seller: {}", sellerId);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();

    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "updateOrderStatusFallback")
    @Retry(name = "orderService", fallbackMethod = "updateOrderStatusFallback")
    public Boolean updateOrderStatus(String orderId, OrderStatus orderStatus) {
        String url = UriComponentsBuilder.fromHttpUrl(orderServiceUrl)
                .path("/{orderId}/status")
                .buildAndExpand(orderId)
                .toUriString();

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(orderStatus);

        ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                createHttpEntity(request),
                Boolean.class
        );
        log.debug("Updated order {} status to: {}", orderId, orderStatus);
        return Boolean.TRUE.equals(response.getBody());

    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderByIdFallback")
    @Retry(name = "orderService", fallbackMethod = "getOrderByIdFallback")
    public OrderDto getOrderById(String orderId) {
        String url = UriComponentsBuilder.fromHttpUrl(orderServiceUrl)
                .path("/{orderId}")
                .buildAndExpand(orderId)
                .toUriString();

        ResponseEntity<OrderDto> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(null), OrderDto.class);
        log.debug("Retrieved order: {}", orderId);
        return response.getBody();

    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "getUserOrdersFallback")
    @Retry(name = "orderService", fallbackMethod = "getUserOrdersFallback")
    public List<OrderDto> getUserOrders(String userId) {
        String url = UriComponentsBuilder.fromHttpUrl(orderServiceUrl)
                .path("/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();
        ResponseEntity<OrderDto[]> response = restTemplate.exchange(
                url, HttpMethod.GET,createHttpEntity(null), OrderDto[].class);
        log.debug("Retrieved orders for user: {}", userId);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Service-Auth", internalToken);
        return new HttpEntity<>(body, headers);
    }

    private List<OrderDto> getSellerOrdersFallback(String sellerId, Exception e) {
        log.warn("Using fallback for seller orders: {}, error: {}", sellerId, e.getMessage());
        return Collections.emptyList();
    }

    private Boolean updateOrderStatusFallback(String orderId, OrderStatus orderStatus, Exception e) {
        log.warn("Using fallback for updating order status: {}, status: {}, error: {}",
                orderId, orderStatus, e.getMessage());
        return false;
    }

    private OrderDto getOrderByIdFallback(String orderId, Exception e) {
        log.warn("Using fallback for order: {}, error: {}", orderId, e.getMessage());
        throw new RuntimeException("Order service unavailable for order: " + orderId);
    }

    private List<OrderDto> getUserOrdersFallback(String userId, Exception e) {
        log.warn("Using fallback for user orders: {}, error: {}", userId, e.getMessage());
        return Collections.emptyList();
    }

}
