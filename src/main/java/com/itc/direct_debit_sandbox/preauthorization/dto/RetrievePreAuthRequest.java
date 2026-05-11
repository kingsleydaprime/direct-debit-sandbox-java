package com.itc.direct_debit_sandbox.preauthorization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Body for POST /direct-debit/pre-authorization/retrieve/details */
@Data
public class RetrievePreAuthRequest {

    @NotBlank
    private String referenceId;   // third-party referenceNo used at creation

    @NotBlank
    private String productId;
}
