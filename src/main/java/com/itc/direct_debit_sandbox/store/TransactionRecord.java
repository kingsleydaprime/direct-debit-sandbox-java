package com.itc.direct_debit_sandbox.store;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionRecord {
    private String id;
    private String networkTransactionId;
    private String subscriptionId;
    private String merchantId;
    private String productId;
    private String debitAccount;
    private String debitAmount;
    private String reference;
    private String narration;
    private String channel;
    private String responseCode;
    private String responseMessage;
    private String timestamp;
    private String charge;
    private String status; // PROCESSING, SUCCESS, FAILED, RETRYING, EXHAUSTED
    private int retriesUsed;
    private int maxRetries;
}