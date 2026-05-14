package com.itc.direct_debit_sandbox.preauthorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TriggerMandateDebitRequest {

    @NotBlank
    @Schema(example = "replace-with-mandateId", description = "Returned in the preapproval callback after creating the preauth")
    private String mandateId;

    @NotBlank
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;

    @NotBlank
    @Schema(example = "100.00")
    private String debitAmount;

    @NotBlank
    @Schema(example = "Monthly premium payment")
    private String narration;

    @NotBlank
    @Schema(example = "REF-MANDATE-001")
    private String referenceNo;

    @NotBlank
    @Schema(example = "0241234001")
    private String debitAccount;

    @NotBlank
    @Schema(example = "GHS")
    private String currency;
}
