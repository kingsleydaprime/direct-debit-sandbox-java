package com.itc.direct_debit_sandbox.transactions;

import com.itc.direct_debit_sandbox.store.InMemoryStore;
import com.itc.direct_debit_sandbox.store.TransactionRecord;
import com.itc.direct_debit_sandbox.transactions.dto.TransactionStatusRequestDto;
import com.itc.direct_debit_sandbox.transactions.dto.TransactionStatusResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final InMemoryStore store;

    public TransactionStatusResponseDto checkStatus(TransactionStatusRequestDto request) {
        TransactionRecord record = store.getTransaction(request.getReference());

        if (record == null) {
            return TransactionStatusResponseDto.builder()
                    .responseCode("99")
                    .status("NOT FOUND")
                    .build();
        }

        return TransactionStatusResponseDto.builder()
                .responseCode(record.getResponseCode())
                .status(record.getStatus())
                .build();
    }
}