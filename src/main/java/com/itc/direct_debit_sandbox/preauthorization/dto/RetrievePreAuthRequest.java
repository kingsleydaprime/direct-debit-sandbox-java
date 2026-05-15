package com.itc.direct_debit_sandbox.preauthorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RetrievePreAuthRequest {

    @NotBlank
    @Schema(example = "REF-PREAUTH-001", description = "The referenceNo used at preauth creation")
    private String referenceId;

    @NotBlank
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;
}
