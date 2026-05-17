package com.coffeechain.dto;

/**
 * Data nhận về sau khi đăng nhập thành công.
 * Token được lưu vào SessionManager, user dùng để render menu theo quyền.
 */
public class LoginResponse {
    private String token;
    private UserInfoResponse user;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserInfoResponse getUser() { return user; }
    public void setUser(UserInfoResponse user) { this.user = user; }
}
