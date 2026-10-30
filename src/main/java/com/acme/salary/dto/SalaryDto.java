package com.acme.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "Salary", description = "Schema to hold Salary information")
@Data
public class SalaryDto {

    @Schema(description = "System generated salary ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Effective from date cannot be null.")
    @Schema(description = "First day the salary is in force (inclusive)", example = "2026-01-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Last day the salary is in force (inclusive). Leave empty for a current, open-ended salary", example = "2026-12-31", nullable = true)
    private LocalDate effectiveTill;

    @NotEmpty(message = "Currency cannot be null or empty.")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter uppercase ISO 4217 code.")
    @Schema(description = "ISO 4217 currency code", example = "INR")
    private String currency;

    @NotNull(message = "Basic salary cannot be null.")
    @PositiveOrZero(message = "Basic salary cannot be negative.")
    @Digits(integer = 13, fraction = 2, message = "Basic salary must have at most 13 digits and 2 decimals.")
    private BigDecimal basicSalary;

    @NotNull(message = "HRA cannot be null.")
    @PositiveOrZero(message = "HRA cannot be negative.")
    @Digits(integer = 13, fraction = 2, message = "HRA must have at most 13 digits and 2 decimals.")
    private BigDecimal hra;

    @NotNull(message = "DA cannot be null.")
    @PositiveOrZero(message = "DA cannot be negative.")
    @Digits(integer = 13, fraction = 2, message = "DA must have at most 13 digits and 2 decimals.")
    private BigDecimal da;

    @NotNull(message = "Tax cannot be null.")
    @PositiveOrZero(message = "Tax cannot be negative.")
    @Digits(integer = 13, fraction = 2, message = "Tax must have at most 13 digits and 2 decimals.")
    private BigDecimal tax;

    @NotNull(message = "EPF cannot be null.")
    @PositiveOrZero(message = "EPF cannot be negative.")
    @Digits(integer = 13, fraction = 2, message = "EPF must have at most 13 digits and 2 decimals.")
    private BigDecimal epf;

    @Schema(description = "Calculated as basic salary + HRA + DA", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal grossSalary;

    @Schema(description = "Calculated as gross salary - tax - EPF", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal netSalary;

}