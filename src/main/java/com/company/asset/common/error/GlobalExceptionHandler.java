package com.company.asset.common.error;

import com.company.asset.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        var ec = e.getErrorCode();
        return ResponseEntity.status(ec.getStatus()).body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("validation error");
        return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleEtc(Exception e) {
        return ResponseEntity.status(500).body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
