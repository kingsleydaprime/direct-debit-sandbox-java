package com.itc.direct_debit_sandbox.preauthorization.dto;

import com.itc.direct_debit_sandbox.subscriptions.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePreAuthRequest {

    @NotBlank
    private String debitAccount;

    @NotBlank
    private String merchantId;

    @NotBlank
    private String productId;

    @NotNull
    private Channel channel;

    @NotBlank
    private String referenceNo;

    @NotBlank
    private String country;

    @NotBlank
    private String startDate;   // yyyy-MM-dd

    @NotBlank
    private String endDate;     // yyyy-MM-dd — must be after startDate

    // Optional: per-request fallback if no provision has been registered
    private String callbackUrl;
}
