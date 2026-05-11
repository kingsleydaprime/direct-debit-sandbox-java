package com.itc.direct_debit_sandbox.preauthorization;

import com.itc.direct_debit_sandbox.preauthorization.dto.*;
import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Preauthorization Management endpoints.
 *
 * A preauth grants the merchant standing permission to debit a customer's account
 * at any point within a defined date window. No debit occurs at creation — money
 * only moves when /mandate/trigger-debit is called.
 *
 * Route summary (matching the real API spec):
 *   POST /pre-authorization/authorize                        → create preauth
 *   POST /mandate/trigger-debit                             → trigger a preauth debit
 *   POST /mandate/check-status                              → check preauth status
 *   POST /direct-debit/pre-authorization/retrieve/details   → get full mandate details
 *   POST /pre-authorization/cancel                          → cancel preauth
 */
@RestController
@RequiredArgsConstructor
public class PreAuthController {

    private final PreAuthService preAuthService;

    // ─── CREATE PREAUTH ──────────────────────────────────────────────────────

    @PostMapping("/pre-authorization/authorize")
    public ResponseEntity<?> createPreAuth(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key",         required = false) String apiKey,
            @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody CreatePreAuthRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.createPreAuth(transflowId, apiKey, country, req));
    }

    // ─── TRIGGER MANDATE DEBIT ───────────────────────────────────────────────

    @PostMapping("/mandate/trigger-debit")
    public ResponseEntity<?> triggerMandateDebit(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key",         required = false) String apiKey,
            @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody TriggerMandateDebitRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.triggerMandateDebit(transflowId, apiKey, country, req));
    }

    // ─── CHECK MANDATE STATUS ────────────────────────────────────────────────

    @PostMapping("/mandate/check-status")
    public ResponseEntity<?> checkMandateStatus(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key",         required = false) String apiKey,
            @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody CheckMandateStatusRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.checkMandateStatus(transflowId, apiKey, country, req));
    }

    // ─── RETRIEVE PREAUTH DETAILS ────────────────────────────────────────────

    @PostMapping("/direct-debit/pre-authorization/retrieve/details")
    public ResponseEntity<?> retrievePreAuthDetails(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key",         required = false) String apiKey,
            @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody RetrievePreAuthRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.retrievePreAuthDetails(transflowId, apiKey, country, req));
    }

    // ─── CANCEL PREAUTH ──────────────────────────────────────────────────────

    @PostMapping("/pre-authorization/cancel")
    public ResponseEntity<?> cancelPreAuth(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key",         required = false) String apiKey,
            @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody CancelPreAuthRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.cancelPreAuth(transflowId, apiKey, country, req));
    }

    // ─── SHARED HELPERS ──────────────────────────────────────────────────────

    private boolean isUnauthorized(String transflowId, String key, String country) {
        return transflowId == null || transflowId.isBlank() ||
               key        == null || key.isBlank()         ||
               country    == null || country.isBlank();
    }

    private ResponseEntity<?> buildUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.builder()
                        .responseCode("107")
                        .responseMessage("Invalid Credentials")
                        .build());
    }
}
