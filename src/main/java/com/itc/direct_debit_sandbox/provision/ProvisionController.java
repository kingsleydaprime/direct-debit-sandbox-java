package com.itc.direct_debit_sandbox.provision;

import com.itc.direct_debit_sandbox.provision.dto.ProvisionRequestDto;
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

@Tag(name = "Provision", description = "Register merchant/product configuration — callback URL, product type, and retry defaults")
@RestController
@RequestMapping("/provision")
@RequiredArgsConstructor
public class ProvisionController {

    private final ProvisionService provisionService;

    @Operation(
        summary = "Register or update product configuration",
        description = "Stores the callback URL, `productType`, and default configuration values " +
            "(`retryAttempts`, `skipFactor`, `daysToDebitDayNotice`) for a `merchantId + productId` combination. " +
            "Call this once at onboarding. All subsequent callbacks for this product will be sent to the registered URL."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description =
            "Always returned. Check `responseCode` in the body:\n\n" +
            "| Code | Meaning |\n" +
            "|------|---------|\n" +
            "| `01` | Product configuration saved successfully |\n"),
        @ApiResponse(responseCode = "400", description = "Jakarta validation failure — `merchantId`, `productId`, or `callbackUrl` missing/invalid"),
        @ApiResponse(responseCode = "401", description = "Missing required headers — x-transflowId or x-key")
    })
    @PostMapping
    public ResponseEntity<?> provision(
            @Parameter(hidden = true) @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @Parameter(hidden = true) @RequestHeader(value = "x-key",         required = false) String apiKey,
            @Valid @RequestBody ProvisionRequestDto req) {

        if (isUnauthorized(transflowId, apiKey)) return buildUnauthorizedResponse();
        return ResponseEntity.ok(provisionService.provision(transflowId, apiKey, req));
    }

    private boolean isUnauthorized(String transflowId, String key) {
        return transflowId == null || transflowId.isBlank() ||
               key        == null || key.isBlank();
    }

    private ResponseEntity<?> buildUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.builder()
                        .responseCode("401")
                        .responseMessage("Unauthorized. Required headers missing: x-transflowId, x-key")
                        .build());
    }
}
