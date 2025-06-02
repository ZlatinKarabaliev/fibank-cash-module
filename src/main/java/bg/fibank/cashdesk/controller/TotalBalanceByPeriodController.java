package bg.fibank.cashdesk.controller;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.service.api.CashBalanceService;
import bg.fibank.cashdesk.storage.CashHistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cash")
public class TotalBalanceByPeriodController {


    private static final Logger logger = LoggerFactory.getLogger(TotalBalanceByPeriodController.class);
    private final CashBalanceService cashBalanceService;
    private final CashHistoryStorage cashHistoryStorage;

    public TotalBalanceByPeriodController(CashBalanceService cashBalanceService, CashHistoryStorage cashHistoryStorage) {
        this.cashBalanceService = cashBalanceService;
        this.cashHistoryStorage = cashHistoryStorage;
    }

    @GetMapping("/balance/total")
    public Map<CurrencyType, BigDecimal> getTotalBalanceByPeriod(
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        logger.info("Loading totals fromDate {} toDate {}", fromDate, toDate);
        return cashBalanceService.getTotalBalanceByPeriod(fromDate, toDate);


    }
}
