package com.acme.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(name = "Employee",
        description = "Schema to hold Employee information"
)
@Data
public class EmployeeDto {

    @NotEmpty(message = "Employee code cannot be null or empty.")
    @Size(min = 6, max = 6, message = "Employee code should in a valid format.")
    private String employeeCode;

    @NotEmpty(message = "First name cannot be null or empty.")
    private String firstName;

    @NotEmpty(message = "Last name cannot be null or empty.")
    private String lastName;

    @NotEmpty(message = "Department name cannot be null or empty.")
    private String department;

    @NotEmpty(message = "Job title cannot be null or empty.")
    private String jobTitle;

    @NotEmpty(message = "Address cannot be null or empty.")
    private String address;

    @NotEmpty(message = "Phone number cannot be null or empty.")
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Phone number must be 10 digits.")
    private String phone;

    @NotEmpty(message = "Email cannot be null or empty.")
    @Email(message = "Email should be in a valid format.")
    private String email;

    @NotEmpty(message = "Country cannot be null or empty.")
    private String country;

    @Valid
    private SalaryDto salary;

}
