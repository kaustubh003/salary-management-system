package com.acme.salary.dto;

import com.acme.salary.enums.LeaveType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(name = "AvailableLeaves",
        description = "Schema to hold available leaves information"
)
@Data
public class AvailableLeavesDto {

    @Schema(description = "Unique identifier for the available leaves record", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Code of the employee", accessMode = Schema.AccessMode.READ_ONLY)
    private String employeeCode;

    @NotNull(message = "Leave type is required")
    @Schema(description = "Type of leave")
    private LeaveType leaveType;

    @NotNull(message = "Available leaves is required")
    @Schema(description = "Number of available leaves")
    private int availableLeaves;

}
