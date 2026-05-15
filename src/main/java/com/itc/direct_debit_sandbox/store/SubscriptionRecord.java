package com.itc.direct_debit_sandbox.store;


import com.itc.direct_debit_sandbox.subscriptions.FrequencyType;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SubscriptionRecord {
    private String id;
    // private String mandateId;
    private String merchantId;
    private String productId;
    private String debitAccount;
    private String debitAmount;
    private FrequencyType frequencyType;
    private String startDate;
    private String endDate;
    private String debitDay;
    private String debitTime;
    private String referenceNo;
    private String channel;
    private String currency;
    private String country;
    private String debitNotificationAccount;

    private String status; // ACTIVE, PAUSED, CANCELLED
    private boolean triggerDebitStatus;
    private boolean notificationStatus;
    private List<ConfigurationItem> configuration;
    private String createdAt;
}
