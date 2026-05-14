package com.itc.direct_debit_sandbox.subscriptions.lifecycle;

import com.itc.direct_debit_sandbox.callbacks.CallbackService;
import com.itc.direct_debit_sandbox.store.Store;
import com.itc.direct_debit_sandbox.store.SubscriptionRecord;
import com.itc.direct_debit_sandbox.store.TransactionRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Polls every 30 seconds for failed subscription transactions that still have
 * retry attempts remaining and fires the next retry attempt.
 *
 * State machine:
 *   FAILED (retriesUsed < maxRetries) → RETRYING → SUCCESS | FAILED (retriesUsed++)
 *   FAILED (retriesUsed == maxRetries) → EXHAUSTED  (terminal, no more retries)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryScheduler {

    private final Store store;
    private final CallbackService callbackService;

    @Scheduled(fixedDelay = 30_000)
    public void processRetries() {
        List<TransactionRecord> eligible = store.getAllFailedTransactions();
        if (eligible.isEmpty()) return;

        log.info("RetryScheduler: found {} transaction(s) eligible for retry", eligible.size());

        for (TransactionRecord tx : eligible) {
            SubscriptionRecord subscription = store.getSubscription(tx.getSubscriptionId());

            // Skip if the subscription no longer exists or is not active
            if (subscription == null || !"ACTIVE".equalsIgnoreCase(subscription.getStatus())) {
                log.info("RetryScheduler: skipping tx {} — subscription inactive or missing", tx.getReference());
                continue;
            }

            // Skip if the subscription has triggerDebitStatus disabled (no auto-retries)
            if (!subscription.isTriggerDebitStatus()) {
                log.info("RetryScheduler: skipping tx {} — triggerDebitStatus is false", tx.getReference());
                continue;
            }

            int nextAttempt = tx.getRetriesUsed() + 1;

            // Mark as RETRYING so manual trigger-debit calls are blocked during this window
            tx.setStatus("RETRYING");
            store.saveTransaction(tx.getReference(), tx);
            log.info("RetryScheduler: firing retry attempt {} for reference {}", nextAttempt, tx.getReference());

            try {
                callbackService.fireTransactionCallback(subscription, nextAttempt);

                // After the callback saves the updated record, check if retries are now exhausted
                TransactionRecord updated = store.getTransaction(tx.getReference());
                if (updated != null
                        && "FAILED".equalsIgnoreCase(updated.getStatus())
                        && updated.getRetriesUsed() >= updated.getMaxRetries()) {
                    updated.setStatus("EXHAUSTED");
                    store.saveTransaction(tx.getReference(), updated);
                    log.info("RetryScheduler: reference {} exhausted all {} retry attempt(s)",
                            tx.getReference(), updated.getMaxRetries());
                }
            } catch (Exception e) {
                log.error("RetryScheduler: retry callback failed for reference {}: {}", tx.getReference(), e.getMessage());
                tx.setStatus("FAILED");
                store.saveTransaction(tx.getReference(), tx);
            }
        }
    }
}
