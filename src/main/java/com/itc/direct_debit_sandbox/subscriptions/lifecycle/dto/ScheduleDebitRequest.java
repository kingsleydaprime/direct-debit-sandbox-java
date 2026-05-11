package com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto;

import com.itc.direct_debit_sandbox.subscriptions.Channel;
import lombok.Data;

@Data
public class ScheduleDebitRequest {
    private String merchantId;
    private String productId;
    private String country;
    private String debitDate;
    private String debitTime;
    private String debitNotificationAccount;
    private String referenceNo;
    private Channel channel;
    private String currency;
    private String debitAccount;
    private String debitAmount;
    private String callbackUrl;

}
