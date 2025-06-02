package bg.fibank.cashdesk.helper;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.exception.CashOperationException;
import bg.fibank.cashdesk.model.Cashier;
import bg.fibank.cashdesk.model.CurrencyBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CashOperationHelper {

    private static final Logger logger = LoggerFactory.getLogger(CashOperationHelper.class);

    // Calculates the total amount of money for a given list of denominations
    public static BigDecimal calculateTotal(List<DenominationDTO> denominations) {
        return denominations.stream()
                .map(d -> d.value().multiply(BigDecimal.valueOf(d.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Applies a cash operation (deposit or withdrawal) to the cashier's balance
    public static CurrencyBalance applyOperation(Cashier cashier, CashOperationRequestDTO request) {
        CurrencyType currency = request.currency();
        CurrencyBalance current = cashier.getBalance(currency);

        if (current == null) {
            current = new CurrencyBalance(Map.of(), BigDecimal.ZERO);
        }

        CurrencyBalance updated = current;
        boolean isWithdrawal = request.operationType() == OperationType.WITHDRAWAL;

        for (DenominationDTO denom : request.denominations()) {
            int requestedQty = denom.quantity();
            int delta = isWithdrawal ? -requestedQty : requestedQty;

            int currentQty = current.getDenominations().getOrDefault(denom.value(), 0);
            if (isWithdrawal && currentQty < requestedQty) {
                logger.error("Insufficient quantity for denomination {}: requested={}, available={}",
                        denom.value(), requestedQty, currentQty);
                throw new CashOperationException("Insufficient quantity for denomination " + denom.value());
            }

            updated = updated.update(denom.value(), delta);
        }

        return updated;
    }

    // Utility method to default a LocalDate if it's null
    public static LocalDate defaultDate(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }

    // Deserialize a string line into CashOperationRecordDTO
    public static CashOperationRecordDTO deserializeRecord(String line) {
        return CashOperationRecordDTO.fromString(line);
    }
}
