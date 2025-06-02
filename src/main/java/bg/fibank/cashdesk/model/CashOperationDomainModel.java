package bg.fibank.cashdesk.model;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Internal model representing a validated and transformed cash operation.
 * Separated from external DTO for better maintainability.
 */
public record CashOperationDomainModel(
        String cashierName,
        OperationType operationType,
        CurrencyType currency,
        Map<BigDecimal, Integer> denominations,
        LocalDate operationDate

) {}
