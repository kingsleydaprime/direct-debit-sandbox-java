package com.itc.direct_debit_sandbox.subscriptions.lifecycle;

import com.itc.direct_debit_sandbox.scenarios.ScenarioEngine;
import com.itc.direct_debit_sandbox.store.InMemoryStore;
import com.itc.direct_debit_sandbox.store.SubscriptionRecord;
import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import com.itc.direct_debit_sandbox.subscriptions.lifecycle.dto.PauseRequest;
import com.itc.direct_debit_sandbox.callbacks.CallbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class LifecycleServiceTest {

    private LifecycleService lifecycleService;
    private InMemoryStore store;
    private ScenarioEngine scenarioEngine;
    private CallbackService callbackService;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        scenarioEngine = mock(ScenarioEngine.class);
        callbackService = mock(CallbackService.class);
        lifecycleService = new LifecycleService(store, scenarioEngine, callbackService);
    }

    @Test
    void testPauseSubscription() {
        // 1. Arrange: Save an ACTIVE subscription to the store
        String subId = "SUB123";
        SubscriptionRecord record = SubscriptionRecord.builder()
                .subscriptionId(subId)
                .status("ACTIVE")
                .referenceNo("REF123")
                .build();
        store.saveSubscription(subId, record);

        // 2. Act: Call pause
        PauseRequest request = new PauseRequest();
        request.setSubscriptionId(subId);
        request.setProductId("PROD1");
        
        ApiResponseDto<?> response = lifecycleService.pause(request);

        // 3. Assert: Verify response code is success (01) and store status is now PAUSED
        assertEquals("01", response.getResponseCode());
        assertEquals("PAUSED", store.getSubscription(subId).getStatus());
    }
}
