package bg.fibank.cashdesk.common.utils;

import java.util.Arrays;
import java.util.Optional;

public enum CurrencyType {
    BGN("BGN"), EUR("EUR");

    private final String code;

    CurrencyType(String code) {
        this.code = code;
    }

    /**
     * Returns the ISO code of the currency.
     */
    public String getCode() {
        return code;
    }

    /**
     * Attempts to resolve a CurrencyType by ISO code (case insensitive).
     *
     * @param code the currency code as a string
     * @return optional containing the matching CurrencyType, if present
     */
    public static Optional<CurrencyType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * Returns the ISO code as string representation.
     */
    @Override
    public String toString() {
        return code;
    }
}