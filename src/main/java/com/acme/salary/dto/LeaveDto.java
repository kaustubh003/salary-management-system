package com.acme.salary.dto;

import com.acme.salary.enums.LeaveType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Schema(name = "Leave",
        description = "Schema to hold Leave information"
)
@Data
public class LeaveDto {

    @Schema(description = "System generated leave ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long leaveId;

    @Schema(description = "Employee code, taken from the URL", accessMode = Schema.AccessMode.READ_ONLY)
    private String employeeCode;

    @NotNull(message = "Leave date cannot be null.")
    @Schema(description = "Date of the leave. Saturdays and Sundays are holidays and are rejected",
            example = "2026-11-04")
    private LocalDate leaveDate;

    @NotNull(message = "Leave type cannot be null.")
    @Schema(description = "Type of leave", example = "CASUAL")
    private LeaveType leaveType;

}
