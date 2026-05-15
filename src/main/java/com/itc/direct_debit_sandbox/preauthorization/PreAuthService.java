package com.itc.direct_debit_sandbox.preauthorization;

import com.itc.direct_debit_sandbox.callbacks.CallbackService;
import com.itc.direct_debit_sandbox.common.CountryDialingCode;
import com.itc.direct_debit_sandbox.preauthorization.dto.*;
import com.itc.direct_debit_sandbox.provision.ProductType;
import com.itc.direct_debit_sandbox.store.PreAuthRecord;
import com.itc.direct_debit_sandbox.store.ProvisionRecord;
import com.itc.direct_debit_sandbox.store.Store;
import com.itc.direct_debit_sandbox.subscriptions.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreAuthService {

    private final Store store;
    private final CallbackService callbackService;

    // ─── HEADER VALIDATION ───────────────────────────────────────────────────

    private ApiResponseDto<?> unauthorizedResponse() {
        return ApiResponseDto.builder()
                .responseCode("107")
                .responseMessage("Invalid Credentials")
                .build();
    }

    private boolean headersInvalid(String transflowId, String apiKey, String country) {
        return transflowId == null || transflowId.isBlank() ||
               apiKey      == null || apiKey.isBlank()      ||
               country     == null || country.isBlank();
    }

    private static final String[] SANDBOX_NAMES = {
        "Ama Owusu", "Kwame Mensah", "Abena Asante", "Kofi Boateng",
        "Akua Darko", "Yaw Adjei", "Efua Osei", "Kwesi Acheampong",
        "Adwoa Frimpong", "Kojo Antwi"
    };

    private String generateClientName(String debitAccount) {
        if (debitAccount == null || debitAccount.isBlank()) return "Sandbox User";
        int idx = Math.abs(debitAccount.hashCode()) % SANDBOX_NAMES.length;
        return SANDBOX_NAMES[idx];
    }

    private ApiResponseDto<?> validatePhoneCountry(String country, String phone, String fieldName) {
        return CountryDialingCode.fromIso(country)
                .flatMap(c -> c.validatePhone(fieldName, phone))
                .map(msg -> (ApiResponseDto<?>) ApiResponseDto.builder()
                        .responseCode("100")
                        .responseMessage(msg)
                        .build())
                .orElse(null);
    }

    private ApiResponseDto<?> checkPreAuthMerchantType(String merchantId, String productId) {
        ProvisionRecord provision = store.getProvision(merchantId, productId);
        if (provision == null || provision.getProductType() == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Product type not configured. Provision with productType PREAUTHORIZED_ONLY or HYBRID to use preauthorization endpoints")
                    .build();
        }
        if (provision.getProductType() == ProductType.SUBSCRIPTIONS_ONLY) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("SUBSCRIPTIONS_ONLY products cannot use preauthorization endpoints. Set productType to PREAUTHORIZED_ONLY or HYBRID")
                    .build();
        }
        return null;
    }

    // ─── CREATE PREAUTH ──────────────────────────────────────────────────────

    /**
     * Creates a preauthorization mandate.
     *
     * A preauth is a standing permission for the merchant to debit the customer's
     * account at any point during the startDate–endDate window. No money moves at
     * creation time — the debit only fires when the merchant calls /mandate/trigger-debit.
     *
     * On success we return "03" immediately and fire a preapproval callback asynchronously
     * to confirm the mandate was set up.
     */
    public ApiResponseDto<?> createPreAuth(String transflowId, String apiKey, String country,
                                           CreatePreAuthRequest req) {

        if (headersInvalid(transflowId, apiKey, country)) return unauthorizedResponse();

        ApiResponseDto<?> phoneError = validatePhoneCountry(country, req.getDebitAccount(), "debitAccount");
        if (phoneError != null) return phoneError;

        ApiResponseDto<?> typeError = checkPreAuthMerchantType(req.getMerchantId(), req.getProductId());
        if (typeError != null) return typeError;

        // Duplicate reference guard
        if (store.getPreAuthByReference(req.getReferenceNo()) != null) {
            return ApiResponseDto.builder()
                    .responseCode("104")
                    .responseMessage("A preauthorization with this reference already exists")
                    .build();
        }

        // endDate must be strictly after startDate
        try {
            LocalDate start = LocalDate.parse(req.getStartDate());
            LocalDate end   = LocalDate.parse(req.getEndDate());
            if (!end.isAfter(start)) {
                return ApiResponseDto.builder()
                        .responseCode("100")
                        .responseMessage("endDate must be after startDate")
                        .build();
            }
        } catch (Exception e) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Invalid date format. Expected yyyy-MM-dd")
                    .build();
        }

        String preApprovalId = UUID.randomUUID().toString();
        String mandateId     = UUID.randomUUID().toString();
        String now           = Instant.now().toString();

        PreAuthRecord record = PreAuthRecord.builder()
                .preApprovalId(preApprovalId)
                .mandateId(mandateId)
                .merchantId(req.getMerchantId())
                .productId(req.getProductId())
                .clientName(generateClientName(req.getDebitAccount()))
                .debitAccount(req.getDebitAccount())
                .countryId(req.getCountry())
                .channel(req.getChannel().name())
                .referenceNo(req.getReferenceNo())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status("ACTIVE")
                .callbackUrl(req.getCallbackUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();

        store.createPreAuth(preApprovalId, record);
        callbackService.firePreAuthCallbacks(record);

        return ApiResponseDto.builder()
                .responseCode("03")
                .responseMessage("your request is being processed")
                .build();
    }

    // ─── TRIGGER MANDATE DEBIT ───────────────────────────────────────────────

    /**
     * Triggers a debit against an existing preauthorization.
     *
     * Unlike subscription trigger-debit (which retries a failed automatic debit),
     * mandate trigger-debit is a deliberate on-demand debit. The caller supplies
     * all debit details (amount, narration, reference) because each call can differ.
     *
     * Guards:
     *  - Mandate must be ACTIVE
     *  - Today must fall within the startDate–endDate window
     */
    public ApiResponseDto<?> triggerMandateDebit(String transflowId, String apiKey, String country,
                                                  TriggerMandateDebitRequest req) {

        if (headersInvalid(transflowId, apiKey, country)) return unauthorizedResponse();

        PreAuthRecord preAuth = store.getPreAuthByMandateId(req.getMandateId());
        if (preAuth == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization mandate not found")
                    .build();
        }

        ApiResponseDto<?> typeError = checkPreAuthMerchantType(preAuth.getMerchantId(), preAuth.getProductId());
        if (typeError != null) return typeError;

        if (!"ACTIVE".equalsIgnoreCase(preAuth.getStatus())) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization is not active")
                    .build();
        }

        // Verify today is within the authorised window
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.parse(preAuth.getStartDate());
        LocalDate end   = LocalDate.parse(preAuth.getEndDate());
        if (today.isBefore(start)) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization window has not started yet")
                    .build();
        }
        if (today.isAfter(end)) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization has expired")
                    .build();
        }

        callbackService.fireMandateTransactionCallback(preAuth, req);

        return ApiResponseDto.builder()
                .responseCode("03")
                .responseMessage("processing")
                .build();
    }

    // ─── CHECK MANDATE STATUS ────────────────────────────────────────────────

    /**
     * Returns the current status of a preauthorization (ACTIVE / CANCELLED).
     * Uses the third-party reference number to look up the record.
     */
    public ApiResponseDto<?> checkMandateStatus(String transflowId, String apiKey, String country,
                                                 CheckMandateStatusRequest req) {

        if (headersInvalid(transflowId, apiKey, country)) return unauthorizedResponse();

        PreAuthRecord preAuth = store.getPreAuthByReference(req.getReference());
        if (preAuth == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization not found")
                    .build();
        }

        return ApiResponseDto.builder()
                .responseCode("01")
                .responseMessage(preAuth.getStatus().toLowerCase())
                .build();
    }

    // ─── RETRIEVE PREAUTH DETAILS ────────────────────────────────────────────

    /**
     * Returns the full preauthorization record.
     * PreAuthRecord field names already match the API response schema, so the record
     * is returned directly in the data field without any mapping.
     */
    public ApiResponseDto<?> retrievePreAuthDetails(String transflowId, String apiKey, String country,
                                                     RetrievePreAuthRequest req) {

        if (headersInvalid(transflowId, apiKey, country)) return unauthorizedResponse();

        PreAuthRecord preAuth = store.getPreAuthByReference(req.getReferenceId());
        if (preAuth == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization not found")
                    .build();
        }

        return ApiResponseDto.builder()
                .responseCode("01")
                .responseMessage("operation successful")
                .data(toRetrieveResponse(preAuth))
                .build();
    }

    private java.util.Map<String, Object> toRetrieveResponse(PreAuthRecord r) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("preApprovalId", r.getPreApprovalId());
        m.put("productId",     r.getProductId());
        m.put("clientName",    r.getClientName());
        m.put("mandateId",     r.getMandateId());
        m.put("debitAccount",  r.getDebitAccount());
        m.put("countryId",     r.getCountryId());
        m.put("merchantId",    r.getMerchantId());
        m.put("mandateType",   "authorization");
        m.put("status",        r.getStatus() != null ? r.getStatus().toLowerCase() : null);
        m.put("debitSource",   r.getChannel());
        m.put("refNo",         r.getReferenceNo());
        m.put("startDate",     r.getStartDate());
        m.put("endDate",       r.getEndDate());
        m.put("created",       r.getCreatedAt());
        m.put("updated",       r.getUpdatedAt());
        return m;
    }

    // ─── CANCEL PREAUTH ──────────────────────────────────────────────────────

    /**
     * Cancels an active preauthorization.
     * The caller must supply the preApprovalId, debitAccount, channel, and country to
     * confirm they are the correct owner of the mandate before cancellation is allowed.
     */
    public ApiResponseDto<?> cancelPreAuth(String transflowId, String apiKey, String country,
                                           CancelPreAuthRequest req) {

        if (headersInvalid(transflowId, apiKey, country)) return unauthorizedResponse();

        PreAuthRecord preAuth = store.getPreAuth(req.getPreApprovalId());
        if (preAuth == null) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization not found")
                    .build();
        }

        if (!"ACTIVE".equalsIgnoreCase(preAuth.getStatus())) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Preauthorization is already cancelled")
                    .build();
        }

        // Verify the caller's details match the record (prevents one merchant from
        // cancelling another merchant's mandate)
        if (!preAuth.getDebitAccount().equals(req.getDebitAccount()) ||
            !preAuth.getChannel().equals(req.getChannel().name())    ||
            !preAuth.getCountryId().equalsIgnoreCase(req.getCountry())) {
            return ApiResponseDto.builder()
                    .responseCode("100")
                    .responseMessage("Provided details do not match the preauthorization record")
                    .build();
        }

        store.updatePreAuthStatus(req.getPreApprovalId(), "CANCELLED");

        return ApiResponseDto.builder()
                .responseCode("01")
                .responseMessage("your request has been processed")
                .build();
    }
}
