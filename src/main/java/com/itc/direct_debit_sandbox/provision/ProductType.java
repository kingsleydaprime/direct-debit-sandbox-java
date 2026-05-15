package com.itc.direct_debit_sandbox.provision;

/**
 * The three product categories for direct-debit integration.
 *
 * SUBSCRIPTIONS_ONLY   — automated recurring debits only
 * HYBRID               — automated debits + manual trigger for failed autodebits
 * PREAUTHORIZED_ONLY   — standing mandate; manual on-demand debits only
 */
public enum ProductType {
    SUBSCRIPTIONS_ONLY,
    HYBRID,
    PREAUTHORIZED_ONLY
}
