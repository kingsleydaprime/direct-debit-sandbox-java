package com.itc.direct_debit_sandbox.subscriptions;

import com.itc.direct_debit_sandbox.callbacks.CallbackService;
import com.itc.direct_debit_sandbox.common.CountryDialingCode;
import com.itc.direct_debit_sandbox.provision.ProductType;
import com.itc.direct_debit_sandbox.store.ConfigurationItem;
import com.itc.direct_debit_sandbox.store.InMemoryStore;
import com.itc.direct_debit_sandbox.store.ProvisionRecord;
import com.itc.direct_debit_sandbox.store.SubscriptionRecord;
import com.itc.direct_debit_sandbox.subscriptions.dto.CancelRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.CustomerSubRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.SubscriptionRequestDto;
import com.itc.direct_debit_sandbox.subscriptions.dto.UpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    // ─── PRODUCT TYPE VALIDATION ─────────────────────────────────────────────

    private Map<String, Object> checkSubscriptionProductType(String merchantId, String productId) {
        ProvisionRecord provision = store.getProvision(merchantId, productId);
        if (provision == null || provision.getProductType() == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("responseCode",    "100");
            err.put("responseMessage", "Product type not configured. Provision with productType SUBSCRIPTIONS_ONLY or HYBRID to use subscription endpoints");
            return err;
        }
        if (provision.getProductType() == ProductType.PREAUTHORIZED_ONLY) {
            Map<String, Object> err = new HashMap<>();
            err.put("responseCode",    "100");
            err.put("responseMessage", "PREAUTHORIZED_ONLY products cannot use subscription endpoints. Set productType to SUBSCRIPTIONS_ONLY or HYBRID");
            return err;
        }
        return null;
    }

    // ─── PHONE / COUNTRY VALIDATION ─────────────────────────────────────────

    private Map<String, Object> validatePhoneCountry(String country, String phone, String fieldName) {
        return CountryDialingCode.fromIso(country)
                .flatMap(c -> c.validatePhone(fieldName, phone))
                .map(msg -> {
                    Map<String, Object> err = new HashMap<>();
                    err.put("responseCode",    "100");
                    err.put("responseMessage", msg);
                    return err;
                })
                .orElse(null);
    }

    // ─── CONFIG HELPERS ──────────────────────────────────────────────────────
    // Returns the requested config list merged with any missing product defaults
    // from the ProvisionRecord. Items already present in the request are never overwritten.

    private List<ConfigurationItem> resolveEffectiveConfig(
            List<ConfigurationItem> requested, ProvisionRecord provision) {

        List<ConfigurationItem> resolved = requested != null ? new ArrayList<>(requested) : new ArrayList<>();
        Set<String> present = resolved.stream().map(ConfigurationItem::getName).collect(Collectors.toSet());

        if (provision != null) {
            if (!present.contains("retryAttempts") && provision.getRetryAttempts() != null) {
                ConfigurationItem item = new ConfigurationItem();
                item.setName("retryAttempts");
                item.setValue(provision.getRetryAttempts().toString());
                resolved.add(item);
            }
            if (!present.contains("skipFactor") && provision.getSkipFactor() != null) {
                ConfigurationItem item = new ConfigurationItem();
                item.setName("skipFactor");
                item.setValue(provision.getSkipFactor().toString());
                resolved.add(item);
            }
            if (!present.contains("daysToDebitDayNotice") && provision.getDaysToDebitDayNotice() != null) {
                ConfigurationItem item = new ConfigurationItem();
                item.setName("daysToDebitDayNotice");
                item.setValue(provision.getDaysToDebitDayNotice());
                resolved.add(item);
            }
        }
        return resolved;
    }

    private OptionalInt getConfigIntValue(List<ConfigurationItem> config, String name) {
        if (config == null) return OptionalInt.empty();
        return config.stream()
                .filter(c -> name.equals(c.getName()))
                .mapToInt(c -> {
                    try { return Integer.parseInt(c.getValue()); }
                    catch (NumberFormatException e) { return -1; }
                })
                .findFirst();
    }

    // ─── SUBSCRIBE ───────────────────────────────────────────────────────────

    public Map<String, Object> subscribe(
        String transflowId,
        String apiKey,
        String country,
        SubscriptionRequestDto req) {

        Map<String, Object> authError = validateHeaders(transflowId, apiKey, country);
        if (authError != null) return authError;

        // Guard: product type must allow subscriptions
        Map<String, Object> typeError = checkSubscriptionProductType(req.getMerchantId(), req.getProductId());
        if (typeError != null) return typeError;

        // Guard: debitAccount and debitNotificationAccount must match the x-country dialing prefix
        Map<String, Object> phoneError = validatePhoneCountry(country, req.getDebitAccount(), "debitAccount");
        if (phoneError != null) return phoneError;
        if (req.getDebitNotificationAccount() != null && !req.getDebitNotificationAccount().isBlank()) {
            phoneError = validatePhoneCountry(country, req.getDebitNotificationAccount(), "debitNotificationAccount");
            if (phoneError != null) return phoneError;
        }

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

        if(country.equals("GH") && Channel.VODAFONE == req.getChannel() ) {
            req.setChannel(Channel.TELECEL);
            log.info("Channel changed from VODAFONE to TELECEL");
        }



        // Guard: debitDay must be valid for the given frequencyType
        Map<String, Object> debitDayError = validateDebitDay(req.getFrequencyType(), req.getDebitDay());
        if (debitDayError != null) return debitDayError;

        // Guard: DAILY subscriptions cannot have notificationStatus enabled
        if (FrequencyType.DAILY == req.getFrequencyType() && Boolean.TRUE.equals(req.getNotificationStatus())) {
            Map<String, Object> error = new HashMap<>();
            error.put("responseCode", "100");
            error.put("responseMessage", "notificationStatus cannot be enabled for DAILY subscriptions");
            return error;
        }

        // Resolve effective configuration: merge request config with product defaults
        ProvisionRecord provision = store.getProvision(req.getMerchantId(), req.getProductId());
        List<ConfigurationItem> effectiveConfig = resolveEffectiveConfig(req.getConfiguration(), provision);

        // Guard: DAILY subscriptions cannot have retryAttempts > 1
        if (FrequencyType.DAILY == req.getFrequencyType()) {
            OptionalInt retryAttempts = getConfigIntValue(effectiveConfig, "retryAttempts");
            if (retryAttempts.isPresent() && retryAttempts.getAsInt() > 1) {
                Map<String, Object> error = new HashMap<>();
                error.put("responseCode", "100");
                error.put("responseMessage", "retryAttempts cannot exceed 1 for DAILY subscriptions");
                return error;
            }
        }

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
                .configuration(effectiveConfig)
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

        // callbackService.fireCallbacks(existing);

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

        List<Map<String, Object>> sanitizedResults = results.stream()
                .map(this::toSubscriptionResponse)
                .collect(Collectors.toList());

        response.put("responseCode",    "01");
        response.put("responseMessage", "operation successful");
        response.put("data",            sanitizedResults);
        return response;
    }

    private Map<String, Object> toSubscriptionResponse(SubscriptionRecord record) {
        Map<String, Object> subscription = new LinkedHashMap<>();
        subscription.put("merchantId",               record.getMerchantId());
        subscription.put("productId",                record.getProductId());
        subscription.put("subscriptionId",           record.getId());
        subscription.put("debitAccount",             record.getDebitAccount());
        subscription.put("country",                  record.getCountry());
        subscription.put("debitAmount",              record.getDebitAmount());
        subscription.put("frequencyType",            record.getFrequencyType());
        subscription.put("startDate",                record.getStartDate());
        subscription.put("endDate",                  record.getEndDate());
        subscription.put("debitDay",                 record.getDebitDay());
        subscription.put("referenceNo",              record.getReferenceNo());
        subscription.put("channel",                  record.getChannel());
        subscription.put("debitTime",                record.getDebitTime());
        subscription.put("debitNotificationAccount", record.getDebitNotificationAccount());
        subscription.put("currency",                 record.getCurrency());
        subscription.put("created",                  record.getCreatedAt());
        return subscription;
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
