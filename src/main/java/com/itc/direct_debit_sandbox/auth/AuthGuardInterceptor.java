package com.itc.direct_debit_sandbox.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itc.direct_debit_sandbox.common.CountryDialingCode;
import com.itc.direct_debit_sandbox.store.ProvisionRecord;
import com.itc.direct_debit_sandbox.store.Store;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthGuardInterceptor implements HandlerInterceptor {

    private final Store store;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String transflowId = request.getHeader("x-transflowId");
        String apiKey      = request.getHeader("x-key");

        if (!isValidUuid(transflowId) || !isValidUuid(apiKey)) {
            writeError(response, "x-transflowId and x-key must be valid UUIDs");
            return false;
        }

        ProvisionRecord provision = store.getProvisionByTransflowId(transflowId);
        if (provision == null || !apiKey.equals(provision.getApiKey())) {
            writeError(response, "Unrecognised transflowId or apiKey");
            return false;
        }

        String country = request.getHeader("x-country");
        if (country != null && !country.isBlank() && CountryDialingCode.fromIso(country).isEmpty()) {
            writeError(response, "Unsupported x-country '" + country + "'. Supported codes: GH, RW, UG, KE, NG, TZ, CI, SN, CM");
            return false;
        }

        return true;
    }

    private boolean isValidUuid(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void writeError(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                Map.of("responseCode", "107", "responseMessage", "Invalid credentials: " + message));
    }
}
