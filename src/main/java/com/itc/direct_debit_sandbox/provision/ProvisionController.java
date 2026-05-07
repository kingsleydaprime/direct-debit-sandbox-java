package com.itc.direct_debit_sandbox.provision;

import com.itc.direct_debit_sandbox.provision.dto.ProvisionRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/provision")
@RequiredArgsConstructor
public class ProvisionController {

    private final ProvisionService provisionService;

    @PostMapping
    public Map<String, Object> provision(
            @RequestHeader("x-transflowId") String transflowId,
            @RequestHeader("x-key") String apiKey,
            @Valid @RequestBody ProvisionRequestDto req) {

        return provisionService.provision(transflowId, apiKey, req);
    }
}
