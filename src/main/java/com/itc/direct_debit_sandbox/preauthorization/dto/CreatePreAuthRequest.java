package com.itc.direct_debit_sandbox.preauthorization.dto;

import com.itc.direct_debit_sandbox.subscriptions.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePreAuthRequest {

    @NotBlank
    @Schema(example = "233241234001", description = "Last 3 digits control the simulated outcome — 001=success, 004=pre-approval pending")
    private String debitAccount;

    @NotBlank
    @Schema(example = "c64bf5f9-f147-4232-8d00-f28105823d6a")
    private String merchantId;

    @NotBlank
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;

    @NotNull
    @Schema(example = "MTN")
    private Channel channel;

    @NotBlank
    @Schema(example = "REF-PREAUTH-001")
    private String referenceNo;

    @NotBlank
    @Schema(example = "GH")
    private String country;

    @NotBlank
    @Schema(example = "2026-06-01")
    private String startDate;

    @NotBlank
    @Schema(example = "2027-06-01", description = "Must be after startDate")
    private String endDate;

    @Schema(example = "https://webhook.site/05b66f52-ae04-4130-a287-db6737198eb9", description = "Optional — falls back to the provisioned callback URL")
    private String callbackUrl;
}
