package com.itc.direct_debit_sandbox.store;

import com.itc.direct_debit_sandbox.provision.ProductType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvisionRecord {
    private String merchantId;
    private String productId;
    private String callbackUrl;
    private String transflowId;
    private String apiKey;
    // Determines which API operations this product is permitted to use.
    // Null means the type was never set (treated as unconfigured in the sandbox).
    private ProductType productType;
    // Optional catalogue configurations — mirror the API spec fields
    private Integer retryAttempts;
    private Integer skipFactor;
    private String daysToDebitDayNotice;
    private String registeredAt;
}
