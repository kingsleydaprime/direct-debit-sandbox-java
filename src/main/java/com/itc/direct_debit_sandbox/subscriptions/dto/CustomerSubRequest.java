package com.itc.direct_debit_sandbox.subscriptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustomerSubRequest {
    @Schema(example = "0241234001")
    private String debitAccount;
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;
}
