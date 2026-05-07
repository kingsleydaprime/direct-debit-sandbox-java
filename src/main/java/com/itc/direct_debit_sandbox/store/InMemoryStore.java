package com.itc.direct_debit_sandbox.store;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStore implements Store {

    // ConcurrentHashMap — thread-safe, multiple async callbacks can hit this simultaneously
    private final Map<String, SubscriptionRecord> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, TransactionRecord> transactions = new ConcurrentHashMap<>();

    // Subscription methods
    public void saveSubscription(String subscriptionId, SubscriptionRecord record) {
        subscriptions.put(subscriptionId, record);
    }

    public SubscriptionRecord getSubscription(String subscriptionId) {
        return subscriptions.get(subscriptionId);
    }

    public SubscriptionRecord getSubscriptionByReference(String referenceNo) {
        return subscriptions.values().stream()
                .filter(s -> s.getReferenceNo().equals(referenceNo))
                .findFirst()
                .orElse(null);
    }

    public List<SubscriptionRecord> getSubscriptionsByAccount(String debitAccount, String productId) {
        return subscriptions.values().stream()
                .filter(s -> s.getDebitAccount().equals(debitAccount)
                        && s.getProductId().equals(productId))
                .toList();
    }

    public void updateSubscriptionStatus(String subscriptionId, String status) {
        SubscriptionRecord record = subscriptions.get(subscriptionId);
        if (record != null) {
            record.setStatus(status);
        }
    }

    public void deleteSubscription(String subscriptionId) {
        subscriptions.remove(subscriptionId);
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
}