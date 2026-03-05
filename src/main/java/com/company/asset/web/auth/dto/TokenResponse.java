package com.company.asset.web.auth.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}
