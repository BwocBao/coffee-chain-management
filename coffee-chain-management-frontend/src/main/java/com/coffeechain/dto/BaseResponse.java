package com.coffeechain.dto;

/**
 * Wrapper response chuẩn backend trả về cho mọi API. ApiClientSupport đọc class này trước, sau đó
 * lấy phần data bên trong.
 */
public class BaseResponse<T> {
  private boolean success;
  private int statusCode;
  private String message;
  private T data;

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
