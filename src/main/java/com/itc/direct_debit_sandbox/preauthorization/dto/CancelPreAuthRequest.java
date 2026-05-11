package com.itc.direct_debit_sandbox.preauthorization.dto;

import com.itc.direct_debit_sandbox.subscriptions.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Body for POST /pre-authorization/cancel */
@Data
public class CancelPreAuthRequest {

    @NotBlank
    private String preApprovalId;   // ID returned in the preapproval callback

    @NotBlank
    private String productId;

    @NotBlank
    private String debitAccount;

    @NotNull
    private Channel channel;

    @NotBlank
    private String country;
}
