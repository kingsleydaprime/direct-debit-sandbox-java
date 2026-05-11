package com.itc.direct_debit_sandbox.subscriptions;

import com.itc.direct_debit_sandbox.callbacks.CallbackService;
import com.itc.direct_debit_sandbox.store.InMemoryStore;
import com.itc.direct_debit_sandbox.store.SubscriptionRecord;
import com.itc.direct_debit_sandbox.subscriptions.dto.CancelRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.CustomerSubRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.SubscriptionRequestDto;
import com.itc.direct_debit_sandbox.subscriptions.dto.UpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final InMemoryStore store;
    private final CallbackService callbackService;

    // ─── HEADER VALIDATION ───────────────────────────────────────────────────
    // Called at the start of every method. Returns an error map if headers
    // are missing, or null if everything is fine.

    private Map<String, Object> validateHeaders(String transflowId, String apiKey, String country) {
        if (transflowId == null || transflowId.isBlank() ||
                apiKey      == null || apiKey.isBlank()      ||
                country     == null || country.isBlank()) {

            Map<String, Object> error = new HashMap<>();
            error.put("responseCode",    "107");
            error.put("responseMessage", "Invalid Credentials");
            return error;
        }
        return null; // null means headers are valid, continue processing
    }

    // ─── DEBIT DAY VALIDATION ────────────────────────────────────────────────
    // DAILY → must be "1"
    // WEEKLY → 1–7
    // MONTHLY / YEARLY → 1–28
    // Returns an error map if invalid, null if OK.

    private Map<String, Object> validateDebitDay(FrequencyType frequency, String debitDay) {
        if (frequency == null || debitDay == null || debitDay.isBlank()) return null;
        try {
            int day = Integer.parseInt(debitDay.trim());
            boolean valid = switch (frequency) {
                case DAILY   -> day == 1;
                case WEEKLY  -> day >= 1 && day <= 7;
                case MONTHLY, YEARLY -> day >= 1 && day <= 28;
            };
            if (!valid) {
                String allowed = switch (frequency) {
                    case DAILY   -> "must be 1 for DAILY frequency";
                    case WEEKLY  -> "must be between 1 and 7 for WEEKLY frequency";
                    case MONTHLY, YEARLY -> "must be between 1 and 28 for " + frequency + " frequency";
                };
                Map<String, Object> error = new HashMap<>();
                error.put("responseCode",    "100");
                error.put("responseMessage", "Invalid debitDay: " + allowed);
                return error;
            }
        } catch (NumberFormatException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("responseCode",    "100");
            error.put("responseMessage", "debitDay must be a numeric value");
            return error;
        }
        return null;
    }

    // ─── SUBSCRIBE ───────────────────────────────────────────────────────────

    public Map<String, Object> subscribe(
        String transflowId,
        String apiKey,
        String country,
        SubscriptionRequestDto req) {

        Map<String, Object> authError = validateHeaders(transflowId, apiKey, country);
        if (authError != null) return authError;

        // Guard against duplicate references — same referenceNo already exists
        if (req.getReferenceNo() != null && store.getSubscriptionByReference(req.getReferenceNo()) != null) {
            Map<String, Object> duplicate = new HashMap<>();
            duplicate.put("responseCode", "104");
            duplicate.put("responseMessage", "Duplicate transaction - a subscription with this reference already exists");
            return duplicate;
        }

        // Guard: endDate must be strictly after startDate when both are provided
        if (req.getEndDate() != null && !req.getEndDate().isBlank()) {
            try {
                LocalDate start = LocalDate.parse(req.getStartDate());
                LocalDate end   = LocalDate.parse(req.getEndDate());
                if (!end.isAfter(start)) {
                    Map<String, Object> dateError = new HashMap<>();
                    dateError.put("responseCode",    "100");
                    dateError.put("responseMessage", "endDate must be after startDate");
                    return dateError;
                }
            } catch (Exception e) {
                Map<String, Object> dateError = new HashMap<>();
                dateError.put("responseCode",    "100");
                dateError.put("responseMessage", "Invalid date format. Expected yyyy-MM-dd");
                return dateError;
            }
        }

        // Guard: debitDay must be valid for the given frequencyType
        Map<String, Object> debitDayError = validateDebitDay(req.getFrequencyType(), req.getDebitDay());
        if (debitDayError != null) return debitDayError;

        // Generate unique IDs for this subscription
        String mandateId = UUID.randomUUID().toString();

        // Build the record that will live in the in-memory store
        SubscriptionRecord record = SubscriptionRecord.builder()
                .id(mandateId)
                .merchantId(req.getMerchantId())
                .productId(req.getProductId())
                .debitAccount(req.getDebitAccount())
                .debitAmount(req.getDebitAmount())
                .frequencyType(req.getFrequencyType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .debitDay(req.getDebitDay())
                .debitTime(req.getDebitTime())
                .referenceNo(req.getReferenceNo())
                .channel(req.getChannel().name())
                .currency(req.getCurrency())
                .country(country)
                .debitNotificationAccount(req.getDebitNotificationAccount())
                .status("ACTIVE")
                // Boolean fields: treat null as false
                .triggerDebitStatus(Boolean.TRUE.equals(req.getTriggerDebitStatus()))
                .notificationStatus(Boolean.TRUE.equals(req.getNotificationStatus()))
                .configuration(req.getConfiguration())
                .createdAt(Instant.now().toString())
                .build();

        store.createSubscription(mandateId, record);

        // Fire preapproval callback then transaction callback asynchronously
        callbackService.fireCallbacks(record);

        Map<String, Object> response = new HashMap<>();
        response.put("responseCode",    "03");
        response.put("responseMessage", "your request is being processed");
        return response;
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    public Map<String, Object> update(String transflowId, String apiKey,
                                      String country, UpdateRequest req) {

        Map<String, Object> authError = validateHeaders(transflowId, apiKey, country);
        if (authError != null) return authError;

        SubscriptionRecord existing = store.getSubscription(req.getSubscriptionId());

        if (existing == null) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("responseCode",    "100");
            notFound.put("responseMessage", "Subscription not found");
            return notFound;
        }

        // Validate debitDay against the effective frequency after the update
        FrequencyType effectiveFrequency = req.getFrequencyType() != null ? req.getFrequencyType()
                : existing.getFrequencyType();
        String effectiveDebitDay = req.getDebitDay() != null ? req.getDebitDay() : existing.getDebitDay();
        Map<String, Object> debitDayError = validateDebitDay(effectiveFrequency, effectiveDebitDay);
        if (debitDayError != null) return debitDayError;

        // Only overwrite fields that were actually sent in the request (not null).
        // This matches the real API behaviour: "only changed fields need to be provided."
        if (req.getDebitAmount()            != null) existing.setDebitAmount(req.getDebitAmount());
        if (req.getDebitDay()               != null) existing.setDebitDay( req.getDebitDay());
        if (req.getDebitTime()              != null) existing.setDebitTime(req.getDebitTime());
        if (req.getFrequencyType()          != null) existing.setFrequencyType(req.getFrequencyType());
        if (req.getStartDate()              != null) existing.setStartDate(req.getStartDate());
        if (req.getEndDate()                != null) existing.setEndDate(req.getEndDate());
        if (req.getChannel()                != null) existing.setChannel(req.getChannel().name());
        if (req.getCurrency()               != null) existing.setCurrency(req.getCurrency());
        if (req.getDebitNotificationAccount() != null) existing.setDebitNotificationAccount(req.getDebitNotificationAccount());

        store.updateSubscription(req.getSubscriptionId(), existing);

        callbackService.fireCallbacks(existing);

        Map<String, Object> response = new HashMap<>();
        response.put("responseCode",    "03");
        response.put("responseMessage", "your request is being processed");
        return response;
    }

    // ─── CANCEL ──────────────────────────────────────────────────────────────

    public Map<String, Object> cancel(String transflowId, String apiKey,
                                      String country, CancelRequest req) {

        Map<String, Object> authError = validateHeaders(transflowId, apiKey, country);
        if (authError != null) return authError;

        SubscriptionRecord existing = store.getSubscription(req.getSubscriptionId());

        if (existing == null) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("responseCode",    "100");
            notFound.put("responseMessage", "Subscription not found");
            return notFound;
        }

        store.deleteSubscription(req.getSubscriptionId());

        Map<String, Object> response = new HashMap<>();
        response.put("responseCode",    "01");
        response.put("responseMessage", "your request has been processed");
        return response;
    }

    // ─── GET CUSTOMER SUBSCRIPTIONS ──────────────────────────────────────────

    public Map<String, Object> getCustomerSubscriptions(String transflowId, String apiKey,
                                                        String country, CustomerSubRequest req) {

        Map<String, Object> authError = validateHeaders(transflowId, apiKey, country);
        if (authError != null) return authError;

        List<SubscriptionRecord> results = store.getSubscriptionsByAccount(req.getDebitAccount(), req.getProductId());

        Map<String, Object> response = new HashMap<>();

        if (results.isEmpty()) {
            response.put("responseCode",    "100");
            response.put("responseMessage", "No subscriptions found");
            return response;
        }

        response.put("responseCode",    "01");
        response.put("responseMessage", "operation successful");
        response.put("data",            results);
        return response;
    }

    // Delegates to the correctly-named method above.
    // The controller calls this name (typo preserved so we don't break the call site).
    public Map<String, Object> getCustomerSuscriptions(String transflowId, String apiKey, String country, SubscriptionRequestDto req) {
        CustomerSubRequest subReq = new CustomerSubRequest();
        subReq.setDebitAccount(req.getDebitAccount());
        subReq.setProductId(req.getProductId());
        return getCustomerSubscriptions(transflowId, apiKey, country, subReq);
    }
}