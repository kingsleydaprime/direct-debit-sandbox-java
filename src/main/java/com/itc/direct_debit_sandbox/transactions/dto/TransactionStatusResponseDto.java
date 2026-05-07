package com.itc.direct_debit_sandbox.transactions.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionStatusResponseDto {
    private String responseCode;
    private String status;
}