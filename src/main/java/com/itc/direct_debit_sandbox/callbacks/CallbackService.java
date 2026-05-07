package com.itc.direct_debit_sandbox.callbacks;

import com.itc.direct_debit_sandbox.callbacks.dto.PreapprovalCallbackPayloadDto;
import com.itc.direct_debit_sandbox.callbacks.dto.TransactionCallbackPayloadDto;
import com.itc.direct_debit_sandbox.config.SandboxConfig;
import com.itc.direct_debit_sandbox.scenarios.ScenarioEngine;
import com.itc.direct_debit_sandbox.store.InMemoryStore;
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
    private final InMemoryStore store;

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
                .mandateId(subscription.getMandateId())
                .merchantId(subscription.getMerchantId())
                .productId(subscription.getProductId())
                .debitAccount(subscription.getDebitAccount())
                .reference(subscription.getReferenceNo())
                .channel(subscription.getChannel())
                .country(subscription.getCountry())
                .build();

        sendCallback(subscription.getCallbackUrl(), payload);
        log.info("Preapproval callback fired for mandateId: {}", subscription.getMandateId());
    }

    private void fireTransactionCallback(SubscriptionRecord record) {
        String responseCode = scenarioEngine.resolveResponseCode(record.getDebitAccount());
        String responseMessage = scenarioEngine.resolveResponseMessage(responseCode);
        String transactionId = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        TransactionCallbackPayloadDto payload = TransactionCallbackPayloadDto.builder()
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .debitOrderTransactionId(transactionId)
                .networkTransactionId(UUID.randomUUID().toString())
                .merchantId(record.getMerchantId())
                .productId(record.getProductId())
                .mandateId(record.getMandateId())
                .debitAccount(record.getDebitAccount())
                .debitAmount(record.getDebitAmount())
                .reference(record.getReferenceNo())
                .narration("SANDBOX DEBIT FOR " + record.getMandateId())
                .timestamp(timestamp)
                .channel(record.getChannel())
                .charge("0.00")
                .build();

        // Save transaction to store
        store.saveTransaction(record.getReferenceNo(), TransactionRecord.builder()
                .transactionId(transactionId)
                .networkTransactionId(payload.getNetworkTransactionId())
                .mandateId(record.getMandateId())
                .subscriptionId(record.getSubscriptionId())
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
                .build());

        sendCallback(record.getCallbackUrl(), payload);
        log.info("Transaction callback fired for mandateId: {}, responseCode: {}",
                record.getMandateId(), responseCode);
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