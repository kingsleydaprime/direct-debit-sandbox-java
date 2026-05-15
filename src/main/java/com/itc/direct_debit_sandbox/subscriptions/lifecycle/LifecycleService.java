package com.itc.direct_debit_sandbox.subscriptions.lifecycle;

import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.PauseRequest;
import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.ResumeRequest;
import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.RetrieveByReferenceRequest;
import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.ScheduleDebitRequest;
import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.TriggerDebitRequest;
import com.itc.direct_debit_sandbox.callbacks.CallbackService;
import com.itc.direct_debit_sandbox.store.Store;
import com.itc.direct_debit_sandbox.store.SubscriptionRecord;
import com.itc.direct_debit_sandbox.store.TransactionRecord;
import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Business logic for the subscription lifecycle slice (pause, resume, retrieve, trigger).
 */
@Service
@RequiredArgsConstructor
public class LifecycleService {

    private final Store store; // InMemoryStore implementation (owned by Person 1)
    private final CallbackService callbackService; // fires async transaction callbacks

    /**
     * Pause a subscription.
     */
    public ApiResponseDto<?> pause(PauseRequest request) {
        SubscriptionRecord record = store.getSubscription(request.getSubscriptionId());
        if (record == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Subscription not found")
                    .build();
        }
        String currentStatus = record.getStatus();
        if ("PAUSED".equalsIgnoreCase(currentStatus)) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Subscription already paused")
                    .build();
        }
        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Subscription is cancelled and cannot be paused")
                    .build();
        }
        store.updateSubscriptionStatus(request.getSubscriptionId(), "PAUSED");
        return ApiResponseDto.builder()
                .responseCode("01")
                .responseMessage("Subscription paused successfully")
                .build();
    }

    /**
     * Resume a paused subscription.
     */
    public ApiResponseDto<?> resume(ResumeRequest request) {
        SubscriptionRecord record = store.getSubscription(request.getSubscriptionId());
        if (record == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Subscription not found")
                    .build();
        }
        String currentStatus = record.getStatus();
        if (!"PAUSED".equalsIgnoreCase(currentStatus)) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Subscription is not paused and cannot be resumed")
                    .build();
        }
        store.updateSubscriptionStatus(request.getSubscriptionId(), "ACTIVE");
        return ApiResponseDto.builder()
                .responseCode("01")
                .responseMessage("Subscription resumed successfully")
                .build();
    }

    /**
     * Retrieve a subscription by its reference number.
     */
    public ApiResponseDto<?> retrieveByReference(RetrieveByReferenceRequest request) {
        SubscriptionRecord record = store.getSubscriptionByReference(request.getReferenceId());
        if (record == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Subscription not found")
                    .build();
        }
        return ApiResponseDto.builder()
                .responseCode("01")
                .responseMessage("Subscription retrieved successfully")
                .data(record)
                .build();
    }

    /**
     * Trigger a debit for a subscription whose automatic retries have been exhausted.
     *
     * This endpoint is only meaningful once all automatic retry attempts have completed
     * without success. If there is still a PROCESSING transaction for this reference it
     * means the system is still running scheduled retries — we block the call and tell
     * the caller to wait. Once all retries have resolved (status FAILED or no record
     * exists yet), the manual trigger is allowed.
     */
    public ApiResponseDto<?> triggerDebit(TriggerDebitRequest request) {
        SubscriptionRecord record = store.getSubscriptionByReference(request.getReferenceId());
        if (record == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Subscription not found")
                    .build();
        }

        // Block if automatic retries are still running (PROCESSING) or a retry attempt
        // is currently executing (RETRYING). The caller must wait for completion.
        TransactionRecord existingTx = store.getTransaction(request.getReferenceId());
        if (existingTx != null && (
                "PROCESSING".equalsIgnoreCase(existingTx.getStatus()) ||
                "RETRYING".equalsIgnoreCase(existingTx.getStatus()))) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("A retry is already in progress for this reference. Please wait for it to complete before triggering manually.")
                    .build();
        }

        callbackService.fireTransactionCallback(record);

        return ApiResponseDto.builder()
                .responseCode("03")
                .responseMessage("Transaction processing started")
                .build();
    }

    /**
     * Schedule a one-time debit.
     */
    public ApiResponseDto<?> scheduleDebit(ScheduleDebitRequest request) {
        // 1. Validate debitDate is at least 24 hours in the future
        try {
            LocalDate debitDate = LocalDate.parse(request.getDebitDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate now = LocalDate.now();
            if (ChronoUnit.DAYS.between(now, debitDate) < 1) {
                return ApiResponseDto.builder()
                        .responseCode("100")
                        .responseMessage("Debit date must be at least 24 hours in the future")
                        .build();
            }
        } catch (Exception e) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Invalid date format. Expected yyyy-MM-dd")
                    .build();
        }

        // 2. Generate transactionId and mandateId
        String mandateId = "MAND" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        // 3. Save TransactionRecord to the store with status PROCESSING
        TransactionRecord record = TransactionRecord.builder()
                .id(mandateId)
                .merchantId(request.getMerchantId())
                .productId(request.getProductId())
                .debitAccount(request.getDebitAccount())
                .debitAmount(request.getDebitAmount())
                .reference(request.getReferenceNo())
                .channel(request.getChannel() != null ? request.getChannel().name() : null)
                .status("PROCESSING")
                .timestamp(LocalDateTime.now().toString())
                .build();

        store.saveTransaction(request.getReferenceNo(), record);

        // 4. Return 03 immediately
        ApiResponseDto<?> immediateResponse = ApiResponseDto.builder()
                .responseCode("03")
                .responseMessage("Transaction scheduled successfully")
                .data(record)
                .build();

        // 5. Fire async transaction callback after delay (Person 3 code integrated)
        callbackService.fireOneTimeTransactionCallback(record, request.getCallbackUrl());

        return immediateResponse;
    }
}
