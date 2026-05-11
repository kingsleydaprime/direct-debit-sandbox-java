package com.itc.direct_debit_sandbox.subscriptions.dto;
import com.itc.direct_debit_sandbox.subscriptions.Channel;
import com.itc.direct_debit_sandbox.subscriptions.FrequencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.itc.direct_debit_sandbox.store.ConfigurationItem;
import lombok.Data;
import java.util.List;

@Data
public class SubscriptionRequestDto {

    @NotBlank
    private String merchantId;

    @NotBlank
    private String productId;

    @NotBlank
    private String debitAccount;

    @NotBlank
    private String debitAmount;

    @NotNull
    private FrequencyType frequencyType;

    @NotBlank
    private String startDate;

    private String endDate;

    @NotBlank
    private String debitDay;

    @NotBlank
    private String debitTime;

    // @NotNull ensures the value must be one of the Channel enum constants.
    // Jackson automatically rejects unrecognized strings with a 400.
    @NotNull
    private Channel channel;

    @NotBlank
    private String currency;

    private String referenceNo;
    private Boolean triggerDebitStatus;
    private Boolean notificationStatus;
    private String debitNotificationAccount;
    private List<ConfigurationItem> configuration;
}
