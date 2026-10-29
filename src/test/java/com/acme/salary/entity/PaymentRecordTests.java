package com.acme.salary.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acme.salary.enums.PaidStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class PaymentRecordTests {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TILL = LocalDate.of(2026, 6, 30);

    private Employee employee() {
        return new Employee("EMP01", "Asha", "Rao", "Finance", "Analyst",
                "Pune", "9876543210", "asha@example.com", "India");
    }

    private Salary attachedSalary(LocalDate till) {
        Salary salary = new Salary(FROM, till, "INR",
                new BigDecimal("60000.00"), new BigDecimal("24000.00"), new BigDecimal("6000.00"),
                new BigDecimal("9000.00"), new BigDecimal("7200.00"));
        employee().addSalary(salary);
        return salary;
    }

    private Payment pay(Salary salary, LocalDate start, LocalDate end) {
        return Payment.record(salary, start, end, end, LocalTime.of(10, 30));
    }

    @Test
    void recordCopiesSalaryValuesAndMarksPaid() {
        Salary salary = attachedSalary(TILL);
        Payment payment = pay(salary, FROM, FROM.plusMonths(1).minusDays(1));

        assertEquals("EMP01", payment.getEmployeeCode());
        assertEquals("INR", payment.getCurrency());
        assertEquals(0, new BigDecimal("73800.00").compareTo(payment.getNetSalary()));
        assertEquals(0, new BigDecimal("90000.00").compareTo(payment.getGrossSalary()));
        assertEquals(PaidStatus.PAID, payment.getPaidStatus());
    }

    @Test
    void paymentDoesNotChangeWhenSalaryIsUpdatedLater() {
        Salary salary = attachedSalary(TILL);
        Payment payment = pay(salary, FROM, FROM.plusDays(30));

        salary.setBasicSalary(new BigDecimal("70000.00"));

        assertEquals(0, new BigDecimal("73800.00").compareTo(payment.getNetSalary()));
        assertEquals(0, new BigDecimal("83800.00").compareTo(salary.getNetSalary()));
    }

    @Test
    void periodEndingBeforeItStartsIsRejected() {
        Salary salary = attachedSalary(TILL);
        assertThrows(IllegalArgumentException.class,
                () -> pay(salary, FROM.plusDays(10), FROM.plusDays(5)));
    }

    @Test
    void periodStartingBeforeSalaryIsRejected() {
        Salary salary = attachedSalary(TILL);
        assertThrows(IllegalArgumentException.class,
                () -> pay(salary, FROM.minusDays(1), FROM.plusDays(10)));
    }

    @Test
    void periodEndingAfterBoundedSalaryIsRejected() {
        Salary salary = attachedSalary(TILL);
        assertThrows(IllegalArgumentException.class,
                () -> pay(salary, TILL.minusDays(10), TILL.plusDays(1)));
    }

    @Test
    void periodMayUseBothBoundaryDatesOfBoundedSalary() {
        Salary salary = attachedSalary(TILL);
        Payment payment = pay(salary, FROM, TILL);
        assertEquals(FROM, payment.getPeriodStart());
        assertEquals(TILL, payment.getPeriodEnd());
    }

    @Test
    void openEndedSalaryAcceptsLaterPeriods() {
        Salary salary = attachedSalary(null);
        Payment payment = pay(salary, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31));
        assertEquals(PaidStatus.PAID, payment.getPaidStatus());
    }
}