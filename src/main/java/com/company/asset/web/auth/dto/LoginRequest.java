package com.company.asset.web.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class LoginRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;
}
