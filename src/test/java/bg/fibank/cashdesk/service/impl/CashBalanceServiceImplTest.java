package bg.fibank.cashdesk.service.impl;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.CurrencyBalanceDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.model.CurrencyBalance;
import bg.fibank.cashdesk.service.api.CashBalanceService;
import bg.fibank.cashdesk.storage.CashBalanceStorage;
import bg.fibank.cashdesk.storage.CashHistoryStorage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CashBalanceServiceImplTest {

    private static final String BALANCE_DIR = "test-balances.txt";
    private static final String HISTORY_DIR = "test-history.txt";

    private CashBalanceService service;
    private CashBalanceStorage dummyBalanceStorage;

    @BeforeEach
    void setUp() {
        cleanUp(); // Ensure clean state before test

        CashHistoryStorage dummyHistoryStorage = new CashHistoryStorage(HISTORY_DIR);
        dummyBalanceStorage = new CashBalanceStorage(BALANCE_DIR);

        // Initialize test data: MARTINA with 1000 BGN
        Map<String, Map<CurrencyType, CurrencyBalance>> initialData = new HashMap<>();
        Map<BigDecimal, Integer> denominations = Map.of(
                BigDecimal.valueOf(50), 10,
                BigDecimal.valueOf(10), 50
        );
        CurrencyBalance balance = new CurrencyBalance(denominations, BigDecimal.valueOf(1000));
        Map<CurrencyType, CurrencyBalance> currencyMap = new HashMap<>();
        currencyMap.put(CurrencyType.BGN, balance);
        initialData.put("MARTINA", currencyMap);

        dummyBalanceStorage.saveBalances(initialData);

        CashBalanceServiceImpl impl = new CashBalanceServiceImpl(dummyHistoryStorage, dummyBalanceStorage);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        impl.validator = validator;

        impl.init();

        service = impl;
    }

    @AfterEach
    void cleanUp() {
        deleteRecursive(new File(BALANCE_DIR));
        deleteRecursive(new File(HISTORY_DIR));
    }

    private void deleteRecursive(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] contents = file.listFiles();
                if (contents != null) {
                    for (File f : contents) deleteRecursive(f);
                }
            }
            file.delete();
        }
    }

    @Test
    void testDepositIncreasesBalance() {
        // Create deposit request: 1 x 50 BGN
        CashOperationRequestDTO request = new CashOperationRequestDTO(
                "MARTINA",
                OperationType.DEPOSIT,
                CurrencyType.BGN,
                List.of(new DenominationDTO(BigDecimal.valueOf(50), 1)),
                LocalDate.now()
        );

        service.performCashOperation(request);

        CurrencyBalanceDTO balance = service.getBalance("MARTINA", CurrencyType.BGN, null, null);
        assertEquals(0, balance.total().compareTo(new BigDecimal("1050.00")));
    }
}
