package bg.fibank.cashdesk.model;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a cashier entity holding balances per currency.
 * Uses thread-safe collections for concurrent access.
 */
public class Cashier {

    private final String name;
    private final Map<CurrencyType, CurrencyBalance> balances;

    public Cashier(String name) {
        this.name = name;
        this.balances = new ConcurrentHashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<CurrencyType, CurrencyBalance> getBalances() {
        return balances;
    }

    /**
     * Returns the balance for the given currency or null if not present.
     */
    public CurrencyBalance getBalance(CurrencyType currency) {
        return balances.get(currency);
    }

    /**
     * Updates or adds the balance for a given currency.
     */
    public void updateBalance(CurrencyType currency, CurrencyBalance newBalance) {
        balances.put(currency, newBalance);
    }
}
