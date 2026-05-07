package com.itc.direct_debit_sandbox.scenarios;

import org.springframework.stereotype.Component;

@Component
public class ScenarioEngine {

    public String resolveResponseCode(String debitAccount) {
        return switch (debitAccount) {
            case "233244300001" -> "01";  // Success
            case "233244300100" -> "100"; // Payment failed
            case "233244300107" -> "107"; // Invalid credentials
            case "233244300110" -> "110"; // Duplicate transaction
            case "233244300529" -> "529"; // Insufficient balance
            case "233244300131" -> "131"; // Timeout
            case "233244300527" -> "527"; // Resource not found
            case "233244300515" -> "515"; // Account not found
            case "233244300121" -> "121"; // Not allowed
            case "233244300682" -> "682"; // Internal error
            case "233244300779" -> "779"; // Resource locked
            case "233244300111" -> "111"; // Inconclusive
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