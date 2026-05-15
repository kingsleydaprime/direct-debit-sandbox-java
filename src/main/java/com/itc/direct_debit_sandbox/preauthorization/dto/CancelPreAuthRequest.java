package com.itc.direct_debit_sandbox.preauthorization.dto;

import com.itc.direct_debit_sandbox.subscriptions.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelPreAuthRequest {

    @NotBlank
    @Schema(example = "replace-with-preApprovalId", description = "Returned in the preapproval callback")
    private String preApprovalId;

    @NotBlank
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;

    @NotBlank
    @Schema(example = "233241234001", description = "Must match the account used when creating the preauth")
    private String debitAccount;

    @NotNull
    @Schema(example = "MTN", description = "Must match the channel used when creating the preauth")
    private Channel channel;

    @NotBlank
    @Schema(example = "GH", description = "Must match the country used when creating the preauth")
    private String country;
}
