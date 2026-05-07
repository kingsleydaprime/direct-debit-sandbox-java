package com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto;

import lombok.Data;

@Data
public class ResumeRequest {
    private String subscriptionId;
    private String productId;
}
