package com.itc.direct_debit_sandbox.preauthorization;

import com.itc.direct_debit_sandbox.preauthorization.dto.*;
import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Preauthorization", description = "Create and manage standing debit mandates. No money moves at creation — debits are triggered on demand via /mandate/trigger-debit.")
@RestController
@RequiredArgsConstructor
public class PreAuthController {

    private final PreAuthService preAuthService;

    // ─── CREATE PREAUTH ──────────────────────────────────────────────────────

    @Operation(
        summary = "Create a preauthorization mandate",
        description = "Grants the merchant standing permission to debit the customer's account at any time " +
            "within the `startDate`–`endDate` window. Returns `03` immediately. A preapproval callback fires after ~2 s. " +
            "Only available to `PREAUTHORIZED_ONLY` products."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `03` | Mandate created and being processed |\n" +
            "| `100` | Product type mismatch, duplicate reference, or invalid date range |\n" +
            "| `107` | Invalid credentials |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/pre-authorization/authorize")
    public ResponseEntity<?> createPreAuth(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody CreatePreAuthRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.createPreAuth(transflowId, apiKey, country, req));
    }

    // ─── TRIGGER MANDATE DEBIT ───────────────────────────────────────────────

    @Operation(
        summary = "Trigger a mandate debit",
        description = "Executes a specific debit against an existing preauth mandate. Each call specifies its own " +
            "`debitAmount`, `narration`, and `referenceNo` — so each debit can differ. " +
            "Returns `03`; a transaction callback fires after ~5 s."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `03` | Debit accepted and being processed |\n" +
            "| `100` | Mandate not found, not active, window not started yet, or expired |\n" +
            "| `107` | Invalid credentials |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/mandate/trigger-debit")
    public ResponseEntity<?> triggerMandateDebit(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody TriggerMandateDebitRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.triggerMandateDebit(transflowId, apiKey, country, req));
    }

    // ─── CHECK MANDATE STATUS ────────────────────────────────────────────────

    @Operation(
        summary = "Check mandate status",
        description = "Returns the current status of a preauth mandate (ACTIVE or CANCELLED). Lookup is by `referenceNo`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Status returned — `responseMessage` is `active` or `cancelled` |\n" +
            "| `100` | Mandate not found |\n" +
            "| `107` | Invalid credentials |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/mandate/check-status")
    public ResponseEntity<?> checkMandateStatus(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody CheckMandateStatusRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.checkMandateStatus(transflowId, apiKey, country, req));
    }

    // ─── RETRIEVE PREAUTH DETAILS ────────────────────────────────────────────

    @Operation(
        summary = "Retrieve mandate details",
        description = "Returns the full preauth record including `preApprovalId`, `mandateId`, status, and date window. Lookup is by `referenceId` (= the `referenceNo` supplied at creation)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Full mandate record returned |\n" +
            "| `100` | Mandate not found for this reference |\n" +
            "| `107` | Invalid credentials |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/direct-debit/pre-authorization/retrieve/details")
    public ResponseEntity<?> retrievePreAuthDetails(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody RetrievePreAuthRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(preAuthService.retrievePreAuthDetails(transflowId, apiKey, country, req));
    }

    // ─── CANCEL PREAUTH ──────────────────────────────────────────────────────

    @Operation(
        summary = "Cancel a preauthorization",
        description = "Cancels an ACTIVE mandate. `debitAccount`, `channel`, and `country` must match the original record to confirm ownership."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Mandate cancelled successfully |\n" +
            "| `100` | Not found, already cancelled, or ownership details do not match |\n" +
            "| `107` | Invalid credentials |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/pre-authorization/cancel")
    public ResponseEntity<?> cancelPreAuth(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
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
