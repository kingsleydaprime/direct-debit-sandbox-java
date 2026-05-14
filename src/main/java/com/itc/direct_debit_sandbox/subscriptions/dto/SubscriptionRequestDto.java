package com.itc.direct_debit_sandbox.subscriptions.dto;
import com.itc.direct_debit_sandbox.subscriptions.Channel;
import com.itc.direct_debit_sandbox.subscriptions.FrequencyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.itc.direct_debit_sandbox.store.ConfigurationItem;
import lombok.Data;
import java.util.List;

@Data
public class SubscriptionRequestDto {

    @NotBlank
    @Schema(example = "c64bf5f9-f147-4232-8d00-f28105823d6a")
    private String merchantId;

    @NotBlank
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;

    @NotBlank
    @Schema(example = "0241234001", description = "Last 3 digits control the simulated outcome — 001=success, 002=fail+retry, 003=fail+fail+retry")
    private String debitAccount;

    @NotBlank
    @Schema(example = "50.00")
    private String debitAmount;

    @NotNull
    @Schema(example = "MONTHLY")
    private FrequencyType frequencyType;

    @NotBlank
    @Schema(example = "2026-06-01")
    private String startDate;

    @Schema(example = "2027-06-01")
    private String endDate;

    @NotBlank
    @Schema(example = "15")
    private String debitDay;

    @NotBlank
    @Schema(example = "08:00")
    private String debitTime;

    @NotNull
    @Schema(example = "MTN")
    private Channel channel;

    @NotBlank
    @Schema(example = "GHS")
    private String currency;

    @NotBlank
    @Schema(example = "REF-001")
    private String referenceNo;

    @Schema(example = "true")
    private Boolean triggerDebitStatus;
    @Schema(example = "false")
    private Boolean notificationStatus;
    @Schema(example = "0241234001")
    private String debitNotificationAccount;
    private List<ConfigurationItem> configuration;
}
