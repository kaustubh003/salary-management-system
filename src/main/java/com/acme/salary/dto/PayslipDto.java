package com.acme.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(name = "Payslip",
        description = "Schema to hold information about a stored payslip file"
)
@Data
public class PayslipDto {

    @Schema(description = "ID of the payment this payslip is for")
    private Long paymentId;

    @Schema(description = "Employee code. The file is stored in the folder of the same name")
    private String employeeCode;

    @Schema(description = "Name of the stored PDF file", example = "payslip-1.pdf")
    private String fileName;

    @Schema(description = "When the file was last written")
    private LocalDateTime generatedAt;

}
