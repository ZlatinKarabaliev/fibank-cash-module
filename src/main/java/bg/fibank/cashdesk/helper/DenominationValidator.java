package bg.fibank.cashdesk.helper;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.DenominationRegistry;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.exception.CashOperationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class DenominationValidator {

    public static void validate(CurrencyType currency, List<DenominationDTO> denominations) {
        Set<BigDecimal> supported = DenominationRegistry.getSupportedDenominations(currency);
        for (DenominationDTO d : denominations) {
            boolean matched = supported.stream().anyMatch(s -> s.compareTo(d.value()) == 0);
            if (!matched) {
                throw new CashOperationException("Unsupported denomination: " + d.value());
            }
        }
    }
}
