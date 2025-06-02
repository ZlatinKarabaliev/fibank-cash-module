package bg.fibank.cashdesk.common.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public enum BgnDenomination {
    BGN_0_01(new BigDecimal("0.01")),
    BGN_0_02(new BigDecimal("0.02")),
    BGN_0_05(new BigDecimal("0.05")),
    BGN_0_10(new BigDecimal("0.10")),
    BGN_0_20(new BigDecimal("0.20")),
    BGN_0_50(new BigDecimal("0.50")),
    BGN_1(new BigDecimal("1.00")),
    BGN_2(new BigDecimal("2.00")),
    BGN_5(new BigDecimal("5.00")),
    BGN_10(new BigDecimal("10.00")),
    BGN_20(new BigDecimal("20.00")),
    BGN_50(new BigDecimal("50.00")),
    BGN_100(new BigDecimal("100.00"));

    private final BigDecimal value;

    BgnDenomination(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public static Optional<BgnDenomination> fromValue(BigDecimal value) {
        return Arrays.stream(values())
                .filter(d -> d.value.compareTo(value) == 0)
                .findFirst();
    }

    public static boolean isSupported(BigDecimal value) {
        return fromValue(value).isPresent();
    }
}
