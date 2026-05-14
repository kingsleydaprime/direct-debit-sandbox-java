package com.itc.direct_debit_sandbox.debug;

import com.itc.direct_debit_sandbox.store.InMemoryStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Debug", description = "Sandbox-only utilities — inspect live in-memory state")
@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final InMemoryStore store;

    @Operation(
        summary = "Dump store snapshot",
        description = "Returns the full contents of all in-memory maps: subscriptions, transactions, provisions, preAuths, and all secondary indexes. Useful for watching state change during retry and callback testing."
    )
    @GetMapping("/store")
    public Map<String, Object> store() {
        Map<String, Object> snapshot = store.getSnapshot();
        log.info("Store snapshot: {}", snapshot);
        return snapshot;
    }
}
