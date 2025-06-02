package bg.fibank.cashdesk.mapper;

import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.model.CashOperationDomainModel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps external DTO into an internal domain model.
 */
public class RequestToDomainMapper {
    public CashOperationDomainModel mapToDomain(CashOperationRequestDTO dto) {
        Map<BigDecimal, Integer> map = dto.denominations().stream()
                .collect(Collectors.toMap(DenominationDTO::value, DenominationDTO::quantity));

        return new CashOperationDomainModel(
                dto.cashierName(),
                dto.operationType(),
                dto.currency(),
                map,
                dto.operationDate()
        );
    }
}
