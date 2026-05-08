package com.itc.direct_debit_sandbox.store;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStore implements Store {

    // Primary map: subscriptionId → record
    private final Map<String, SubscriptionRecord> subscriptions      = new ConcurrentHashMap<>();
    private final Map<String, TransactionRecord>  transactions       = new ConcurrentHashMap<>();
    // Key = "merchantId:productId"
    private final Map<String, ProvisionRecord>    provisions         = new ConcurrentHashMap<>();

    // Secondary indexes for O(1) lookups
    private final Map<String, String> referenceIndex      = new ConcurrentHashMap<>(); // referenceNo → subscriptionId
    private final Map<String, String> accountProductIndex = new ConcurrentHashMap<>(); // "debitAccount:productId" → subscriptionId

    // Subscription methods
    public void createSubscription(String subscriptionId, SubscriptionRecord record) {
        subscriptions.put(subscriptionId, record);
        if (record.getReferenceNo() != null) {
            referenceIndex.put(record.getReferenceNo(), subscriptionId);
        }
        accountProductIndex.put(record.getDebitAccount() + ":" + record.getProductId(), subscriptionId);
    }

    public void updateSubscription(String subscriptionId, SubscriptionRecord record) {
        subscriptions.put(subscriptionId, record);
    }

    public SubscriptionRecord getSubscription(String subscriptionId) {
        return subscriptions.get(subscriptionId);
    }

    public SubscriptionRecord getSubscriptionByReference(String referenceNo) {
        String id = referenceIndex.get(referenceNo);
        return id != null ? subscriptions.get(id) : null;
    }

    public List<SubscriptionRecord> getSubscriptionsByAccount(String debitAccount, String productId) {
        String id = accountProductIndex.get(debitAccount + ":" + productId);
        if (id == null) return Collections.emptyList();
        SubscriptionRecord record = subscriptions.get(id);
        return record != null ? List.of(record) : Collections.emptyList();
    }

    public void updateSubscriptionStatus(String subscriptionId, String status) {
        SubscriptionRecord record = subscriptions.get(subscriptionId);
        if (record != null) {
            record.setStatus(status);
        }
    }

    public void deleteSubscription(String subscriptionId) {
        SubscriptionRecord record = subscriptions.remove(subscriptionId);
        if (record != null) {
            if (record.getReferenceNo() != null) {
                referenceIndex.remove(record.getReferenceNo());
            }
            accountProductIndex.remove(record.getDebitAccount() + ":" + record.getProductId());
        }
    }

    // Transaction methods
    public void saveTransaction(String reference, TransactionRecord record) {
        transactions.put(reference, record);
    }

    public TransactionRecord getTransaction(String reference) {
        return transactions.get(reference);
    }

    public boolean transactionExists(String reference) {
        return transactions.containsKey(reference);
    }

    // Debug
    public Map<String, Object> getSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("subscriptions", subscriptions);
        snapshot.put("transactions", transactions);
        snapshot.put("provisions", provisions);
        snapshot.put("referenceIndex", referenceIndex);
        snapshot.put("accountProductIndex", accountProductIndex);
        return snapshot;
    }

    // Provision methods
    public void saveProvision(String merchantId, String productId, ProvisionRecord record) {
        provisions.put(merchantId + ":" + productId, record);
    }

    public ProvisionRecord getProvision(String merchantId, String productId) {
        return provisions.get(merchantId + ":" + productId);
    }
}