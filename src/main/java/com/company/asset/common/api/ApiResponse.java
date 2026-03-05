package com.company.asset.common.api;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "success", data);
    }
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "OK", "success", null);
    }
    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
