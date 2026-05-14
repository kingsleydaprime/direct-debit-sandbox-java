package com.itc.direct_debit_sandbox.scenarios;

import org.springframework.stereotype.Component;

@Component
public class ScenarioEngine {

    public String resolveResponseCode(String debitAccount) {
        return resolveResponseCode(debitAccount, 0);
    }

    /**
     * Resolve a response code for a debit attempt.
     *
     * attemptNumber is zero-based: 0 = initial attempt, 1 = first retry, etc.
     * Suffix 002 — fail on attempt 0, succeed from attempt 1 onward (transient failure).
     * Suffix 003 — fail on attempts 0 and 1, succeed from attempt 2 onward.
     */
    public String resolveResponseCode(String debitAccount, int attemptNumber) {
        var threeNums = debitAccount.substring(debitAccount.length() - 3);
        return switch (threeNums) {
            case "001" -> "01";  // Always success
            case "002" -> attemptNumber >= 1 ? "01" : "101"; // Transient: fail once then succeed
            case "003" -> attemptNumber >= 2 ? "01" : "101"; // Transient: fail twice then succeed
            case "101" -> "101"; // Always insufficient funds
            case "104" -> "104"; // Duplicate transaction
            case "004" -> "04";  // Pre-approval pending
            case "100" -> "100"; // General failure
            case "107" -> "107"; // Invalid credentials
            case "110" -> "110"; // Duplicate transaction (internal)
            case "529" -> "529"; // Insufficient balance
            case "131" -> "131"; // Timeout
            case "527" -> "527"; // Resource not found
            case "515" -> "515"; // Account not found
            case "121" -> "121"; // Not allowed
            case "682" -> "682"; // Internal error
            case "779" -> "779"; // Resource locked
            case "111" -> "111"; // Inconclusive
            default -> "100";    // Default to failure
        };
    }

    public String resolveResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "01"  -> "Transaction processed successfully";
            case "03"  -> "Request is being processed";
            case "04"  -> "Pre-approval pending";
            case "100" -> "Payment failed";
            case "101" -> "Insufficient funds in customer account";
            case "104" -> "Duplicate transaction - a transaction with the same reference already processed";
            case "107" -> "Invalid credentials";
            case "110" -> "Duplicate transaction";
            case "111" -> "Inconclusive - transaction status could not be determined";
            case "121" -> "Not allowed to access this service";
            case "131" -> "Request timed out";
            case "515" -> "Account holder not found";
            case "527" -> "Resource not found";
            case "529" -> "Insufficient balance or maximum transaction limit exceeded";
            case "682" -> "An internal error caused the operation to fail";
            case "779" -> "The required resource is temporarily locked";
            default -> "Payment failed";
        };
    }
}