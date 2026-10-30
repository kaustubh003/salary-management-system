package com.acme.salary.dto;

import com.acme.salary.enums.PaidStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "Payment",
        description = "Schema to hold Payment information"
)
@Data
public class PaymentDto {

    @Schema(description = "System generated payment ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long paymentID;

    @Schema(description = "Employee code, taken from the salary being paid", accessMode = Schema.AccessMode.READ_ONLY)
    private String employeeCode;

    @NotNull(message = "Salary ID cannot be null.")
    @Schema(description = "ID of the salary this payment is made under")
    private Long salaryId;

    @NotNull(message = "Period start date cannot be null.")
    @Schema(description = "First day of the pay period (inclusive)", example = "2026-01-01")
    private LocalDate periodStart;

    @NotNull(message = "Period end date cannot be null.")
    @Schema(description = "Last day of the pay period (inclusive)", example = "2026-01-31")
    private LocalDate periodEnd;

    @Schema(description = "Date the payment was made, set by the system", example = "2026-01-31",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate paidDate;

    @Schema(description = "Time the payment was made, set by the system", example = "10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalTime paidTime;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private PaidStatus paidStatus;

    // Snapshot of the salary taken when the payment was recorded
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String currency;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal basicSalary;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal hra;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal da;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal grossSalary;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal tax;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal epf;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal netSalary;

}