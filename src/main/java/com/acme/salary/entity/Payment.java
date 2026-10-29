package com.acme.salary.entity;

import com.acme.salary.enums.PaidStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Immutable record of a payment. It stores IDs and a snapshot of the salary
 * as it was when the payment was made, and it never reads the Salary table.
 */
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // Payment details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private PaidStatus paidStatus;

    @Column(nullable = false, updatable = false)
    private LocalDate paidDate;

    @Column(nullable = false, updatable = false)
    private LocalTime paidTime;

    protected Payment() {
        // required by JPA
    }

    private Payment(Salary salary, LocalDate periodStart, LocalDate periodEnd, LocalDate paidDate, LocalTime paidTime) {
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
        this.netSalary = salary.getNetSalary();

        this.paidStatus = PaidStatus.PAID;
        this.paidDate = paidDate;
        this.paidTime = paidTime;
    }

    /**
     * Records a payment for a pay period using the salary's current values.
     * The salary is read only here. Later changes to it do not affect this record.
     */
    public static Payment record(Salary salary, LocalDate periodStart, LocalDate periodEnd, LocalDate paidDate, LocalTime paidTime) {
        Objects.requireNonNull(salary, "salary is required");
        Objects.requireNonNull(periodStart, "periodStart is required");
        Objects.requireNonNull(periodEnd, "periodEnd is required");
        Objects.requireNonNull(paidDate, "paidDate is required");
        Objects.requireNonNull(paidTime, "paidTime is required");

        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("periodEnd cannot be before periodStart");
        }
        if (periodStart.isBefore(salary.getEffectiveFrom())) {
            throw new IllegalArgumentException("Pay period cannot start before the salary's effective date");
        }
        return new Payment(salary, periodStart, periodEnd, paidDate, paidTime);
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public Long getSalaryId() {
        return salaryId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public BigDecimal getDa() {
        return da;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getEpf() {
        return epf;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public PaidStatus getPaidStatus() {
        return paidStatus;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public LocalTime getPaidTime() {
        return paidTime;
    }
}