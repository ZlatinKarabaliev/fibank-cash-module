package bg.fibank.cashdesk.dto;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record CashOperationRecordDTO(
        String cashierName,
        OperationType operationType,
        CurrencyType currency,
        List<DenominationDTO> denominations,
        LocalDate operationDate,
        BigDecimal total


) {
    public static CashOperationRecordDTO fromString(String line) {
        String[] parts = line.split(";");
        String cashierName = parts[0];
        OperationType operationType = OperationType.valueOf(parts[1]);
        CurrencyType currency = CurrencyType.valueOf(parts[2]);
        List<DenominationDTO> denominations = Arrays.stream(parts[3].split(","))
                .map(denStr -> {
                    String[] denParts = denStr.split("x");
                    int quantity = Integer.parseInt(denParts[0]);
                    BigDecimal value = new BigDecimal(denParts[1]);
                    return new DenominationDTO(value, quantity);
                }).toList();
        LocalDate operationDate = LocalDate.parse(parts[4]);
        BigDecimal total = new BigDecimal(parts[5]);

        return new CashOperationRecordDTO(cashierName, operationType, currency, denominations, operationDate, total);
    }

    @Override
    public String toString() {
        String denStr = denominations.stream()
                .map(d -> d.quantity() + "x" + d.value())
                .collect(Collectors.joining(","));
        return String.join(";",
                cashierName,
                operationType.name(),
                currency.name(),
                denStr,
                operationDate.toString(),
                total.toPlainString()  // записваме total в последната позиция
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CashOperationRecordDTO that = (CashOperationRecordDTO) o;
        return Objects.equals(total, that.total) && Objects.equals(cashierName, that.cashierName) && currency == that.currency && Objects.equals(operationDate, that.operationDate) && operationType == that.operationType && Objects.equals(denominations, that.denominations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cashierName, operationType, currency, denominations, operationDate, total);
    }
}

