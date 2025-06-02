package bg.fibank.cashdesk.storage;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.helper.CashOperationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CashHistoryStorage {

    private static final Logger logger = LoggerFactory.getLogger(CashHistoryStorage.class);

    private String historyDirectory;


    public CashHistoryStorage(@Value("${cash.history.directory}") String historyDirectory) {
        this.historyDirectory = historyDirectory;
    }
    private Path getHistoryFilePath(LocalDate date) {
        String fileName = String.format("history_%s.txt", date);
        return Paths.get(historyDirectory, fileName);
    }

    private String serializeRecord(CashOperationRecordDTO record) {
        return record.toString();
    }

    public void saveOperationRecord(CashOperationRecordDTO record) {
        Path path = getHistoryFilePath(record.operationDate());
        String line = serializeRecord(record);

        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("Saved operation record to file {}", path);
        } catch (IOException e) {
            logger.error("Failed to save operation record", e);
            throw new RuntimeException("Unable to save operation record", e);
        }
    }

    public List<CashOperationRecordDTO> getOperationHistory(String cashierName, CurrencyType currency,
                                                            LocalDate dateFrom, LocalDate dateTo) {
        List<CashOperationRecordDTO> history = new ArrayList<>();

        for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
            Path file = getHistoryFilePath(date);
            history.addAll(readHistoryFile(file, cashierName, currency));
        }

        return history;
    }

    private List<CashOperationRecordDTO> readHistoryFile(Path file, String cashierName, CurrencyType currency) {
        if (!Files.exists(file)) {
            return List.of();
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return reader.lines()
                    .map(line -> {
                        try {
                            return CashOperationHelper.deserializeRecord(line);
                        } catch (Exception ex) {
                            logger.warn("Skipping invalid history line: {} – reason: {}", line, ex.getMessage());
                            return null;
                        }
                    })
                    .filter(record -> record != null)
                    .filter(record -> cashierName == null || record.cashierName().equalsIgnoreCase(cashierName))
                    .filter(record -> currency == null || record.currency().equals(currency))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error reading history file {}", file, e);
            throw new RuntimeException("Failed to read history files", e);
        }
    }
}
