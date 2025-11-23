package com.mygitgor.product_service.dto.client;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkStatusUpdateRequest {
    @NotEmpty
    private List<UUID> productIds;

    @NotNull
    private Boolean active;
}
