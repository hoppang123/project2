package com.company.asset.web.extension.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ExtensionRejectRequest {
    @NotBlank
    private String adminNote;
}
