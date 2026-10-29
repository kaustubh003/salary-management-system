package com.acme.salary.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Mutable pay reference for an employee. Changing a component recalculates
 * the net. Payments take a snapshot of this record when they are made.
 */
@Entity
public class Salary {

    private static final Pattern CURRENCY_FORMAT = Pattern.compile("[A-Z]{3}");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_code", nullable = false, updatable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @Column(nullable = false)
    private LocalDate effectiveTo;

    @Column(nullable = false, length = 3)
    private String currency;

    // Allowances
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal hra;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal da;

    // Deductions
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal epf;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    protected Salary() {
        // required by JPA
    }

    public Salary(LocalDate effectiveFrom, LocalDate effectiveTo, String currency, BigDecimal basicSalary, BigDecimal hra, BigDecimal da, BigDecimal tax, BigDecimal epf) {
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
        this.effectiveTo = Objects.requireNonNull(effectiveTo, "effectiveTo is required");
        this.currency = validateCurrency(currency);
        this.basicSalary = requireNonNegative(basicSalary, "basicSalary");
        this.hra = requireNonNegative(hra, "hra");
        this.da = requireNonNegative(da, "da");
        this.tax = requireNonNegative(tax, "tax");
        this.epf = requireNonNegative(epf, "epf");
        recalculateNet();
    }

    /**
     * Net = gross - tax - EPF. Stored, and kept in sync on every change.
     */
    private void recalculateNet() {
        this.netSalary = getGrossSalary().subtract(tax).subtract(epf);
    }

    private static String validateCurrency(String code) {
        if (code == null || !CURRENCY_FORMAT.matcher(code).matches()) {
            throw new IllegalArgumentException("Currency must be a 3-letter uppercase ISO 4217 code");
        }
        try {
            Currency.getInstance(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ISO 4217 currency code: " + code, e);
        }
        return code;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String name) {
        Objects.requireNonNull(value, name + " is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
        return value;
    }

    void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
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

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getEpf() {
        return epf;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    /**
     * Gross = basic + HRA + DA. Computed on demand.
     */
    public BigDecimal getGrossSalary() {
        return basicSalary.add(hra).add(da);
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = Objects.requireNonNull(effectiveTo, "effectiveTo is required");
    }

    public void setCurrency(String currency) {
        this.currency = validateCurrency(currency);
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = requireNonNegative(basicSalary, "basicSalary");
        recalculateNet();
    }

    public void setHra(BigDecimal hra) {
        this.hra = requireNonNegative(hra, "hra");
        recalculateNet();
    }

    public void setDa(BigDecimal da) {
        this.da = requireNonNegative(da, "da");
        recalculateNet();
    }

    public void setTax(BigDecimal tax) {
        this.tax = requireNonNegative(tax, "tax");
        recalculateNet();
    }

    public void setEpf(BigDecimal epf) {
        this.epf = requireNonNegative(epf, "epf");
        recalculateNet();
    }

}