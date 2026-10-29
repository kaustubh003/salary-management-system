package com.acme.salary.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class SalaryRangeTests {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TILL = LocalDate.of(2026, 6, 30);

    private Salary salary(LocalDate till) {
        return new Salary(FROM, till, "INR", new BigDecimal("1000"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Test
    void boundedSalaryIncludesBothEndDates() {
        Salary s = salary(TILL);
        assertTrue(s.isEffectiveOn(FROM));
        assertTrue(s.isEffectiveOn(TILL));
    }

    @Test
    void boundedSalaryExcludesDaysOutsideRange() {
        Salary s = salary(TILL);
        assertFalse(s.isEffectiveOn(FROM.minusDays(1)));
        assertFalse(s.isEffectiveOn(TILL.plusDays(1)));
    }

    @Test
    void openEndedSalaryIsEffectiveForAnyLaterDate() {
        Salary s = salary(null);
        assertNull(s.getEffectiveTill());
        assertTrue(s.isEffectiveOn(FROM));
        assertTrue(s.isEffectiveOn(LocalDate.of(2040, 12, 31)));
        assertFalse(s.isEffectiveOn(FROM.minusDays(1)));
    }

    @Test
    void coversPeriodRequiresBothEndsInRange() {
        Salary s = salary(TILL);
        assertTrue(s.coversPeriod(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)));
        assertTrue(s.coversPeriod(FROM, TILL));
        assertFalse(s.coversPeriod(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31)));
        assertFalse(s.coversPeriod(LocalDate.of(2025, 12, 1), LocalDate.of(2026, 1, 31)));
    }

    @Test
    void constructorRejectsEndDateBeforeStart() {
        assertThrows(IllegalArgumentException.class, () -> salary(FROM.minusDays(1)));
    }

    @Test
    void setEffectiveTillToNullMakesSalaryOpenEnded() {
        Salary s = salary(TILL);
        s.setEffectiveTill(null);
        assertNull(s.getEffectiveTill());
        assertTrue(s.isEffectiveOn(LocalDate.of(2030, 1, 1)));
    }

    @Test
    void setEffectiveTillRejectsDateBeforeStart() {
        Salary s = salary(null);
        assertThrows(IllegalArgumentException.class, () -> s.setEffectiveTill(FROM.minusDays(1)));
    }

    @Test
    void setEffectiveFromRejectsStartAfterExistingEnd() {
        Salary s = salary(TILL);
        assertThrows(IllegalArgumentException.class, () -> s.setEffectiveFrom(TILL.plusDays(1)));
    }
}