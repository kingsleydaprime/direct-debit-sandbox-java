package com.itc.direct_debit_sandbox.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TransactionStatusRequestDto {
    @Schema(example = "REF-001")
    private String reference;
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;
    @Schema(example = "replace-with-mandateId", description = "Only required for mandate (preauth) transactions")
    private String mandateId;
}
