package com.acme.salary.entity;

import com.acme.salary.enums.PaidStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Immutable record of a payment. It stores IDs and a snapshot of the salary
 * as it was when the payment was made, and it never reads the Salary table.
 */
@Getter
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentID;

    @Column(nullable = false, updatable = false)
    private String employeeCode;

    @Column(nullable = false, updatable = false)
    private Long salaryId;

    @Column(nullable = false, updatable = false)
    private LocalDate periodStart;

    @Column(nullable = false, updatable = false)
    private LocalDate periodEnd;

    // Snapshot of the salary at payment time
    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal hra;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal da;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal grossSalary;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal tax;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal epf;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal netSalary;

    // netSalary above is the amount actually paid: the salary's net minus leaveDeduction
    @ColumnDefault("0")
    @Column(nullable = false, updatable = false)
    private int unpaidLeaveDays;

    @ColumnDefault("0")
    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal leaveDeduction;

    // Payment details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaidStatus paidStatus = PaidStatus.UNPAID;

    @Column(nullable = false, updatable = false)
    private LocalDate paidDate;

    @Column(nullable = false, updatable = false)
    private LocalTime paidTime;

    protected Payment() {
        // required by JPA
    }

    private Payment(Salary salary, LocalDate periodStart, LocalDate periodEnd, LocalDate paidDate,
                    LocalTime paidTime, int unpaidLeaveDays) {
        this.employeeCode = salary.getEmployee().getEmployeeCode();
        this.salaryId = salary.getId();
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;

        this.currency = salary.getCurrency();
        this.basicSalary = salary.getBasicSalary();
        this.hra = salary.getHra();
        this.da = salary.getDa();
        this.grossSalary = salary.getGrossSalary();
        this.tax = salary.getTax();
        this.epf = salary.getEpf();

        this.unpaidLeaveDays = unpaidLeaveDays;
        this.leaveDeduction = calculateLeaveDeduction(salary, periodStart, periodEnd, unpaidLeaveDays);
        this.netSalary = salary.getNetSalary().subtract(this.leaveDeduction);

        this.paidStatus = PaidStatus.PAID;
        this.paidDate = paidDate;
        this.paidTime = paidTime;
    }

    /**
     * Records a payment with no unpaid leave.
     */
    public static Payment record(Salary salary, LocalDate periodStart, LocalDate periodEnd,
                                 LocalDate paidDate, LocalTime paidTime) {
        return record(salary, periodStart, periodEnd, paidDate, paidTime, 0);
    }

    /**
     * Records a payment for a pay period using the salary's current values.
     * The pay period must fall entirely within the salary's effective range.
     * Pay is reduced for the given number of unpaid leave days.
     */
    public static Payment record(Salary salary, LocalDate periodStart, LocalDate periodEnd,
                                 LocalDate paidDate, LocalTime paidTime, int unpaidLeaveDays) {
        Objects.requireNonNull(salary, "salary is required");
        Objects.requireNonNull(periodStart, "periodStart is required");
        Objects.requireNonNull(periodEnd, "periodEnd is required");
        Objects.requireNonNull(paidDate, "paidDate is required");
        Objects.requireNonNull(paidTime, "paidTime is required");

        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("periodEnd cannot be before periodStart");
        }
        if (!salary.isEffectiveOn(periodStart)) {
            throw new IllegalArgumentException("Pay period starts outside the salary's effective range");
        }
        if (!salary.isEffectiveOn(periodEnd)) {
            throw new IllegalArgumentException("Pay period ends outside the salary's effective range");
        }
        return new Payment(salary, periodStart, periodEnd, paidDate, paidTime, unpaidLeaveDays);
    }

    /**
     * Pay lost to unpaid leave: gross divided by the weekdays in the period, times the unpaid days.
     * Saturdays and Sundays are holidays, so they are not counted. The deduction never exceeds
     * the salary's net, so the amount paid cannot go below zero.
     */
    private static BigDecimal calculateLeaveDeduction(Salary salary, LocalDate periodStart,
                                                      LocalDate periodEnd, int unpaidLeaveDays) {
        if (unpaidLeaveDays < 0) {
            throw new IllegalArgumentException("unpaidLeaveDays cannot be negative");
        }
        if (unpaidLeaveDays == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        long weekdays = weekdaysBetween(periodStart, periodEnd);
        if (unpaidLeaveDays > weekdays) {
            throw new IllegalArgumentException("Unpaid leave days (" + unpaidLeaveDays
                    + ") exceed the weekdays in the pay period (" + weekdays + ")");
        }
        BigDecimal deduction = salary.getGrossSalary()
                .multiply(BigDecimal.valueOf(unpaidLeaveDays))
                .divide(BigDecimal.valueOf(weekdays), 2, RoundingMode.HALF_UP);
        return deduction.min(salary.getNetSalary().max(BigDecimal.ZERO));
    }

    private static long weekdaysBetween(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }

}