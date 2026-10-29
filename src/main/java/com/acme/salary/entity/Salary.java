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
 * A null effectiveTill means the salary is open-ended (current).
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

    @Column
    private LocalDate effectiveTill;

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

    public Salary(LocalDate effectiveFrom, LocalDate effectiveTill, String currency, BigDecimal basicSalary, BigDecimal hra, BigDecimal da, BigDecimal tax, BigDecimal epf) {
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
        this.effectiveTill = validateRange(effectiveFrom, effectiveTill);
        this.currency = validateCurrency(currency);
        this.basicSalary = requireNonNegative(basicSalary, "basicSalary");
        this.hra = requireNonNegative(hra, "hra");
        this.da = requireNonNegative(da, "da");
        this.tax = requireNonNegative(tax, "tax");
        this.epf = requireNonNegative(epf, "epf");
        recalculateNet();
    }

    /**
     * True if this salary is in force on the given date. Both ends are inclusive.
     * An open-ended salary (null effectiveTill) is in force for any date on or after effectiveFrom.
     */
    public boolean isEffectiveOn(LocalDate date) {
        Objects.requireNonNull(date, "date is required");
        return !date.isBefore(effectiveFrom) && (effectiveTill == null || !date.isAfter(effectiveTill));
    }

    /**
     * True if the whole pay period (start and end) falls within this salary's range.
     */
    public boolean coversPeriod(LocalDate periodStart, LocalDate periodEnd) {
        return isEffectiveOn(periodStart) && isEffectiveOn(periodEnd);
    }

    /**
     * Net = gross - tax - EPF. Stored, and kept in sync on every change.
     */
    private void recalculateNet() {
        this.netSalary = getGrossSalary().subtract(tax).subtract(epf);
    }

    private static LocalDate validateRange(LocalDate from, LocalDate till) {
        if (till != null && till.isBefore(from)) {
            throw new IllegalArgumentException("effectiveTill cannot be before effectiveFrom");
        }
        return till;
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

    public LocalDate getEffectiveTill() {
        return effectiveTill;
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
        Objects.requireNonNull(effectiveFrom, "effectiveFrom is required");
        validateRange(effectiveFrom, this.effectiveTill);
        this.effectiveFrom = effectiveFrom;
    }

    /**
     * Pass null to make the salary open-ended.
     */
    public void setEffectiveTill(LocalDate effectiveTill) {
        this.effectiveTill = validateRange(this.effectiveFrom, effectiveTill);
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