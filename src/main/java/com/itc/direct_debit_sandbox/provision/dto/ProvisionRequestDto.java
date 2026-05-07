package com.itc.direct_debit_sandbox.provision.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProvisionRequestDto {

    @NotBlank
    private String merchantId;

    @NotBlank
    private String productId;

    @NotBlank
    private String callbackUrl;

    // Optional catalogue configuration — if omitted the defaults already stored are kept
    private Integer retryAttempts;
    private Integer skipFactor;
    private String daysToDebitDayNotice;
}
