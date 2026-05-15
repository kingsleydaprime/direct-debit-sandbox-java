package com.itc.direct_debit_sandbox.subscriptions;

import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import com.itc.direct_debit_sandbox.subscriptions.dto.CancelRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.CustomerSubRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.SubscriptionRequestDto;
import com.itc.direct_debit_sandbox.subscriptions.dto.UpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscriptions", description = "Create and manage recurring debit subscriptions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
        summary = "Create a subscription",
        description = "Sets up a new recurring debit mandate. Returns `03` immediately. " +
            "A preapproval callback fires after ~2 s, followed by a transaction callback after ~5 s."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `03` | Request accepted and being processed |\n" +
            "| `100` | Business error — invalid debitDay, duplicate reference, DAILY constraint violated, date error |\n" +
            "| `107` | Invalid credentials |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing or `@NotBlank` violated"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody SubscriptionRequestDto req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(subscriptionService.subscribe(transflowId, apiKey, country, req));
    }

    @Operation(
        summary = "Update a subscription",
        description = "Partially updates an existing subscription. Only fields present in the request body are changed."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `03` | Update accepted and being processed |\n" +
            "| `100` | Subscription not found |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/update")
    public ResponseEntity<?> update(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody UpdateRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(subscriptionService.update(transflowId, apiKey, country, req));
    }

    @Operation(
        summary = "Cancel a subscription",
        description = "Permanently cancels a subscription. A cancelled subscription cannot be resumed."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Subscription cancelled successfully |\n" +
            "| `100` | Subscription not found |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @Valid @RequestBody CancelRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(subscriptionService.cancel(transflowId, apiKey, country, req));
    }

    @Operation(
        summary = "Get customer subscriptions",
        description = "Returns all subscriptions associated with a given `debitAccount` and `productId`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | List returned in `subscriptions` array |\n" +
            "| `100` | No subscriptions found for this account |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/customer-subscriptions")
    public ResponseEntity<?> getCustomerSubscriptions(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @RequestBody CustomerSubRequest req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(subscriptionService.getCustomerSubscriptions(transflowId, apiKey, country, req));
    }

    private boolean isUnauthorized(String transflowId, String key, String country) {
        return transflowId == null || transflowId.isBlank() ||
               key        == null || key.isBlank()         ||
               country    == null || country.isBlank();
    }

    private ResponseEntity<?> buildUnauthorizedResponse() {
        return ResponseEntity.ok(ApiResponseDto.builder()
                .responseCode("107")
                .responseMessage("Invalid credentials: missing or blank required headers")
                .build());
    }
}
