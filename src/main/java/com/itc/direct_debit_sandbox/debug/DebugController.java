package com.itc.direct_debit_sandbox.debug;

import com.itc.direct_debit_sandbox.store.InMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final InMemoryStore store;

    @GetMapping("/store")
    public Map<String, Object> store() {
        Map<String, Object> snapshot = store.getSnapshot();
        log.info("Store snapshot: {}", snapshot);
        return snapshot;
    }
}
