package com.company.asset.web.auth;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.security.jwt.JwtProvider;
import com.company.asset.web.auth.dto.LoginRequest;
import com.company.asset.web.auth.dto.MeResponse;
import com.company.asset.web.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        String access = jwtProvider.createAccessToken(
                principal.getUserId(), principal.getUsername(), principal.getRoleName()
        );
        String refresh = jwtProvider.createRefreshToken(
                principal.getUserId(), principal.getUsername(), principal.getRoleName()
        );

        return ApiResponse.ok(new TokenResponse(access, refresh));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@org.springframework.security.core.annotation.AuthenticationPrincipal
                                      com.company.asset.security.auth.CustomUserDetails principal) {
        return ApiResponse.ok(new MeResponse(
                principal.getUserId(),
                principal.getUsername(),
                // CustomUserDetails에 name이 없으니 간단히 email로 확인하거나,
                // 다음 단계에서 User 조회로 name 넣어줄게.
                principal.getUsername(),
                principal.getRoleName()
        ));
    }

}
