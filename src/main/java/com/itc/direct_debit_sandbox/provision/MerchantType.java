package com.itc.direct_debit_sandbox.provision;

/**
 * The three categories of direct-debit merchant.
 *
 * SUBSCRIPTIONS_ONLY   — automated recurring debits only; cannot use mandate/preauth endpoints
 * HYBRID               — automated debits + manual trigger for failed autodebits; cannot use mandate/preauth endpoints
 * PREAUTHORIZED_ONLY   — standing mandate; manual on-demand debits; cannot use subscription endpoints
 *
 * This is enforced on /mandate/trigger-debit and /pre-authorization/authorize.
 */
public enum MerchantType {
    SUBSCRIPTIONS_ONLY,
    HYBRID,
    PREAUTHORIZED_ONLY
}
