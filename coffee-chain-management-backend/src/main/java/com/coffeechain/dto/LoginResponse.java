package com.coffeechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response sau khi đăng nhập thành công")
public class LoginResponse {
    @Schema(description = "Bearer token dùng để gọi các API cần đăng nhập", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    @Schema(description = "Thông tin người dùng, vai trò, chi nhánh và danh sách quyền hiện tại")
    private UserInfoResponse user;

    public LoginResponse() {
    }

    public LoginResponse(String token, UserInfoResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoResponse getUser() {
        return user;
    }

    public void setUser(UserInfoResponse user) {
        this.user = user;
    }
}
