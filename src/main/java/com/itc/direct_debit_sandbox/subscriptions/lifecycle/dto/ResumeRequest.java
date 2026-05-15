package com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResumeRequest {
    @Schema(example = "replace-with-subscriptionId")
    private String subscriptionId;
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;
}
