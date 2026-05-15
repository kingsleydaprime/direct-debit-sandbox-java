package com.itc.direct_debit_sandbox.provision.dto;

import com.itc.direct_debit_sandbox.provision.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProvisionRequestDto {

    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
             message = "merchantId must be a valid UUID")
    @Schema(example = "c64bf5f9-f147-4232-8d00-f28105823d6a")
    private String merchantId;

    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
             message = "productId must be a valid UUID")
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;

    @NotBlank
    @Schema(example = "https://webhook.site/05b66f52-ae04-4130-a287-db6737198eb9")
    private String callbackUrl;

    @Schema(example = "HYBRID", description = "HYBRID | SUBSCRIPTIONS_ONLY | PREAUTHORIZED_ONLY")
    private ProductType productType;

    @Schema(example = "3", description = "Max retry attempts on failure. Omit to keep existing value.")
    private Integer retryAttempts;

    @Schema(example = "1", description = "Number of billing cycles to skip after a failed + exhausted attempt.")
    private Integer skipFactor;

    @Schema(example = "3", description = "Days before debit day to send a notification.")
    private String daysToDebitDayNotice;
}
