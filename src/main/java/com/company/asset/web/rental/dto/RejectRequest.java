package com.company.asset.web.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RejectRequest {
    @NotBlank
    private String reason;
}
