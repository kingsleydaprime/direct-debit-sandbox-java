package com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto;

import lombok.Data;

@Data
public class RetrieveByReferenceRequest {

    private String referenceId;
    private String productId;

}
