package com.company.asset.web.health;

import com.company.asset.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("ok");
    }
}
