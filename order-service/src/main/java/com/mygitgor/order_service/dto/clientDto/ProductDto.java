package com.mygitgor.order_service.dto.clientDto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductDto {
    private UUID id;
    private String title;
    private String description;
    private Integer mrpPrice;
    private Integer sellingPrice;
    private String color;
    private List<String> images;
    private Integer numRatings;
    private String size;
    private String categoryName;
    private String brand;
    private String sku;
    private Integer quantity;
    private UUID sellerId;
    private Boolean active;
    private String weight;
    private String dimensions;
    private Boolean inStock;
    private Boolean featured;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private String material;
    private String warranty;
}
