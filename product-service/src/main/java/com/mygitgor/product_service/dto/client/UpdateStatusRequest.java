package com.mygitgor.product_service.dto.client;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull
    private Boolean active;
}
