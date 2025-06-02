package bg.fibank.cashdesk.dto;

import bg.fibank.cashdesk.common.utils.CurrencyType;

import java.math.BigDecimal;
import java.util.Set;

public record CurrencyDenominationsDTO(
        CurrencyType currency,
        Set<BigDecimal> denominations
) {}
