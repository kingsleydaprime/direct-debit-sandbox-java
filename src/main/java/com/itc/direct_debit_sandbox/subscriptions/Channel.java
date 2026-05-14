package com.itc.direct_debit_sandbox.subscriptions;

/**
 * Supported debit channels. Using an enum here means Jackson will automatically
 * reject any value that isn't on this list — no manual validation needed.
 */
public enum Channel {
    MTN,
    TELECEL,
    AT,
    AIRTEL,
    BANK,
    CARD,
    VODAFONE,
}
