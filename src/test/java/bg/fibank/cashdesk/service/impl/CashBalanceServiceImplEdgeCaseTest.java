
package bg.fibank.cashdesk.service.impl;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.exception.CashOperationException;
import bg.fibank.cashdesk.service.api.CashBalanceService;
import bg.fibank.cashdesk.storage.CashBalanceStorage;
import bg.fibank.cashdesk.storage.CashHistoryStorage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


class CashBalanceServiceImplEdgeCaseTest {

    private CashBalanceService service;

    @BeforeEach
    void setUp() {
        CashHistoryStorage dummyStorage = new CashHistoryStorage("test-history.txt") {
            @Override
            public void saveOperationRecord(CashOperationRecordDTO record) {
                // skip writing to file
            }

            @Override
            public List<CashOperationRecordDTO> getOperationHistory(String cashierName, CurrencyType currency, LocalDate fromDate, LocalDate toDate) {
                return List.of(); // no-op
            }
        };

        CashBalanceStorage dummyBalanceStorage = new CashBalanceStorage("test-balances.txt");

        CashBalanceServiceImpl impl = new CashBalanceServiceImpl(dummyStorage, dummyBalanceStorage);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        impl.setValidator(validator);

        impl.init(); // важно за да се инициализират касиерите

        service = impl;
    }


    @Test
    void testInvalidDenominationThrows() {
        // Tests that using a denomination not supported by the currency (e.g., 123 BGN)
        // will throw a CashOperationException due to business validation.
        CashOperationRequestDTO request = new CashOperationRequestDTO(
                "LINDA",
                OperationType.DEPOSIT,
                CurrencyType.BGN,
                List.of(new DenominationDTO(new BigDecimal("123"), 1)),
                LocalDate.now()
        );

        assertThrows(CashOperationException.class, () -> service.performCashOperation(request));
    }

    @Test
    void testNegativeQuantityThrows() {
        // Tests that providing a negative quantity for a denomination
        // triggers a CashOperationException due to validation constraints.
        CashOperationRequestDTO request = new CashOperationRequestDTO(
                "LINDA",
                OperationType.DEPOSIT,
                CurrencyType.EUR,
                List.of(new DenominationDTO(new BigDecimal("50"), -2)),
                LocalDate.now()
        );

        assertThrows(CashOperationException.class, () -> service.performCashOperation(request));
    }

    @Test
    void testNullCashierThrows() {
        // Tests that providing a null cashier name leads to an exception,
        // either due to validation or unexpected system behavior.
        // This ensures robustness against null inputs.
        CashOperationRequestDTO request = new CashOperationRequestDTO(
                null,
                OperationType.DEPOSIT,
                CurrencyType.BGN,
                List.of(new DenominationDTO(new BigDecimal("10"), 1)),
                LocalDate.now()
        );

        assertThrows(CashOperationException.class, () -> service.performCashOperation(request));
    }
}
