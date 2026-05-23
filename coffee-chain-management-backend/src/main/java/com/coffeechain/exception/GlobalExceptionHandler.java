package com.coffeechain.exception;

import com.coffeechain.dto.BaseResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Void>> handleValidationException(
      MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Dữ liệu không hợp lệ");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.fail(400, message));
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<BaseResponse<Void>> handleAppException(AppException e) {
    int statusCode = e.getStatus().value();

    return ResponseEntity.status(e.getStatus()).body(BaseResponse.fail(statusCode, e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
    e.printStackTrace();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(BaseResponse.fail(500, "Lỗi server: " + e.getMessage()));
  }
}
