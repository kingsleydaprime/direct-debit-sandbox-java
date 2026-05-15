package com.itc.direct_debit_sandbox.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * In-memory record for a preauthorization mandate.
 *
 * Field names match the API's retrieve-details response so the record can be
 * returned directly without a mapping step. The few fields that differ between
 * request and response are noted inline.
 *
 * Request field → stored as
 *   channel     → channel  (same, also exposed as debitSource in response)
 *   referenceNo → referenceNo (also exposed as refNo in response)
 *   country     → countryId (renamed to match response schema)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreAuthRecord {

    // ─── IDs ────────────────────────────────────────────────────────────────
    private String preApprovalId;   // primary key, returned to the caller
    private String mandateId;       // secondary ID used in callbacks and trigger-debit

    // ─── IDENTITY ───────────────────────────────────────────────────────────
    private String merchantId;
    private String productId;
    private String clientName;
    private String debitAccount;
    private String countryId;       // stored as countryId to match response schema
    private String channel;         // e.g. "MTN" — also returned as debitSource
    private String referenceNo;     // third-party reference; also returned as refNo

    // ─── WINDOW ─────────────────────────────────────────────────────────────
    private String startDate;       // yyyy-MM-dd: earliest date a mandate debit is allowed
    private String endDate;         // yyyy-MM-dd: preauth expires on this date

    // ─── STATE ──────────────────────────────────────────────────────────────
    private String status;          // ACTIVE | CANCELLED

    // ─── INTERNAL ───────────────────────────────────────────────────────────
    // callbackUrl is not returned in responses — it is only used when routing
    // async callbacks and the provision store has no entry for this merchant+product.
    private String callbackUrl;

    // ─── TIMESTAMPS ─────────────────────────────────────────────────────────
    private String createdAt;
    private String updatedAt;
}
