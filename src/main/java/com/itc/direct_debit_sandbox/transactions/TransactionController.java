package com.itc.direct_debit_sandbox.transactions;

import com.itc.direct_debit_sandbox.transactions.dto.TransactionStatusRequestDto;
import com.itc.direct_debit_sandbox.transactions.dto.TransactionStatusResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/check-status")
    public TransactionStatusResponseDto checkStatus(
            @RequestHeader("x-transflowId") String transflowId,
            @RequestHeader("x-key") String apiKey,
            @RequestHeader("x-country") String country,
            @RequestBody TransactionStatusRequestDto req) {

        return transactionService.checkStatus(req);
    }
}
