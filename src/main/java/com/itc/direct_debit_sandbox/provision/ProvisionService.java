package com.itc.direct_debit_sandbox.provision;

import com.itc.direct_debit_sandbox.provision.dto.ProvisionRequestDto;
import com.itc.direct_debit_sandbox.store.ProvisionRecord;
import com.itc.direct_debit_sandbox.store.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProvisionService {

    private final Store store;

    public Map<String, Object> provision(String transflowId, String apiKey, ProvisionRequestDto req) {

        if (transflowId == null || transflowId.isBlank() || apiKey == null || apiKey.isBlank()) {
            Map<String, Object> error = new HashMap<>();
            error.put("responseCode", "107");
            error.put("responseMessage", "Invalid credentials");
            return error;
        }

        // Build the record, preserving any existing config values the caller didn't send
        ProvisionRecord existing = store.getProvision(req.getMerchantId(), req.getProductId());

        ProvisionRecord record = ProvisionRecord.builder()
                .merchantId(req.getMerchantId())
                .productId(req.getProductId())
                .callbackUrl(req.getCallbackUrl())
                .apiKey(apiKey)
                .transflowId(transflowId)
                // If caller didn't send a config field, keep the previous value (or null for new registrations)
                .productType(req.getProductType() != null ? req.getProductType()
                        : (existing != null ? existing.getProductType() : null))
                .retryAttempts(req.getRetryAttempts() != null ? req.getRetryAttempts()
                        : (existing != null ? existing.getRetryAttempts() : null))
                .skipFactor(req.getSkipFactor() != null ? req.getSkipFactor()
                        : (existing != null ? existing.getSkipFactor() : null))
                .daysToDebitDayNotice(req.getDaysToDebitDayNotice() != null ? req.getDaysToDebitDayNotice()
                        : (existing != null ? existing.getDaysToDebitDayNotice() : null))
                .registeredAt(Instant.now().toString())
                .build();

        store.saveProvision(req.getMerchantId(), req.getProductId(), record);

        Map<String, Object> response = new HashMap<>();
        response.put("responseCode", "01");
        response.put("responseMessage", "Provision registered successfully");
        response.put("data", record);
        return response;
    }
}
