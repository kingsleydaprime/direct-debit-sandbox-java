package com.itc.direct_debit_sandbox.callbacks.dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionCallbackPayloadDto {
    private String responseCode;
    private String responseMessage;
    private String debitOrderTransactionId;
    private String networkTransactionId;
    private String merchantId;
    private String productId;
    private String mandateId;
    private String debitAccount;
    private String debitAmount;
    private String reference;
    private String narration;
    private String timestamp;
    private String channel;
    private String charge;
}