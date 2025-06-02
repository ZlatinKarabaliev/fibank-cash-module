package bg.fibank.cashdesk.service.api;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.CurrencyBalanceDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CashBalanceService {
    CurrencyBalanceDTO getBalance(String cashierName, CurrencyType currency, LocalDate dateFrom, LocalDate dateTo);
    void performCashOperation(CashOperationRequestDTO request);
    Set<CurrencyType> getSupportedCurrencies();

    Map<CurrencyType, BigDecimal> getTotalBalanceByPeriod(LocalDate fromDate, LocalDate toDate);

    Set<java.math.BigDecimal> getSupportedDenominations(CurrencyType currency);
    List<CashOperationRecordDTO> getOperationHistory(String cashierName, CurrencyType currency, LocalDate dateFrom, LocalDate dateTo);
    Map<CurrencyType, BigDecimal> getTotalBalances();

}
