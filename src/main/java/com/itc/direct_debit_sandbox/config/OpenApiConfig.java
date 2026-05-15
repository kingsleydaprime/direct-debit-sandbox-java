package com.itc.direct_debit_sandbox.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "x-transflowId",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "x-transflowId",
        description = "Transflow session ID issued at login"
)
@SecurityScheme(
        name = "x-key",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "x-key",
        description = "API key for the merchant integration"
)
@SecurityScheme(
        name = "x-country",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "x-country",
        description = "ISO country code for the request (e.g. GH, RW, UG)"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ITC Direct Debit Sandbox API")
                        .version("2.0")
                        .description("""
                                Sandbox environment for testing ITC Direct Debit v2 integrations.

                                ---

                                ## Before you start — provision first

                                Every API flow begins with a call to **POST /provision**. This registers your
                                `callbackUrl`, `productType`, and default retry configuration for a
                                `merchantId + productId` pair. All subsequent callbacks will be delivered to
                                that URL, and all subscriptions for that product will inherit the default config.

                                `merchantId` and `productId` must be **UUIDs**\s
                                (e.g. `c64bf5f9-f147-4232-8d00-f28105823d6a`).

                                ---

                                ## Authentication

                                Click **Authorize** (top-right) and fill in `x-transflowId`, `x-key`, and
                                `x-country` once — they will be sent automatically with every request.

                                ---

                                ## Scenario cheat sheet

                                The **last 3 digits of `debitAccount`** control the simulated bank outcome.
                                Use any prefix you like (e.g. `233241234001`).

                                ### Transient outcomes (retry-aware)
                                | Suffix | Initial attempt | After 1st retry | After 2nd retry |
                                |--------|----------------|-----------------|-----------------|
                                | **001** | ✅ Success | — | — |
                                | **002** | ❌ Fail (101) | ✅ Success | — |
                                | **003** | ❌ Fail (101) | ❌ Fail (101) | ✅ Success |

                                ### Permanent failure outcomes
                                | Suffix | Response code | Meaning |
                                |--------|--------------|---------|
                                | **100** | 100 | General payment failure |
                                | **101** | 101 | Insufficient funds |
                                | **104** | 104 | Duplicate transaction |
                                | **107** | 107 | Invalid credentials |
                                | **110** | 110 | Duplicate transaction (internal) |
                                | **111** | 111 | Inconclusive — status could not be determined |
                                | **121** | 121 | Not allowed to access this service |
                                | **131** | 131 | Request timed out |
                                | **515** | 515 | Account holder not found |
                                | **527** | 527 | Resource not found |
                                | **529** | 529 | Insufficient balance / max limit exceeded |
                                | **682** | 682 | Internal error |
                                | **779** | 779 | Resource temporarily locked |

                                ### Pre-approval scenario
                                | Suffix | Response code | Meaning |
                                |--------|--------------|---------|
                                | **004** | 04 | Pre-approval pending |

                                ---

                                ## Callback timing

                                - Preapproval callback fires **~2 s** after subscription or preauth creation.
                                - Transaction callback fires **~5 s** after the initial debit (or after each retry).
                                - Use `GET /debug/store` to inspect the live in-memory state at any time.
                                """)
                        .contact(new Contact()
                                .name("ITC Direct Debit Sandbox")
                                .email("sandbox@itc.example.com")))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local sandbox"))
                .addServersItem(new Server()
                        .url("https://4379-154-161-176-237.ngrok-free.app")
                        .description("Ngrok"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("x-transflowId")
                        .addList("x-key")
                        .addList("x-country"));
    }
}
