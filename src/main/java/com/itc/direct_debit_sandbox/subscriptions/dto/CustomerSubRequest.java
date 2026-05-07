package com.itc.direct_debit_sandbox.subscriptions.dto;

import lombok.Data;

@Data
public class CustomerSubRequest {
    private String debitAccount;
    private String productId;
}
