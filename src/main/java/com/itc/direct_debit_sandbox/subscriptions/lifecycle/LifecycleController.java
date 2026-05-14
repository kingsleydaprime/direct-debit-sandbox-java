package com.itc.direct_debit_sandbox.subscriptions.lifecycle;

import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscription Lifecycle", description = "Pause, resume, trigger, schedule, and retrieve subscriptions")
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class LifecycleController {

    private final LifecycleService lifecycleService;

    @Operation(summary = "Pause a subscription", description = "Suspends an ACTIVE subscription. No debits fire while paused.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Subscription paused successfully |\n" +
            "| `100` | Not found, already paused, or already cancelled |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/pause")
    public ResponseEntity<?> pause(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key", required = false) String key,
            @Parameter(hidden = true) @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody PauseRequest request) {
        
        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.pause(request));
    }

    @Operation(summary = "Resume a subscription", description = "Resumes a PAUSED subscription, returning it to ACTIVE status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Subscription resumed successfully |\n" +
            "| `100` | Not found, or subscription is not currently paused |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/resume-debit")
    public ResponseEntity<?> resume(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key", required = false) String key,
            @Parameter(hidden = true) @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody ResumeRequest request) {

        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.resume(request));
    }

    @Operation(
        summary = "Manually trigger a debit",
        description = "Fires a one-off debit attempt for a subscription whose automatic retries have been exhausted. " +
            "Blocked if a retry is currently in progress (status PROCESSING or RETRYING)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `03` | Debit triggered and being processed |\n" +
            "| `100` | Not found, or a retry is already in progress |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/trigger-debit")
    public ResponseEntity<?> triggerDebit(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key", required = false) String key,
            @Parameter(hidden = true) @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody TriggerDebitRequest request) {

        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.triggerDebit(request));
    }

    @Operation(
        summary = "Schedule a one-time debit",
        description = "Schedules a single future debit. `debitDate` must be at least 24 hours from now. " +
            "Returns `03` immediately; a transaction callback fires asynchronously."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `03` | Debit scheduled successfully |\n" +
            "| `100` | Subscription not found, date less than 24 h from now, or invalid date format |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/schedule-debit")
    public ResponseEntity<?> scheduleDebit(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key", required = false) String key,
            @Parameter(hidden = true) @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody ScheduleDebitRequest request) {

        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.scheduleDebit(request));
    }

    @Operation(
        summary = "Retrieve subscription by reference",
        description = "Returns the full subscription record looked up by the merchant's own `referenceNo`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Subscription record returned in body |\n" +
            "| `100` | Subscription not found for this reference |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/retrieve/details/thirdpartyreferenceno")
    public ResponseEntity<?> retrieveByReference(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key", required = false) String key,
            @Parameter(hidden = true) @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody RetrieveByReferenceRequest request) {

        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.retrieveByReference(request));
    }

    private boolean isUnauthorized(String transflowId, String key, String country) {
        return transflowId == null || transflowId.isEmpty() ||
               key == null || key.isEmpty() ||
               country == null || country.isEmpty();
    }

    private ResponseEntity<?> buildUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.builder()
                        .responseCode("401")
                        .responseMessage("Unauthorized. Required headers missing.")
                        .build());
    }
}
