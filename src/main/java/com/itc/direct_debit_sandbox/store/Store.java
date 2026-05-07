package com.itc.direct_debit_sandbox.store;

import java.util.List;

/**
 * Shared interface for subscription and transaction data storage.
 * Defined together by the team to minimize conflicts.
 */
public interface Store {
    // Subscription methods
    void saveSubscription(String subscriptionId, SubscriptionRecord record);
    SubscriptionRecord getSubscription(String subscriptionId);
    SubscriptionRecord getSubscriptionByReference(String referenceNo);
    List<SubscriptionRecord> getSubscriptionsByAccount(String debitAccount, String productId);
    void updateSubscriptionStatus(String subscriptionId, String status);
    void deleteSubscription(String subscriptionId);

    // Transaction methods
    void saveTransaction(String reference, TransactionRecord record);
    TransactionRecord getTransaction(String reference);
    boolean transactionExists(String reference);
}
