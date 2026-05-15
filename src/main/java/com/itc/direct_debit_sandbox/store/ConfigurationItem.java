package com.itc.direct_debit_sandbox.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConfigurationItem {

    @Schema(
        example = "retryAttempts",
        description = "`retryAttempts` | `skipFactor` | `daysToDebitDayNotice`"
    )
    private String name;

    @Schema(
        example = "3",
        description = "Integer string for `retryAttempts` (min 1) and `skipFactor` (min 1). " +
                      "Comma-separated day list for `daysToDebitDayNotice` (e.g. `\"1,3\"` notifies 3 days and 1 day before debit)."
    )
    private String value;
}
