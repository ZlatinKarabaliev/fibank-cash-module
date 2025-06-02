package bg.fibank.cashdesk.controller;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.dto.CashOperationRecordDTO;
import bg.fibank.cashdesk.service.api.CashBalanceService;
import bg.fibank.cashdesk.storage.CashHistoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TotalBalanceByPeriodController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TotalBalanceByPeriodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashBalanceService cashBalanceService;

    @MockBean
    private CashHistoryStorage cashHistoryStorage;

    private CashOperationRecordDTO operationBGN;
    private CashOperationRecordDTO operationEUR;

    private static final String BASE_URL = "/api/v1/cash/balance/total";

    @BeforeEach
    void setup() {
        operationBGN = new CashOperationRecordDTO("MARTINA", null, CurrencyType.BGN, List.of(), LocalDate.of(2025, 5, 10), BigDecimal.valueOf(100));
        operationEUR = new CashOperationRecordDTO("PETER", null, CurrencyType.EUR, List.of(), LocalDate.of(2025, 5, 15), BigDecimal.valueOf(200));
    }

    @Test
    void testGetTotalBalanceByPeriod() throws Exception {
        when(cashBalanceService.getTotalBalanceByPeriod(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31)))
                .thenReturn(Map.of(
                        CurrencyType.BGN, BigDecimal.valueOf(100),
                        CurrencyType.EUR, BigDecimal.valueOf(200)));

        mockMvc.perform(get(BASE_URL)
                        .param("fromDate", "2025-05-01")
                        .param("toDate", "2025-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BGN").value(100))
                .andExpect(jsonPath("$.EUR").value(200));
    }

    @Test
    void testGetTotalBalanceByPeriod_NoOperations() throws Exception {
        when(cashBalanceService.getTotalBalanceByPeriod(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31)))
                .thenReturn(Map.of());

        mockMvc.perform(get(BASE_URL)
                        .param("fromDate", "2025-05-01")
                        .param("toDate", "2025-05-31"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void testGetTotalBalanceByPeriod_OnlyFromDate() throws Exception {
        when(cashBalanceService.getTotalBalanceByPeriod(LocalDate.of(2025, 5, 1), null))
                .thenReturn(Map.of(
                        CurrencyType.BGN, BigDecimal.valueOf(100)));

        mockMvc.perform(get(BASE_URL)
                        .param("fromDate", "2025-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BGN").value(100));
    }

    @Test
    void testGetTotalBalanceByPeriod_OnlyToDate() throws Exception {
        when(cashBalanceService.getTotalBalanceByPeriod(null, LocalDate.of(2025, 5, 31)))
                .thenReturn(Map.of(
                        CurrencyType.EUR, BigDecimal.valueOf(200)));

        mockMvc.perform(get(BASE_URL)
                        .param("toDate", "2025-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EUR").value(200));
    }
}
