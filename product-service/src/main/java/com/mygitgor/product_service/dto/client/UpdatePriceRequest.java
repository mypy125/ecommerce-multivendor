package com.mygitgor.product_service.dto.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePriceRequest {
    @NotNull
    @Min(0)
    private Integer mrpPrice;

    @NotNull
    @Min(0)
    private Integer sellingPrice;
}
