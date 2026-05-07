package com.itc.direct_debit_sandbox.subscriptions;

import com.itc.direct_debit_sandbox.subscriptions.dto.CancelRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.CustomerSubRequest;
import com.itc.direct_debit_sandbox.subscriptions.dto.SubscriptionRequestDto;
import com.itc.direct_debit_sandbox.subscriptions.dto.UpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;



   @PostMapping("/subscribe")
public Map<String, Object> subscribe(
           @RequestHeader ("x-transflowId") String transflowId,
           @RequestHeader ("x-key") String apiKey,
           @RequestHeader("x-country")  String country,
          @Valid @RequestBody SubscriptionRequestDto req
   ) {

        return subscriptionService.subscribe(transflowId,apiKey,country,req);
   }

    @PostMapping("/update")
    public Map<String, Object> update(
            @RequestHeader("x-transflowId") String transflowId,
            @RequestHeader("x-key") String apiKey,
            @RequestHeader("x-country") String country,
            @Valid @RequestBody UpdateRequest req) {

        return subscriptionService.update(transflowId, apiKey, country, req);
    }

    @PostMapping("/cancel")
    public Map<String, Object> cancel(
            @RequestHeader("x-transflowId") String transflowId,
            @RequestHeader("x-key") String apiKey,
            @RequestHeader("x-country") String country,
            @Valid @RequestBody CancelRequest req) {

        return subscriptionService.cancel(transflowId, apiKey, country, req);
    }

    @PostMapping("/customer-subscriptions")
    public Map<String, Object> getCustomerSubscriptions(
            @RequestHeader("x-transflowId") String transflowId,
            @RequestHeader("x-key") String apiKey,
            @RequestHeader("x-country") String country,
            @RequestBody CustomerSubRequest req) {

        return subscriptionService.getCustomerSubscriptions(transflowId, apiKey, country, req);
    }

}
