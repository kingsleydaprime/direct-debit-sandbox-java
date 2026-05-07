package com.itc.direct_debit_sandbox.callbacks.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreapprovalCallbackPayloadDto {
    private String responseCode;
    private String responseMessage;
    private String mandateId;
    private String merchantId;
    private String productId;
    private String debitAccount;
    private String reference;
    private String channel;
    private String country;
}