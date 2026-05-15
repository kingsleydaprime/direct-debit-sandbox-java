package com.itc.direct_debit_sandbox.common;

import java.util.Arrays;
import java.util.Optional;

public enum CountryDialingCode {
    GH("GH", "233"),
    RW("RW", "250"),
    UG("UG", "256"),
    KE("KE", "254"),
    NG("NG", "234"),
    TZ("TZ", "255"),
    CI("CI", "225"),
    SN("SN", "221"),
    CM("CM", "237");

    private final String isoCode;
    private final String prefix;

    CountryDialingCode(String isoCode, String prefix) {
        this.isoCode = isoCode;
        this.prefix  = prefix;
    }

    public String getIsoCode()  { return isoCode; }
    public String getPrefix()   { return prefix; }

    public static Optional<CountryDialingCode> fromIso(String iso) {
        if (iso == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(c -> c.isoCode.equalsIgnoreCase(iso.trim()))
                .findFirst();
    }

    /** Returns an error message if the phone number doesn't start with this country's prefix, empty if valid. */
    public Optional<String> validatePhone(String fieldName, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) return Optional.empty(); // let @NotBlank handle missing
        if (!phoneNumber.startsWith(prefix)) {
            return Optional.of(fieldName + " must start with " + prefix + " for country " + isoCode
                    + " (e.g. " + prefix + "XXXXXXXXX)");
        }
        return Optional.empty();
    }
}
