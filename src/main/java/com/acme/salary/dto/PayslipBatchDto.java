package com.acme.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(name = "PayslipBatch",
        description = "Schema to hold the result of generating payslips in bulk"
)
@Data
@AllArgsConstructor
public class PayslipBatchDto {

    @Schema(description = "Number of payslips generated in this run")
    private int generated;

    @Schema(description = "Number of payments skipped because they already had a payslip")
    private int skipped;

}
