package com.itc.direct_debit_sandbox.subscriptions.lifecycle;

import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing the subscription lifecycle (pause, resume, trigger, schedule, retrieve).
 */
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class LifecycleController {

    private final LifecycleService lifecycleService;

    @PostMapping("/pause")
    public ResponseEntity<?> pause(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key", required = false) String key,
            @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody PauseRequest request) {
        
        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.pause(request));
    }

    @PostMapping("/resume-debit")
    public ResponseEntity<?> resume(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key", required = false) String key,
            @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody ResumeRequest request) {

        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.resume(request));
    }

    @PostMapping("/trigger-debit")
    public ResponseEntity<?> triggerDebit(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key", required = false) String key,
            @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody TriggerDebitRequest request) {

        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.triggerDebit(request));
    }

    @PostMapping("/schedule-debit")
    public ResponseEntity<?> scheduleDebit(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key", required = false) String key,
            @RequestHeader(value = "x-country", required = false) String country,
            @RequestBody ScheduleDebitRequest request) {

        if (isUnauthorized(transflowId, key, country)) {
            return buildUnauthorizedResponse();
        }
        return ResponseEntity.ok(lifecycleService.scheduleDebit(request));
    }

    @PostMapping("/retrieve/details/thirdpartyreferenceno")
    public ResponseEntity<?> retrieveByReference(
            @RequestHeader(value = "x-transflowId", required = false) String transflowId,
            @RequestHeader(value = "x-key", required = false) String key,
            @RequestHeader(value = "x-country", required = false) String country,
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
