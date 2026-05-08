package com.itc.direct_debit_sandbox.subscriptions.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateRequest extends SubscriptionRequestDto {
private String subscriptionId;

}
