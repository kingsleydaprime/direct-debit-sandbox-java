package com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto;

import lombok.Data;

@Data
public class PauseRequest {
    private String subscriptionId;
    private String productId;
}
