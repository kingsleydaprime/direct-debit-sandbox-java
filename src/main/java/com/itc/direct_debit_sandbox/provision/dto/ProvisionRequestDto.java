package com.itc.direct_debit_sandbox.provision.dto;

import com.itc.direct_debit_sandbox.provision.MerchantType;
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

    // Determines which endpoints the merchant is allowed to use.
    // Jackson will reject any value not in the MerchantType enum.
    private MerchantType merchantType;

    // Optional catalogue configuration — if omitted the defaults already stored are kept
    private Integer retryAttempts;
    private Integer skipFactor;
    private String daysToDebitDayNotice;
}
