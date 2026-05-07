package com.itc.direct_debit_sandbox.store;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvisionRecord {
    private String merchantId;
    private String productId;
    private String callbackUrl;
    // Optional catalogue configurations — mirror the API spec fields
    private Integer retryAttempts;
    private Integer skipFactor;
    private String daysToDebitDayNotice;
    private String registeredAt;
}
