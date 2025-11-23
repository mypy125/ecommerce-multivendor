package com.mygitgor.product_service.dto.client;

import lombok.Data;

@Data
public class SellerProductStats {
    private Long totalProducts;
    private Long activeProducts;
    private Long outOfStockProducts;
    private Long totalInventoryValue;
}
