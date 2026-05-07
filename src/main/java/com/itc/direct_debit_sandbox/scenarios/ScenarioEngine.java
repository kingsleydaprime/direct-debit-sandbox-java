package com.itc.direct_debit_sandbox.scenarios;

import org.springframework.stereotype.Component;

@Component
public class ScenarioEngine {

    public String resolveResponseCode(String debitAccount) {
        var threeNums = debitAccount.substring(debitAccount.length() - 3);
        return switch (threeNums) {
            // if last 3 digits = something -> return this response
            case "001" -> "01";  // Success
            case "100" -> "100"; // Payment failed
            case "107" -> "107"; // Invalid credentials
            case "110" -> "110"; // Duplicate transaction
            case "529" -> "529"; // Insufficient balance
            case "131" -> "131"; // Timeout
            case "527" -> "527"; // Resource not found
            case "515" -> "515"; // Account not found
            case "121" -> "121"; // Not allowed
            case "682" -> "682"; // Internal error
            case "779" -> "779"; // Resource locked
            case "111" -> "111"; // Inconclusive
            default -> "100";             // Default to failure
        };
    }

    public String resolveResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "01" -> "Transaction processed successfully";
            case "100" -> "Payment failed";
            case "107" -> "Invalid credentials";
            case "110" -> "Duplicate transaction";
            case "529" -> "Insufficient balance or maximum transaction limit exceeded";
            case "131" -> "Request timed out";
            case "527" -> "Resource not found";
            case "515" -> "Account holder not found";
            case "121" -> "Not allowed to access this service";
            case "682" -> "An internal error caused the operation to fail";
            case "779" -> "The required resource is temporarily locked";
            case "111" -> "Inconclusive status";
            default -> "Payment failed";
        };
    }
}