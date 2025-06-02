package bg.fibank.cashdesk.dto;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Input DTO used to submit a cash operation request.
 */
public record CashOperationRequestDTO(

        @NotBlank(message = "Cashier name must not be blank")
        String cashierName,

        @NotNull(message = "Operation type is required")
        OperationType operationType,

        @NotNull(message = "Currency is required")
        CurrencyType currency,

        @NotEmpty(message = "At least one denomination must be provided")
        @Valid
        List<DenominationDTO> denominations,

        @NotNull(message = "Operation date must be provided")
        LocalDate operationDate

) {}
