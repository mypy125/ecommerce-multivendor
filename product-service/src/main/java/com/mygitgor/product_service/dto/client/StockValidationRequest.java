package com.mygitgor.product_service.dto.client;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class StockValidationRequest {
    @NotEmpty
    private Map<UUID, Integer> productQuantities;
}
