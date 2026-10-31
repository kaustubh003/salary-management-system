package com.acme.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(name = "LoginRequest",
        description = "Schema to hold login credentials"
)
@Data
public class LoginRequestDto {

    @NotEmpty(message = "Username cannot be null or empty.")
    @Schema(description = "HR manager username", example = "hr.manager")
    private String username;

    @NotEmpty(message = "Password cannot be null or empty.")
    @Schema(description = "HR manager password", format = "password")
    private String password;

}