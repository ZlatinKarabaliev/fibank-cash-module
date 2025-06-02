package bg.fibank.cashdesk.controller;

import bg.fibank.cashdesk.common.utils.CurrencyType;
import bg.fibank.cashdesk.common.utils.OperationType;
import bg.fibank.cashdesk.dto.CashOperationRequestDTO;
import bg.fibank.cashdesk.dto.DenominationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CashBalanceController.class)
class CashBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private bg.fibank.cashdesk.service.api.CashBalanceService cashBalanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable()) // <== Това изключва CSRF
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    void testPerformCashOperation_successfulDeposit() throws Exception {
        CashOperationRequestDTO request = new CashOperationRequestDTO(
                "LINDA",
                OperationType.DEPOSIT,
                CurrencyType.BGN,
                List.of(new DenominationDTO(BigDecimal.valueOf(50), 1)),
                LocalDate.now()
        );

        doNothing().when(cashBalanceService).performCashOperation(request);

        mockMvc.perform(post("/api/v1/cash/operation")
                        .header("FIB-X-AUTH", "f9Uie8nNf112hx8s")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testPerformCashOperation_invalidDenomination() throws Exception {
        CashOperationRequestDTO request = new CashOperationRequestDTO(
                "LINDA",
                OperationType.DEPOSIT,
                CurrencyType.BGN,
                List.of(new DenominationDTO(BigDecimal.valueOf(123), 1)),
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/cash/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
