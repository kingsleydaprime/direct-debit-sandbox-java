package com.itc.direct_debit_sandbox.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sandbox")
public class SandboxConfig {
    private long callbackDelayPreapproval;
    private long callbackDelayTransaction;
}