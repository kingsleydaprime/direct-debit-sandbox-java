package com.itc.direct_debit_sandbox.subscriptions.dto;
import com.itc.direct_debit_sandbox.subscriptions.FrequencyType;
import jakarta.validation.constraints.NotBlank;
import com.itc.direct_debit_sandbox.store.ConfigurationItem;
import lombok.Data;
import java.util.List;



@Data
public class SubscriptionRequestDto {




@NotBlank
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
    private Boolean triggerDebitStatus;
    private Boolean notificationStatus;
    private String debitNotificationAccount;

    private List<ConfigurationItem> configuration;


}
