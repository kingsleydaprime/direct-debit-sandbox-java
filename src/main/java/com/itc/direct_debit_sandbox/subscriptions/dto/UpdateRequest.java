package com.itc.direct_debit_sandbox.subscriptions.dto;

import java.util.List;

import com.itc.direct_debit_sandbox.store.ConfigurationItem;
import com.itc.direct_debit_sandbox.subscriptions.Channel;
import com.itc.direct_debit_sandbox.subscriptions.FrequencyType;

// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateRequest  {
private String subscriptionId;

    private String merchantId;

    
    private String productId;

    
    private String debitAccount;

    
    private String debitAmount;

    
    private FrequencyType frequencyType;

    
    private String startDate;

    private String endDate;

    
    private String debitDay;

    
    private String debitTime;

    
    private Channel channel;

    
    private String currency;

    private String referenceNo;
    private Boolean triggerDebitStatus;
    private Boolean notificationStatus;
    private String debitNotificationAccount;
    private List<ConfigurationItem> configuration;

}
