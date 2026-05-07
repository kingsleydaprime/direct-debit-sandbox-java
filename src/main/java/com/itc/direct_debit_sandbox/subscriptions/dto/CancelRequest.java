package com.itc.direct_debit_sandbox.subscriptions.dto;

import lombok.Data;

@Data
public class CancelRequest {
    private String subscriptionId;
    private String productId;
}
