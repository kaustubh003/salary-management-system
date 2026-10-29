package com.acme.salary.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SalaryCalculationTests {

    private Salary sample() {
        return new Salary(LocalDate.of(2026, 1, 1), null, "INR",
                new BigDecimal("60000.00"), new BigDecimal("24000.00"), new BigDecimal("6000.00"),
                new BigDecimal("9000.00"), new BigDecimal("7200.00"));
    }

    @Test
    void grossIsSumOfBasicHraAndDa() {
        assertEquals(0, new BigDecimal("90000.00").compareTo(sample().getGrossSalary()));
    }

    @Test
    void netIsGrossMinusTaxAndEpf() {
        assertEquals(0, new BigDecimal("73800.00").compareTo(sample().getNetSalary()));
    }

    @Test
    void changingAComponentRecalculatesNet() {
        Salary s = sample();
        s.setBasicSalary(new BigDecimal("70000.00"));
        assertEquals(0, new BigDecimal("100000.00").compareTo(s.getGrossSalary()));
        assertEquals(0, new BigDecimal("83800.00").compareTo(s.getNetSalary()));
    }

    @Test
    void negativeAmountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Salary(LocalDate.of(2026, 1, 1), null, "INR",
                new BigDecimal("-1"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void nullAmountIsRejected() {
        assertThrows(NullPointerException.class, () -> new Salary(LocalDate.of(2026, 1, 1), null, "INR",
                null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void lowercaseCurrencyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Salary(LocalDate.of(2026, 1, 1), null, "inr",
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void unknownIsoCurrencyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Salary(LocalDate.of(2026, 1, 1), null, "ABC",
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }
}