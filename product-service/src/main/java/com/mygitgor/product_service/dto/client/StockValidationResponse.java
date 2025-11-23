package com.mygitgor.product_service.dto.client;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class StockValidationResponse {
    private Boolean valid;
    private Map<UUID, String> errors;
    private List<UUID> outOfStockProducts;
}
