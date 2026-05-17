package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Định dạng response chuẩn của backend")
public class BaseResponse<T> {
    @Schema(description = "API xử lý thành công hay thất bại", example = "true")
    private boolean success;

    @Schema(description = "HTTP status code tương ứng", example = "200")
    private int statusCode;

    @Schema(description = "Thông báo ngắn gọn cho frontend hiển thị hoặc debug", example = "Thao tác thành công")
    private String message;

    @Schema(description = "Dữ liệu trả về của API. Có thể null với các API không có payload.")
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(boolean success, int statusCode, String message, T data) {
        this.success = success;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> ok(String message, T data) {
        return new BaseResponse<>(true, 200, message, data);
    }

    public static <T> BaseResponse<T> created(String message, T data) {
        return new BaseResponse<>(true, 201, message, data);
    }

    public static <T> BaseResponse<T> fail(int statusCode, String message) {
        return new BaseResponse<>(false, statusCode, message, null);
    }

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
