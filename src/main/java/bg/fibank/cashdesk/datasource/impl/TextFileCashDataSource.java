package bg.fibank.cashdesk.datasource.impl;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.datasource.CashDataSource;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.common.utils.OperationType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads cash operation data from a structured text file.
 * Supports optional operation date field with default to current date.
 */
@Component
public class TextFileCashDataSource implements CashDataSource {

    @Value("classpath:data/balances.txt")
    private Resource dataFile;

    @Override
    public List<CashOperationRequestDTO> loadOperations() {
        Map<String, List<DenominationDTO>> groupedByCashierCurrency = new HashMap<>();
        Map<String, OperationType> operationMap = new HashMap<>();
        Map<String, CurrencyType> currencyMap = new HashMap<>();
        Map<String, LocalDate> operationDateMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataFile.getInputStream(), StandardCharsets.UTF_8))) {

            reader.lines()
                    .map(this::parseLine)
                    .forEach(entry -> {
                        String key = entry.cashierName + "|" + entry.currency;
                        groupedByCashierCurrency.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(new DenominationDTO(entry.value, entry.quantity));
                        operationMap.put(key, entry.operationType);
                        currencyMap.put(key, entry.currency);
                        operationDateMap.put(key, entry.operationDate);
                    });

        } catch (Exception e) {
            throw new RuntimeException("Failed to read cash operations from file", e);
        }

        return groupedByCashierCurrency.keySet().stream()
                .map(key -> {
                    String[] parts = key.split("\\|");
                    String cashierName = parts[0];
                    CurrencyType currency = currencyMap.get(key);
                    OperationType op = operationMap.get(key);
                    List<DenominationDTO> denominations = groupedByCashierCurrency.get(key);
                    LocalDate operationDate = operationDateMap.getOrDefault(key, LocalDate.now());
                    return new CashOperationRequestDTO(cashierName, op, currency, denominations, operationDate);
                })
                .toList();
    }

    private ParsedLine parseLine(String line) {
        Map<String, String> parts = Arrays.stream(line.split(";"))
                .map(s -> s.split("="))
                .filter(p -> p.length == 2)
                .collect(Collectors.toMap(p -> p[0].trim(), p -> p[1].trim()));

        LocalDate date = parts.containsKey("date") ? LocalDate.parse(parts.get("date")) : LocalDate.now();

        return new ParsedLine(
                parts.get("cashier"),
                CurrencyType.valueOf(parts.get("currency")),
                new BigDecimal(parts.get("value")),
                Integer.parseInt(parts.get("quantity")),
                OperationType.valueOf(parts.getOrDefault("operation", "DEPOSIT")),
                date
        );
    }

    private record ParsedLine(
            String cashierName,
            CurrencyType currency,
            BigDecimal value,
            int quantity,
            OperationType operationType,
            LocalDate operationDate
    ) {}
}
