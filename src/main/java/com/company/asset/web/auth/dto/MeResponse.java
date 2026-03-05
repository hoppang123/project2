package com.company.asset.web.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
}
