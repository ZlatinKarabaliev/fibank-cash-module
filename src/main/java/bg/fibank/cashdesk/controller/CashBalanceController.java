package bg.fibank.cashdesk.controller;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.CurrencyBalanceDTO;
import bg.fibank.cashdesk.dto.CurrencyDenominationsDTO;
import bg.fibank.cashdesk.service.api.CashBalanceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * REST controller for managing cash operations and querying balances.
 */
@RestController
@RequestMapping("/api/v1/cash")
public class CashBalanceController {

    private static final Logger logger = LoggerFactory.getLogger(CashBalanceController.class);

    private final CashBalanceService cashBalanceService;

    public CashBalanceController(CashBalanceService cashBalanceService) {
        this.cashBalanceService = cashBalanceService;
    }

    @PostMapping("/operation")
    public ResponseEntity<Void> performOperation(@RequestBody @Valid CashOperationRequestDTO request) {
        logger.info("Received cash operation request: cashier={}, operationType={}, currency={}",
                request.cashierName(), request.operationType(), request.currency());
        cashBalanceService.performCashOperation(request);
        logger.info("Successfully performed operation for cashier={}", request.cashierName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<CurrencyBalanceDTO> getBalance(
            @RequestParam(value = "cashierName", required = false) String cashierName,
            @RequestParam(value = "currency", required = false) CurrencyType currency,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        if (currency == null) {
            return ResponseEntity.badRequest().body(null); // или ErrorResponse
        }
        CurrencyBalanceDTO balance = cashBalanceService.getBalance(cashierName, currency, dateFrom, dateTo);
        logger.info("Returning balance for cashier={}, currency={}, total={}",
                cashierName, currency, balance.total());
        return ResponseEntity.ok(balance);
    }


    @GetMapping("/currencies")
    public ResponseEntity<Set<CurrencyType>> getSupportedCurrencies() {
        logger.info("Request for supported currencies received");
        Set<CurrencyType> currencies = cashBalanceService.getSupportedCurrencies();
        logger.info("Returning supported currencies: {}", currencies);
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/denominations")
    public ResponseEntity<CurrencyDenominationsDTO> getSupportedDenominations(@RequestParam("currency") CurrencyType currency) {
        logger.info("Request for supported denominations received for currency={}", currency);
        Set<BigDecimal> denominations = cashBalanceService.getSupportedDenominations(currency);
        logger.info("Returning currency,denominations: {}{}", currency,denominations);
        return ResponseEntity.ok(new CurrencyDenominationsDTO(currency, denominations));
    }

    @GetMapping("/history")
    public ResponseEntity<List<CashOperationRecordDTO>> getHistory(
            @RequestParam("cashierName") String cashierName,
            @RequestParam("currency") CurrencyType currency,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        logger.info("Received history request: cashierName={}, currency={}, dateFrom={}, dateTo={}",
                cashierName, currency, dateFrom, dateTo);

        if (dateFrom == null) {
            dateFrom = LocalDate.now().minusMonths(1);
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }
        List<CashOperationRecordDTO> history = cashBalanceService.getOperationHistory(
                cashierName, currency, dateFrom, dateTo);

        logger.info("Returning {} history records", history.size());
        return ResponseEntity.ok(history);
    }


}
