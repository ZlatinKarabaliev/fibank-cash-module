package bg.fibank.cashdesk.service.impl;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.DenominationRegistry;
import bg.fibank.cashdesk.common.utils.OperationType;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.CurrencyBalanceDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import bg.fibank.cashdesk.exception.CashOperationException;
import bg.fibank.cashdesk.helper.CashOperationHelper;
import bg.fibank.cashdesk.helper.DenominationValidator;
import bg.fibank.cashdesk.model.CashOperation;
import bg.fibank.cashdesk.model.Cashier;
import bg.fibank.cashdesk.model.CurrencyBalance;
import bg.fibank.cashdesk.service.api.CashBalanceService;
import bg.fibank.cashdesk.storage.CashBalanceStorage;
import bg.fibank.cashdesk.storage.CashHistoryStorage;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CashBalanceServiceImpl implements CashBalanceService {

    private static final Logger logger = LoggerFactory.getLogger(CashBalanceServiceImpl.class);

    private final Map<String, Cashier> cashiers = new ConcurrentHashMap<>();
    private final List<CashOperation> operationHistory = Collections.synchronizedList(new ArrayList<>());

    @Setter
    @Autowired
    protected Validator validator;


    private CashBalanceStorage cashBalanceStorage;

    private final CashHistoryStorage historyStorage;

    @Autowired
    public CashBalanceServiceImpl(CashHistoryStorage historyStorage,CashBalanceStorage cashBalanceStorage) {
        this.historyStorage = historyStorage;
        this.cashBalanceStorage = cashBalanceStorage;
    }

    @PostConstruct
    public void init() {
        Map<String, Map<CurrencyType, CurrencyBalance>> loaded = cashBalanceStorage.loadBalances();
        if (!loaded.isEmpty()) {
            loaded.forEach((name, balances) -> {
                Cashier c = new Cashier(name);
                balances.forEach(c::updateBalance);
                cashiers.put(name, c);
            });
            logger.info("Loaded cashiers and balances from file");
        } else {
            Cashier martina = new Cashier("MARTINA");
            martina.updateBalance(CurrencyType.BGN,
                    new CurrencyBalance(Map.of(
                            BigDecimal.valueOf(50), 10,
                            BigDecimal.valueOf(10), 50
                    ), BigDecimal.valueOf(1000)));

            martina.updateBalance(CurrencyType.EUR,
                    new CurrencyBalance(Map.of(
                            BigDecimal.valueOf(100), 10,
                            BigDecimal.valueOf(20), 50
                    ), BigDecimal.valueOf(2000)));

            Cashier peter = new Cashier("PETER");
            peter.updateBalance(CurrencyType.BGN,
                    new CurrencyBalance(Map.of(
                            BigDecimal.valueOf(50), 10,
                            BigDecimal.valueOf(10), 50
                    ), BigDecimal.valueOf(1000)));
            peter.updateBalance(CurrencyType.EUR,
                    new CurrencyBalance(Map.of(
                            BigDecimal.valueOf(100), 10,
                            BigDecimal.valueOf(20), 50
                    ), BigDecimal.valueOf(2000)));

            Cashier linda = new Cashier("LINDA");
            linda.updateBalance(CurrencyType.BGN,
                    new CurrencyBalance(Map.of(
                            BigDecimal.valueOf(50), 10,
                            BigDecimal.valueOf(10), 50
                    ), BigDecimal.valueOf(1000)));
            linda.updateBalance(CurrencyType.EUR,
                    new CurrencyBalance(Map.of(
                            BigDecimal.valueOf(100), 10,
                            BigDecimal.valueOf(20), 50
                    ), BigDecimal.valueOf(2000)));

            cashiers.put("MARTINA", martina);
            cashiers.put("PETER", peter);
            cashiers.put("LINDA", linda);

            persistBalances();
            logger.info("Initialized cashiers with starting balances");
        }
    }

    @Override
    public CurrencyBalanceDTO getBalance(String cashierName, CurrencyType currency, LocalDate dateFrom, LocalDate dateTo) {
        logger.info("Getting balance for cashier={}, currency={}, dateFrom={}, dateTo={}",
                cashierName, currency, dateFrom, dateTo);

        Cashier cashier = cashiers.get(cashierName);
        if (cashier == null) {
            logger.warn("Cashier '{}' not found", cashierName);
            return emptyBalance(currency);
        }

        List<CashOperation> filteredOps = operationHistory.stream()
                .filter(op -> op.cashierName().equals(cashierName))
                .filter(op -> op.currency() == currency)
                .filter(op -> dateFrom == null || !op.operationDate().isBefore(dateFrom))
                .filter(op -> dateTo == null || !op.operationDate().isAfter(dateTo))
                .collect(Collectors.toList());

        CurrencyBalance balanceFromHistory = calculateBalanceFromOperations(filteredOps);
        if (balanceFromHistory != null) {
            List<DenominationDTO> denominations = balanceFromHistory.getDenominations().entrySet().stream()
                    .map(e -> new DenominationDTO(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            return new CurrencyBalanceDTO(currency, balanceFromHistory.getTotal(), denominations);
        }

        logger.warn("No operations found for given filters, returning empty balance");
        return emptyBalance(currency);
    }

    private CurrencyBalance calculateBalanceFromOperations(List<CashOperation> operations) {
        if (operations.isEmpty()) return null;

        Map<BigDecimal, Integer> denominationTotals = new HashMap<>();

        for (CashOperation op : operations) {
            int multiplier = (op.operationType() == OperationType.DEPOSIT) ? 1 : -1;
            for (DenominationDTO denom : op.denominations()) {
                denominationTotals.merge(denom.value(), multiplier * denom.quantity(), Integer::sum);
            }
        }

        denominationTotals.entrySet().removeIf(e -> e.getValue() <= 0);

        BigDecimal total = denominationTotals.entrySet().stream()
                .map(e -> e.getKey().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CurrencyBalance(denominationTotals, total);
    }

    @Override
    public void performCashOperation(CashOperationRequestDTO request) {
        logger.info("Starting cash operation: cashier={}, operationType={}, currency={}",
                request.cashierName(), request.operationType(), request.currency());

        // Validate denominations against allowed values
        DenominationValidator.validate(request.currency(), request.denominations());

        // Perform bean validation
        Set<ConstraintViolation<CashOperationRequestDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new CashOperationException("Validation failed: " + violations.iterator().next().getMessage());
        }

        // Calculate total amount
        BigDecimal total = CashOperationHelper.calculateTotal(request.denominations());

        // Get or create the cashier
        Cashier cashier = cashiers.computeIfAbsent(request.cashierName(), Cashier::new);

        // Apply the operation using the helper
        CurrencyBalance updatedBalance = CashOperationHelper.applyOperation(cashier, request);

        // Update cashier's balance
        cashier.updateBalance(request.currency(), updatedBalance);

        // Create a record of the operation
        CashOperationRecordDTO record = new CashOperationRecordDTO(
                request.cashierName(),
                request.operationType(),
                request.currency(),
                request.denominations(),
                request.operationDate(),
                total
        );

        // Persist history and update in-memory operations
        historyStorage.saveOperationRecord(record);
        operationHistory.add(new CashOperation(
                request.cashierName(),
                request.operationType(),
                request.currency(),
                request.denominations(),
                CashOperationHelper.defaultDate(request.operationDate())
        ));

        persistBalances();
        logger.info("Cash operation completed successfully for cashier {}", request.cashierName());
    }


    private void persistBalances() {
        Map<String, Map<CurrencyType, CurrencyBalance>> toSave = new HashMap<>();
        cashiers.forEach((name, c) -> toSave.put(name, c.getBalances()));
        cashBalanceStorage.saveBalances(toSave);
    }

    private BigDecimal calculateTotal(List<DenominationDTO> denominations) {
        return denominations.stream()
                .map(d -> d.value().multiply(BigDecimal.valueOf(d.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Set<CurrencyType> getSupportedCurrencies() {
        return Set.of(CurrencyType.values());
    }


    @Override
    public Map<CurrencyType, BigDecimal> getTotalBalanceByPeriod(LocalDate fromDate, LocalDate toDate) {
        List<CashOperationRecordDTO> operations = historyStorage.getOperationHistory(null, null, fromDate, toDate);

        Map<CurrencyType, BigDecimal> totals = new HashMap<>();

        for (CashOperationRecordDTO op : operations) {
            BigDecimal signedAmount = op.operationType() == OperationType.WITHDRAWAL
                    ? op.total().negate()
                    : op.total();
            totals.merge(op.currency(), signedAmount, BigDecimal::add);
        }

        return totals;
    }

    @Override
    public Set<BigDecimal> getSupportedDenominations(CurrencyType currency) {
        return DenominationRegistry.getSupportedDenominations(currency);
    }

    @Override
    public List<CashOperationRecordDTO> getOperationHistory(String cashierName, CurrencyType currency, LocalDate dateFrom, LocalDate dateTo) {
        return historyStorage.getOperationHistory(cashierName, currency, dateFrom, dateTo);
    }

    @Override
    public Map<CurrencyType, BigDecimal> getTotalBalances() {
        Map<CurrencyType, BigDecimal> totals = new HashMap<>();

        for (Cashier cashier : cashiers.values()) {
            for (Map.Entry<CurrencyType, CurrencyBalance> entry : cashier.getBalances().entrySet()) {
                totals.merge(
                        entry.getKey(),
                        entry.getValue().getTotal(),
                        BigDecimal::add
                );
            }
        }

        return totals;
    }


    private CurrencyBalanceDTO emptyBalance(CurrencyType currency) {
        return new CurrencyBalanceDTO(currency, BigDecimal.ZERO, List.of());
    }

}
