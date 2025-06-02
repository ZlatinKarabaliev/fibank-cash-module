package bg.fibank.cashdesk.common.utils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central registry for supported cash denominations per currency.
 * Uses enums for precise and type-safe configuration.
 */
public final class DenominationRegistry {

    private static final Map<CurrencyType, Set<BigDecimal>> denominationMap = new EnumMap<>(CurrencyType.class);

    static {
        Set<BigDecimal> bgnValues = Arrays.stream(BgnDenomination.values())
                .map(BgnDenomination::getValue)
                .collect(Collectors.toSet());
        Set<BigDecimal> eurValues = Arrays.stream(EuroDenomination.values())
                .map(EuroDenomination::getValue)
                .collect(Collectors.toSet());

        denominationMap.put(CurrencyType.BGN, bgnValues);
        denominationMap.put(CurrencyType.EUR, eurValues);
    }

    private DenominationRegistry() {
        // Prevent instantiation
    }

    /**
     * Returns an unmodifiable set of valid denominations for a currency.
     */
    public static Set<BigDecimal> getSupportedDenominations(CurrencyType currency) {
        return Collections.unmodifiableSet(denominationMap.getOrDefault(currency, Set.of()));
    }

    /**
     * Checks if a denomination is supported for the given currency.
     */
    public static boolean isSupported(CurrencyType currency, BigDecimal value) {
        return denominationMap.getOrDefault(currency, Set.of()).contains(value);
    }

    /**
     * Registers additional denominations at runtime.
     */
    public static void registerDenominations(CurrencyType currency, Collection<BigDecimal> newDenominations) {
        denominationMap.computeIfAbsent(currency, c -> new HashSet<>()).addAll(newDenominations);
    }
}
