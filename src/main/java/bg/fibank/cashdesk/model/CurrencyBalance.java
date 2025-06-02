package bg.fibank.cashdesk.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents the balance for a currency including denominations and total amount.
 * Immutable design for thread safety and clarity.
 */
@Getter
public class CurrencyBalance {

    private final Map<BigDecimal, Integer> denominations;
    private final BigDecimal total;

    public CurrencyBalance(Map<BigDecimal, Integer> denominations, BigDecimal total) {
        this.denominations = denominations;
        this.total = total;
    }

    /**
     * Returns a new CurrencyBalance with updated denominations after applying the quantity change.
     */
    public CurrencyBalance update(BigDecimal denomination, int quantityChange) {
        Map<BigDecimal, Integer> updatedDenominations = new java.util.HashMap<>(denominations);
        updatedDenominations.merge(denomination, quantityChange, Integer::sum);

        // Remove entries with zero or negative quantities to keep data clean
        updatedDenominations.entrySet().removeIf(entry -> entry.getValue() <= 0);

        BigDecimal updatedTotal = updatedDenominations.entrySet().stream()
                .map(e -> e.getKey().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CurrencyBalance(updatedDenominations, updatedTotal);
    }
}
