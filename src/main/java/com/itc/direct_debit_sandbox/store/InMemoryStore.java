package com.itc.direct_debit_sandbox.store;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryStore implements Store {

    // Primary map: subscriptionId → record
    private final Map<String, SubscriptionRecord> subscriptions      = new ConcurrentHashMap<>();
    private final Map<String, TransactionRecord>  transactions       = new ConcurrentHashMap<>();
    // Key = "merchantId:productId"
    private final Map<String, ProvisionRecord>    provisions         = new ConcurrentHashMap<>();
    // Secondary index: transflowId → "merchantId:productId"
    private final Map<String, String>             transflowIdIndex   = new ConcurrentHashMap<>();

    // PreAuth primary map: preApprovalId → record
    private final Map<String, PreAuthRecord> preAuths = new ConcurrentHashMap<>();

    // Secondary indexes for subscription O(1) lookups
    private final Map<String, String>      referenceIndex      = new ConcurrentHashMap<>(); // referenceNo → subscriptionId
    private final Map<String, Set<String>> accountProductIndex = new ConcurrentHashMap<>(); // "debitAccount:productId" → Set<subscriptionId>

    // Secondary indexes for preAuth O(1) lookups
    private final Map<String, String> preAuthReferenceIndex = new ConcurrentHashMap<>(); // referenceNo  → preApprovalId
    private final Map<String, String> preAuthMandateIndex   = new ConcurrentHashMap<>(); // mandateId    → preApprovalId

    // Subscription methods
    public void createSubscription(String subscriptionId, SubscriptionRecord record) {
        subscriptions.put(subscriptionId, record);
        if (record.getReferenceNo() != null) {
            referenceIndex.put(record.getReferenceNo(), subscriptionId);
        }
        accountProductIndex
                .computeIfAbsent(record.getDebitAccount() + ":" + record.getProductId(),
                        k -> ConcurrentHashMap.newKeySet())
                .add(subscriptionId);
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
        Set<String> ids = accountProductIndex.get(debitAccount + ":" + productId);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return ids.stream()
                .map(subscriptions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
            String indexKey = record.getDebitAccount() + ":" + record.getProductId();
            Set<String> ids = accountProductIndex.get(indexKey);
            if (ids != null) {
                ids.remove(subscriptionId);
                if (ids.isEmpty()) accountProductIndex.remove(indexKey);
            }
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

    public List<TransactionRecord> getAllFailedTransactions() {
        return transactions.values().stream()
                .filter(t -> "FAILED".equalsIgnoreCase(t.getStatus()) && t.getRetriesUsed() < t.getMaxRetries())
                .collect(java.util.stream.Collectors.toList());
    }

    // Provision methods
    public void saveProvision(String merchantId, String productId, ProvisionRecord record) {
        String key = merchantId + ":" + productId;
        ProvisionRecord existing = provisions.get(key);
        if (existing != null && existing.getTransflowId() != null) {
            transflowIdIndex.remove(existing.getTransflowId());
        }
        provisions.put(key, record);
        if (record.getTransflowId() != null) {
            transflowIdIndex.put(record.getTransflowId(), key);
        }
    }

    public ProvisionRecord getProvision(String merchantId, String productId) {
        return provisions.get(merchantId + ":" + productId);
    }

    public ProvisionRecord getProvisionByTransflowId(String transflowId) {
        String key = transflowIdIndex.get(transflowId);
        return key != null ? provisions.get(key) : null;
    }

    // PreAuthorization methods

    public void createPreAuth(String preApprovalId, PreAuthRecord record) {
        preAuths.put(preApprovalId, record);
        if (record.getReferenceNo() != null) {
            preAuthReferenceIndex.put(record.getReferenceNo(), preApprovalId);
        }
        if (record.getMandateId() != null) {
            preAuthMandateIndex.put(record.getMandateId(), preApprovalId);
        }
    }

    public PreAuthRecord getPreAuth(String preApprovalId) {
        return preAuths.get(preApprovalId);
    }

    public PreAuthRecord getPreAuthByReference(String referenceNo) {
        String id = preAuthReferenceIndex.get(referenceNo);
        return id != null ? preAuths.get(id) : null;
    }

    public PreAuthRecord getPreAuthByMandateId(String mandateId) {
        String id = preAuthMandateIndex.get(mandateId);
        return id != null ? preAuths.get(id) : null;
    }

    public void updatePreAuthStatus(String preApprovalId, String status) {
        PreAuthRecord record = preAuths.get(preApprovalId);
        if (record != null) {
            record.setStatus(status);
            record.setUpdatedAt(java.time.Instant.now().toString());
        }
    }

    // Updated snapshot to include preauth data
    public Map<String, Object> getSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("subscriptions", subscriptions);
        snapshot.put("transactions", transactions);
        snapshot.put("provisions", provisions);
        snapshot.put("preAuths", preAuths);
        snapshot.put("referenceIndex", referenceIndex);
        snapshot.put("accountProductIndex", accountProductIndex);
        snapshot.put("preAuthReferenceIndex", preAuthReferenceIndex);
        snapshot.put("preAuthMandateIndex", preAuthMandateIndex);
        return snapshot;
    }
}