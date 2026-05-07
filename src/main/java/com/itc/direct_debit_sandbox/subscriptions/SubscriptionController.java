package com.itc.direct_debit_sandbox.subscriptions;

import com.itc.direct_debit_sandbox.subscriptions.SubscriptionService;
import com.itc.direct_debit_sandbox.subscriptions.dto.SubscriptionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;



    @GetMapping("/")
    public ResponseEntity<String> getSubscriptions() {

        return ResponseEntity.ok("Hello world");
    }

    @PostMapping
    public String subscription(@RequestBody SubscriptionRequestDto subscription) {
        return "Nice to meet you!";
    }

}
