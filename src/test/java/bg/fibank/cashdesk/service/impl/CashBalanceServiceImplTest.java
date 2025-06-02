package bg.fibank.cashdesk.service.impl;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.CurrencyBalanceDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.service.api.CashBalanceService;
import bg.fibank.cashdesk.storage.CashBalanceStorage;
import bg.fibank.cashdesk.storage.CashHistoryStorage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CashBalanceServiceImplTest {

    private CashBalanceService service;

    @BeforeEach
    void setUp() {
        CashHistoryStorage dummyHistoryStorage = new CashHistoryStorage("test-history.txt");
        CashBalanceStorage dummyBalanceStorage = new CashBalanceStorage("test-balances.txt");

        CashBalanceServiceImpl impl = new CashBalanceServiceImpl(dummyHistoryStorage, dummyBalanceStorage);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        impl.validator = validator;

        impl.init();

        service = impl;
    }



    @AfterEach
    void cleanUp() {
        new File("test-balances.txt").delete();
        new File("test-history.txt").delete();
    }


    @Test
    void testDepositIncreasesBalance() {
        CashOperationRequestDTO request = new CashOperationRequestDTO(
                "MARTINA",
                OperationType.DEPOSIT,
                CurrencyType.BGN,
                List.of(new DenominationDTO(BigDecimal.valueOf(50), 1)),
                LocalDate.now()
        );

        service.performCashOperation(request);

        CurrencyBalanceDTO balance = service.getBalance("MARTINA", CurrencyType.BGN, null, null);
        assertEquals(0, balance.total().compareTo(new BigDecimal("50.00")));
    }
}
