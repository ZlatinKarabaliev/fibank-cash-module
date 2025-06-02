package bg.fibank.cashdesk.storage;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.model.CurrencyBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CashBalanceStorage {

    private static final Logger logger = LoggerFactory.getLogger(CashBalanceStorage.class);

    private String balanceDirectory;

    public CashBalanceStorage(@Value("${cash.balance.directory}") String balanceDirectory) {
        this.balanceDirectory = balanceDirectory;
    }

    private Path getBalanceFilePath() {
        return Paths.get(balanceDirectory, "balances.txt");
    }

    public void saveBalances(Map<String, Map<CurrencyType, CurrencyBalance>> balances) {
        Path path = getBalanceFilePath();

        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            List<String> lines = new ArrayList<>();
            for (var entry : balances.entrySet()) {
                String cashier = entry.getKey();
                for (var currencyEntry : entry.getValue().entrySet()) {
                    String line = serializeBalance(cashier, currencyEntry.getKey(), currencyEntry.getValue());
                    lines.add(line);
                }
            }

            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Saved balances to file {}", path);
        } catch (IOException e) {
            logger.error("Failed to save balances", e);
            throw new RuntimeException("Unable to save balances", e);
        }
    }

    public Map<String, Map<CurrencyType, CurrencyBalance>> loadBalances() {
        Path path = getBalanceFilePath();
        Map<String, Map<CurrencyType, CurrencyBalance>> result = new HashMap<>();

        if (!Files.exists(path)) {
            logger.warn("Balance file does not exist: {}", path);
            return result;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.lines().forEach(line -> {
                try {
                    String[] parts = line.split(";");
                    String cashier = parts[0];
                    CurrencyType currency = CurrencyType.valueOf(parts[1]);
                    BigDecimal total = new BigDecimal(parts[2]);

                    Map<BigDecimal, Integer> denominations = Arrays.stream(parts[3].split(","))
                            .map(pair -> pair.split("x"))
                            .collect(Collectors.toMap(
                                    tokens -> new BigDecimal(tokens[1]),
                                    tokens -> Integer.parseInt(tokens[0])
                            ));

                    CurrencyBalance balance = new CurrencyBalance(denominations, total);
                    result.computeIfAbsent(cashier, k -> new HashMap<>()).put(currency, balance);
                } catch (Exception ex) {
                    logger.warn("Skipping invalid balance line: {}", line, ex);
                }
            });
        } catch (IOException e) {
            logger.error("Failed to load balances from file {}", path, e);
            throw new RuntimeException("Unable to load balances", e);
        }

        return result;
    }

    private String serializeBalance(String cashier, CurrencyType currency, CurrencyBalance balance) {
        String denomStr = balance.getDenominations().entrySet().stream()
                .map(e -> e.getValue() + "x" + e.getKey())
                .collect(Collectors.joining(","));
        return String.join(";", cashier, currency.name(), balance.getTotal().toPlainString(), denomStr);
    }
}
