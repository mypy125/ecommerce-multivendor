package com.mygitgor.product_service.dto.client;

import lombok.Data;

@Data
public class UpdateStockRequest {
    private Boolean inStock;
    private Integer quantity;
}
