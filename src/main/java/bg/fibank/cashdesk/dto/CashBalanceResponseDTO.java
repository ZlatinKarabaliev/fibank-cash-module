package bg.fibank.cashdesk.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response structure for current balance inquiry.
 * Includes total amount and breakdown by denominations.
 */
public record CashBalanceResponseDTO(
        String cashier,
        String currency,
        BigDecimal total,
        Map<BigDecimal, Integer> denominations
) {}
