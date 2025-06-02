package bg.fibank.cashdesk.model;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.common.utils.OperationType;

import java.time.LocalDate;
import java.util.List;

public record CashOperation(
        String cashierName,
        OperationType operationType,
        CurrencyType currency,
        List<DenominationDTO> denominations,
        LocalDate operationDate
) {}