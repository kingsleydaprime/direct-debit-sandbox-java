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

        // Generate unique IDs for this subscription
        String subscriptionId = "SUB" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String mandateId      = "MAND" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        // Build the record that will live in the in-memory store
        SubscriptionRecord record = SubscriptionRecord.builder()
                .subscriptionId(subscriptionId)
                .mandateId(mandateId)
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
                .channel(req.getChannel())
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

        store.saveSubscription(subscriptionId, record);

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

        // Only overwrite fields that were actually sent in the request (not null).
        // This matches the real API behaviour: "only changed fields need to be provided."
        if (req.getDebitAmount()            != null) existing.setDebitAmount(req.getDebitAmount());
        if (req.getDebitDay()               != null) existing.setDebitDay( req.getDebitDay());
        if (req.getDebitTime()              != null) existing.setDebitTime(req.getDebitTime());
        if (req.getFrequencyType()          != null) existing.setFrequencyType(req.getFrequencyType());
        if (req.getStartDate()              != null) existing.setStartDate(req.getStartDate());
        if (req.getEndDate()                != null) existing.setEndDate(req.getEndDate());
        if (req.getChannel()                != null) existing.setChannel(req.getChannel());
        if (req.getCurrency()               != null) existing.setCurrency(req.getCurrency());
        if (req.getDebitNotificationAccount() != null) existing.setDebitNotificationAccount(req.getDebitNotificationAccount());

        store.saveSubscription(req.getSubscriptionId(), existing);

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