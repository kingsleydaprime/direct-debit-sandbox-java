package com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto;

import com.itc.direct_debit_sandbox.subscriptions.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScheduleDebitRequest {
    @Schema(example = "c64bf5f9-f147-4232-8d00-f28105823d6a")
    private String merchantId;
    @Schema(example = "ff469300-0a9f-43cc-92ca-25e0b75dfe18")
    private String productId;
    @Schema(example = "GH")
    private String country;
    @Schema(example = "2026-06-15", description = "Must be at least 24 hours from now (yyyy-MM-dd)")
    private String debitDate;
    @Schema(example = "08:00")
    private String debitTime;
    @Schema(example = "0241234001")
    private String debitNotificationAccount;
    @Schema(example = "REF-SCHED-001")
    private String referenceNo;
    @Schema(example = "MTN")
    private Channel channel;
    @Schema(example = "GHS")
    private String currency;
    @Schema(example = "0241234001")
    private String debitAccount;
    @Schema(example = "50.00")
    private String debitAmount;
    @Schema(example = "https://webhook.site/your-id")
    private String callbackUrl;
}
