package com.itc.direct_debit_sandbox.preauthorization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body for POST /mandate/check-status */
@Data
public class CheckMandateStatusRequest {

    @NotBlank
    private String reference;   // the referenceNo used when creating the preauth

    @NotBlank
    private String productId;
}
