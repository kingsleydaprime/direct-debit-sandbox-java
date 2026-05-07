package com.itc.direct_debit_sandbox.scenarios;

import org.springframework.stereotype.Component;

@Component
public class ScenarioEngine {

    public String resolveResponseCode(String debitAccount) {
        var threeNums = debitAccount.substring(debitAccount.length() - 3);
        return switch (threeNums) {
            case "001" -> "01";  // Success
            case "101" -> "101"; // Insufficient funds (API spec code)
            case "104" -> "104"; // Duplicate transaction (API spec code)
            case "004" -> "04";  // Pre-approval pending (account ending 004 triggers this)
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