package com.itc.direct_debit_sandbox.preauthorization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Body for POST /mandate/trigger-debit.
 *
 * Unlike subscription trigger-debit (which uses a referenceId to look up a failed
 * subscription), mandate trigger-debit uses the mandateId that was returned in the
 * preapproval callback. The caller also supplies the specific debit details
 * (amount, account, narration, reference) because each mandate debit can have
 * different parameters — preauth is just the standing permission.
 */
@Data
public class TriggerMandateDebitRequest {

    @NotBlank
    private String mandateId;

    @NotBlank
    private String productId;

    @NotBlank
    private String debitAmount;

    @NotBlank
    private String narration;

    @NotBlank
    private String referenceNo;

    @NotBlank
    private String debitAccount;

    @NotBlank
    private String currency;
}
