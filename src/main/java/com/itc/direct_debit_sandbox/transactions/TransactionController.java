package com.itc.direct_debit_sandbox.transactions;

import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import com.itc.direct_debit_sandbox.transactions.dto.TransactionStatusRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transactions", description = "Check the outcome of a subscription or scheduled debit")
@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
        summary = "Check transaction status",
        description = "Returns the outcome of a debit by `reference`. " +
            "Possible statuses: PROCESSING, SUCCESS, FAILED, RETRYING, EXHAUSTED."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. `responseCode` reflects the bank outcome:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Transaction succeeded |\n" +
            "| `99` | Transaction record not found |\n" +
            "| `100` | General payment failure |\n" +
            "| `101` | Insufficient funds |\n" +
            "| `104` | Duplicate transaction |\n" +
            "| `107` | Invalid credentials |\n" +
            "| `110` | Duplicate (internal) |\n" +
            "| `111` | Inconclusive — status could not be determined |\n" +
            "| `121` | Not allowed to access this service |\n" +
            "| `131` | Request timed out |\n" +
            "| `515` | Account holder not found |\n" +
            "| `527` | Resource not found |\n" +
            "| `529` | Insufficient balance / max limit exceeded |\n" +
            "| `682` | Internal error |\n" +
            "| `779` | Resource temporarily locked |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — required field missing"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId, x-key, or x-country")
    })
    @PostMapping("/check-status")
    public ResponseEntity<?> checkStatus(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Parameter(hidden = true) @RequestHeader(value = "x-country",     required = false) String country,
            @RequestBody TransactionStatusRequestDto req) {

        if (isUnauthorized(transflowId, apiKey, country)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(transactionService.checkStatus(req));
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
