package com.itc.direct_debit_sandbox.subscriptions.dto;

import java.util.List;

import com.itc.direct_debit_sandbox.store.ConfigurationItem;
import com.itc.direct_debit_sandbox.subscriptions.Channel;
import com.itc.direct_debit_sandbox.subscriptions.FrequencyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateRequest {

    @Schema(example = "replace-with-subscriptionId")
    private String subscriptionId;

    @Schema(example = "c64bf5f9-f147-4232-8d00-f28105823d6a")
    private String merchantId;

    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;

    @Schema(example = "233241234001")
    private String debitAccount;

    @Schema(example = "75.00")
    private String debitAmount;

    @Schema(example = "MONTHLY")
    private FrequencyType frequencyType;

    @Schema(example = "2026-06-01")
    private String startDate;

    @Schema(example = "2027-06-01")
    private String endDate;

    @Schema(example = "15")
    private String debitDay;

    @Schema(example = "08:00")
    private String debitTime;

    @Schema(example = "MTN")
    private Channel channel;

    @Schema(example = "GHS")
    private String currency;

    @Schema(example = "REF-001")
    private String referenceNo;

    @Schema(example = "true")
    private Boolean triggerDebitStatus;

    @Schema(example = "false")
    private Boolean notificationStatus;

    @Schema(example = "233241234001")
    private String debitNotificationAccount;

    @Schema(description = "Optional per-subscription overrides for product defaults",
            example = "[{\"name\":\"retryAttempts\",\"value\":\"3\"},{\"name\":\"skipFactor\",\"value\":\"2\"},{\"name\":\"daysToDebitDayNotice\",\"value\":\"1,3\"}]")
    private List<ConfigurationItem> configuration;
}
