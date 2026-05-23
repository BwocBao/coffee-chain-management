package com.coffeechain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(
    description =
        "DTO response ExpiryRefreshResponse. Swagger hien thi cac field backend tra ve cho frontend.")
public class ExpiryRefreshResponse {
  @Schema(description = "Thong bao ket qua xu ly", example = "Gia tri mau")
  private String message;

  @Schema(
      description = "Gia tri $field trong response tra ve frontend (refreshed at).",
      example = "2026-05-22T08:30:00")
  private LocalDateTime refreshedAt;

  public ExpiryRefreshResponse() {}

  public ExpiryRefreshResponse(String message, LocalDateTime refreshedAt) {
    this.message = message;
    this.refreshedAt = refreshedAt;
  }

  public String getMessage() {
    return message;
  }

  public LocalDateTime getRefreshedAt() {
    return refreshedAt;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setRefreshedAt(LocalDateTime refreshedAt) {
    this.refreshedAt = refreshedAt;
  }
}
