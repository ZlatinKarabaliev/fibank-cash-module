package bg.fibank.cashdesk.dto;

import bg.fibank.cashdesk.common.utils.CurrencyType;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing a currency balance including total and denomination breakdown.
 */
public record CurrencyBalanceDTO(

        CurrencyType currency,
        BigDecimal total,
        List<DenominationDTO> denominations

) {}
