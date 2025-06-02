package bg.fibank.cashdesk.common.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public enum EuroDenomination {
    EUR_0_01(new BigDecimal("0.01")),
    EUR_0_02(new BigDecimal("0.02")),
    EUR_0_05(new BigDecimal("0.05")),
    EUR_0_10(new BigDecimal("0.10")),
    EUR_0_20(new BigDecimal("0.20")),
    EUR_0_50(new BigDecimal("0.50")),
    EUR_1(new BigDecimal("1.00")),
    EUR_2(new BigDecimal("2.00")),
    EUR_5(new BigDecimal("5.00")),
    EUR_10(new BigDecimal("10.00")),
    EUR_20(new BigDecimal("20.00")),
    EUR_50(new BigDecimal("50.00")),
    EUR_100(new BigDecimal("100.00")),
    EUR_200(new BigDecimal("200.00")),
    EUR_500(new BigDecimal("500.00"));

    private final BigDecimal value;

    EuroDenomination(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public static Optional<EuroDenomination> fromValue(BigDecimal value) {
        return Arrays.stream(values())
                .filter(d -> d.value.compareTo(value) == 0)
                .findFirst();
    }

    public static boolean isSupported(BigDecimal value) {
        return fromValue(value).isPresent();
    }
}
