// TransactionStatusRequest.java
package com.itc.direct_debit_sandbox.transactions.dto;

import lombok.Data;

@Data
public class TransactionStatusRequestDto {
    private String reference;
    private String productId;
    private String mandateId;
}