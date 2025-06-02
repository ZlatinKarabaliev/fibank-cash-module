package bg.fibank.cashdesk.storage;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.model.CurrencyBalance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "cashdesk.balance.file=./data/test/balances-test.txt")
class CashBalanceStorageTest {

    @Autowired
    private CashBalanceStorage storage;

    @Autowired
    private org.springframework.core.env.Environment env;

    private Path getTestFilePath() {
        return Path.of(env.getProperty("cashdesk.balance.file"));
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(getTestFilePath());
    }

    @Test
    void testSaveAndLoadBalances() {
        Map<String, Map<CurrencyType, CurrencyBalance>> data = new HashMap<>();

        Map<BigDecimal, Integer> denominations = Map.of(
                BigDecimal.valueOf(50), 10,
                BigDecimal.valueOf(10), 20
        );

        CurrencyBalance balance = new CurrencyBalance(denominations, BigDecimal.valueOf(700));
        Map<CurrencyType, CurrencyBalance> balanceMap = new HashMap<>();
        balanceMap.put(CurrencyType.BGN, balance);
        data.put("MARTINA", balanceMap);

        // Save to file
        storage.saveBalances(data);

        // Load from file
        Map<String, Map<CurrencyType, CurrencyBalance>> loaded = storage.loadBalances();

        assertTrue(loaded.containsKey("MARTINA"));
        assertTrue(loaded.get("MARTINA").containsKey(CurrencyType.BGN));

        CurrencyBalance loadedBalance = loaded.get("MARTINA").get(CurrencyType.BGN);
        assertEquals(BigDecimal.valueOf(700), loadedBalance.getTotal());
        assertEquals(2, loadedBalance.getDenominations().size());
        assertEquals(10, loadedBalance.getDenominations().get(BigDecimal.valueOf(50)));
        assertEquals(20, loadedBalance.getDenominations().get(BigDecimal.valueOf(10)));
    }
}
