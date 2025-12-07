package com.example.demo.config;

import com.example.demo.dto.ApiResponse;
import com.example.demo.exception.TokenExpiredException;
import com.example.demo.exception.TokenInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 Token 过期异常 - 返回 HTTP 401
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpiredException(TokenExpiredException e) {
        ApiResponse<Void> response = ApiResponse.error(401, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理 Token 无效异常 - 返回 HTTP 401
     */
    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenInvalidException(TokenInvalidException e) {
        ApiResponse<Void> response = ApiResponse.error(401, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理 JWT 过期异常（JJWT 库抛出的）- 返回 HTTP 401
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
        ApiResponse<Void> response = ApiResponse.error(401, "Token 已过期，请重新登录");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理缺少请求头异常（如缺少 Authorization）- 返回 HTTP 401
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException e) {
        if ("Authorization".equals(e.getHeaderName())) {
            ApiResponse<Void> response = ApiResponse.error(401, "请先登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        ApiResponse<Void> response = ApiResponse.error(400, "缺少必需的请求头: " + e.getHeaderName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理运行时异常 - 返回 HTTP 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        String errorMsg = e.getMessage() != null ? e.getMessage() : "系统错误";
        ApiResponse<Void> response = ApiResponse.error(errorMsg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理参数验证异常 - 返回 HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiResponse<Void> response = ApiResponse.error(400, errorMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数绑定异常 - 返回 HTTP 400
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiResponse<Void> response = ApiResponse.error(400, errorMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理其他异常 - 返回 HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        e.printStackTrace();
        String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        ApiResponse<Void> response = ApiResponse.error(500, "系统错误：" + errorMsg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

