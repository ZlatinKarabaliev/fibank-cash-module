package bg.fibank.cashdesk.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO representing a denomination and its quantity in a cash operation.
 */
public record DenominationDTO(

        @NotNull(message = "Denomination value must not be null")
        @DecimalMin(value = "0.01", message = "Minimum denomination is 0.01")
        @Digits(integer = 6, fraction = 2, message = "Denomination must be a valid monetary amount")
        BigDecimal value,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity

) {
}
