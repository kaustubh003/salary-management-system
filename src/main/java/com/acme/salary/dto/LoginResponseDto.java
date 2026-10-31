package com.acme.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(name = "LoginResponse",
        description = "Schema to hold the session token returned after login"
)
@Data
@AllArgsConstructor
public class LoginResponseDto {

    @Schema(description = "Session token. Send it as 'Authorization: Bearer <token>' on later requests")
    private String token;

}