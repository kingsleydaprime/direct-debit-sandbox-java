package com.itc.direct_debit_sandbox.callbacks;

import com.itc.direct_debit_sandbox.callbacks.dto.PreapprovalCallbackPayloadDto;
import com.itc.direct_debit_sandbox.callbacks.dto.TransactionCallbackPayloadDto;
import com.itc.direct_debit_sandbox.config.SandboxConfig;
import com.itc.direct_debit_sandbox.preauthorization.dto.TriggerMandateDebitRequest;
import com.itc.direct_debit_sandbox.scenarios.ScenarioEngine;
import com.itc.direct_debit_sandbox.store.ConfigurationItem;
import com.itc.direct_debit_sandbox.store.PreAuthRecord;
import com.itc.direct_debit_sandbox.store.ProvisionRecord;
import com.itc.direct_debit_sandbox.store.Store;
import com.itc.direct_debit_sandbox.store.SubscriptionRecord;
import com.itc.direct_debit_sandbox.store.TransactionRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final RestTemplate restTemplate;
    private final SandboxConfig sandboxConfig;
    private final ScenarioEngine scenarioEngine;
    private final Store store;

    @Async("callbackExecutor")
    public void fireCallbacks(SubscriptionRecord record) {
        try {
            // Step 1 — fire preapproval callback after delay
            Thread.sleep(sandboxConfig.getCallbackDelayPreapproval());
            firePreapprovalCallback(record);

            // Step 2 — fire transaction callback after delay
            Thread.sleep(sandboxConfig.getCallbackDelayTransaction());
            fireTransactionCallback(record);
        } catch (InterruptedException e) {
            log.error("Callback thread interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void firePreapprovalCallback(SubscriptionRecord subscription) {
        PreapprovalCallbackPayloadDto payload = PreapprovalCallbackPayloadDto.builder()
                .responseCode("01")
                .responseMessage("Recurring subscription has been scheduled successfully")
                .mandateId(subscription.getId())
                .merchantId(subscription.getMerchantId())
                .productId(subscription.getProductId())
                .debitAccount(subscription.getDebitAccount())
                .reference(subscription.getReferenceNo())
                .channel(subscription.getChannel())
                .country(subscription.getCountry())
                .build();

        String url = resolveCallbackUrl(subscription.getMerchantId(), subscription.getProductId(), subscription.getCallbackUrl());
        sendCallback(url, payload);
        log.info("Preapproval callback fired for mandateId: {}", subscription.getId());
    }

    @Async("callbackExecutor")
    public void fireTransactionCallback(SubscriptionRecord record) {
        fireTransactionCallback(record, 0);
    }

    public void fireTransactionCallback(SubscriptionRecord record, int attemptNumber) {
        String responseCode    = scenarioEngine.resolveResponseCode(record.getDebitAccount(), attemptNumber);
        String responseMessage = scenarioEngine.resolveResponseMessage(responseCode);
        String transactionId   = UUID.randomUUID().toString();
        String timestamp       = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int maxRetries         = resolveMaxRetries(record);

        TransactionCallbackPayloadDto payload = TransactionCallbackPayloadDto.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .debitOrderTransactionId(transactionId)
                .networkTransactionId(UUID.randomUUID().toString())
                .merchantId(record.getMerchantId())
                .productId(record.getProductId())
                .mandateId(record.getId())
                .debitAccount(record.getDebitAccount())
                .debitAmount(record.getDebitAmount())
                .reference(record.getReferenceNo())
                .narration("SANDBOX DEBIT FOR " + record.getId())
                .timestamp(timestamp)
                .channel(record.getChannel())
                .charge("0.00")
                .build();

        store.saveTransaction(record.getReferenceNo(), TransactionRecord.builder()
                .id(transactionId)
                .networkTransactionId(payload.getNetworkTransactionId())
                .subscriptionId(record.getId())
                .merchantId(record.getMerchantId())
                .productId(record.getProductId())
                .debitAccount(record.getDebitAccount())
                .debitAmount(record.getDebitAmount())
                .reference(record.getReferenceNo())
                .narration(payload.getNarration())
                .channel(record.getChannel())
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .timestamp(timestamp)
                .charge("0.00")
                .status(responseCode.equals("01") ? "SUCCESS" : "FAILED")
                .retriesUsed(attemptNumber)
                .maxRetries(record.isTriggerDebitStatus() ? maxRetries : 0)
                .build());

        String url = resolveCallbackUrl(record.getMerchantId(), record.getProductId(), record.getCallbackUrl());
        sendCallback(url, payload);
        log.info("Transaction callback fired for mandateId: {}, attempt: {}, responseCode: {}",
                record.getId(), attemptNumber, responseCode);
    }

    private int resolveMaxRetries(SubscriptionRecord record) {
        if (record.getConfiguration() == null) return 1;
        return record.getConfiguration().stream()
                .filter(c -> "retryAttempts".equals(c.getName()))
                .mapToInt(c -> {
                    try { return Integer.parseInt(c.getValue()); }
                    catch (NumberFormatException e) { return 1; }
                })
                .findFirst()
                .orElse(1);
    }

    @Async("callbackExecutor")
    public void fireOneTimeTransactionCallback(TransactionRecord record, String callbackUrl) {
        String responseCode = scenarioEngine.resolveResponseCode(record.getDebitAccount());
        String responseMessage = scenarioEngine.resolveResponseMessage(responseCode);
        
        record.setResponseCode(responseCode);
        record.setResponseMessage(responseMessage);
        record.setStatus(responseCode.equals("01") ? "SUCCESS" : "FAILED");
        
        // Update record in store (assuming saveTransaction overwrites)
        store.saveTransaction(record.getReference(), record);

        TransactionCallbackPayloadDto payload = TransactionCallbackPayloadDto.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .debitOrderTransactionId(record.getId())
                .networkTransactionId(record.getNetworkTransactionId())
                .merchantId(record.getMerchantId())
                .productId(record.getProductId())
                .mandateId(record.getSubscriptionId())
                .debitAccount(record.getDebitAccount())
                .debitAmount(record.getDebitAmount())
                .reference(record.getReference())
                .narration(record.getNarration())
                .timestamp(record.getTimestamp())
                .channel(record.getChannel())
                .charge(record.getCharge() != null ? record.getCharge() : "0.00")
                .build();

        String url = resolveCallbackUrl(record.getMerchantId(), record.getProductId(), callbackUrl);
        sendCallback(url, payload);
        log.info("One-time Transaction callback fired for reference: {}, responseCode: {}",
                record.getReference(), responseCode);
    }

    // ─── PREAUTH CALLBACKS ───────────────────────────────────────────────────

    /**
     * Fires a preapproval callback for a newly created preauthorization mandate.
     * Preauth creation does NOT fire an immediate transaction callback — the debit
     * only happens when the merchant later calls /mandate/trigger-debit.
     */
    @Async("callbackExecutor")
    public void firePreAuthCallbacks(PreAuthRecord record) {
        try {
            Thread.sleep(sandboxConfig.getCallbackDelayPreapproval());

            PreapprovalCallbackPayloadDto payload = PreapprovalCallbackPayloadDto.builder()
                    .responseCode("01")
                    .responseMessage("Recurring subscription has been scheduled successfully")
                    .mandateId(record.getMandateId())
                    .merchantId(record.getMerchantId())
                    .productId(record.getProductId())
                    .debitAccount(record.getDebitAccount())
                    .reference(record.getReferenceNo())
                    .channel(record.getChannel())
                    .country(record.getCountryId())
                    .build();

            String url = resolveCallbackUrl(record.getMerchantId(), record.getProductId(), record.getCallbackUrl());
            sendCallback(url, payload);
            log.info("PreAuth preapproval callback fired for mandateId: {}", record.getMandateId());
        } catch (InterruptedException e) {
            log.error("PreAuth callback thread interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Fires a transaction callback for a mandate debit triggered via /mandate/trigger-debit.
     * The debit details come from the trigger request (amount, account, narration, reference)
     * not from the preauth record itself, since each trigger can have different parameters.
     */
    @Async("callbackExecutor")
    public void fireMandateTransactionCallback(PreAuthRecord preAuth, TriggerMandateDebitRequest req) {
        try {
            Thread.sleep(sandboxConfig.getCallbackDelayTransaction());

            String responseCode    = scenarioEngine.resolveResponseCode(req.getDebitAccount());
            String responseMessage = scenarioEngine.resolveResponseMessage(responseCode);
            String transactionId   = UUID.randomUUID().toString();
            String timestamp       = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            TransactionCallbackPayloadDto payload = TransactionCallbackPayloadDto.builder()
                    .responseCode(responseCode)
                    .responseMessage(responseMessage)
                    .debitOrderTransactionId(transactionId)
                    .networkTransactionId(UUID.randomUUID().toString())
                    .merchantId(preAuth.getMerchantId())
                    .productId(preAuth.getProductId())
                    .mandateId(preAuth.getMandateId())
                    .debitAccount(req.getDebitAccount())
                    .debitAmount(req.getDebitAmount())
                    .reference(req.getReferenceNo())
                    .narration(req.getNarration())
                    .timestamp(timestamp)
                    .channel(preAuth.getChannel())
                    .charge("0.00")
                    .build();

            // Persist the transaction so check-status works
            store.saveTransaction(req.getReferenceNo(), TransactionRecord.builder()
                    .id(transactionId)
                    .networkTransactionId(payload.getNetworkTransactionId())
                    .subscriptionId(preAuth.getPreApprovalId())
                    .merchantId(preAuth.getMerchantId())
                    .productId(preAuth.getProductId())
                    .debitAccount(req.getDebitAccount())
                    .debitAmount(req.getDebitAmount())
                    .reference(req.getReferenceNo())
                    .narration(req.getNarration())
                    .channel(preAuth.getChannel())
                    .responseCode(responseCode)
                    .responseMessage(responseMessage)
                    .timestamp(timestamp)
                    .charge("0.00")
                    .status(responseCode.equals("01") ? "SUCCESS" : "FAILED")
                    .build());

            String url = resolveCallbackUrl(preAuth.getMerchantId(), preAuth.getProductId(), preAuth.getCallbackUrl());
            sendCallback(url, payload);
            log.info("Mandate transaction callback fired for mandateId: {}, responseCode: {}",
                    preAuth.getMandateId(), responseCode);
        } catch (InterruptedException e) {
            log.error("Mandate transaction callback thread interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    // Look up the registered callbackUrl for a merchant+product from the provision store.
    // Falls back to the supplied fallback value if no provision has been registered yet.
    private String resolveCallbackUrl(String merchantId, String productId, String fallback) {
        ProvisionRecord provision = store.getProvision(merchantId, productId);
        if (provision != null && provision.getCallbackUrl() != null) {
            return provision.getCallbackUrl();
        }
        return fallback;
    }

    private void sendCallback(String callbackUrl, Object payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    callbackUrl, HttpMethod.POST, entity, String.class
            );
            log.info("Callback delivered to {} — status: {}", callbackUrl, response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to deliver callback to {}: {}", callbackUrl, e.getMessage());
        }
    }
}