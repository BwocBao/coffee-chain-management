package com.coffeechain.service;

import com.coffeechain.config.ApiConfig;
import com.coffeechain.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Client gọi nhóm API xác thực: login, logout, thông tin user hiện tại và kiểm tra quyền. Được dùng
 * chủ yếu bởi LoginFrame, menu chính và các helper phân quyền.
 */
public class AuthApiClient extends ApiClientSupport {
  public LoginResponse login(String tenDangNhap, String matKhau)
      throws IOException, InterruptedException {
    LoginRequest loginRequest = new LoginRequest(tenDangNhap, matKhau);
    String json = toJson(loginRequest);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.LOGIN_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);

    BaseResponse<LoginResponse> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<LoginResponse>>() {});

    return extractData(baseResponse);
  }

  public ForgotPasswordResponse forgotPassword(String email)
      throws IOException, InterruptedException {
    ForgotPasswordRequest requestBody = new ForgotPasswordRequest(email);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.FORGOT_PASSWORD_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<ForgotPasswordResponse> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<ForgotPasswordResponse>>() {});
    return extractData(baseResponse);
  }

  public void verifyResetCode(String email, String code) throws IOException, InterruptedException {
    VerifyResetCodeRequest requestBody = new VerifyResetCodeRequest(email, code);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.VERIFY_RESET_CODE_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<Void> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<Void>>() {});
    if (!baseResponse.isSuccess()) {
      throw new IOException(baseResponse.getMessage());
    }
  }

  public void resetPassword(String email, String code, String newPassword)
      throws IOException, InterruptedException {
    ResetPasswordRequest requestBody = new ResetPasswordRequest(email, code, newPassword);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.RESET_PASSWORD_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);
    BaseResponse<Void> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<Void>>() {});
    if (!baseResponse.isSuccess()) {
      throw new IOException(baseResponse.getMessage());
    }
  }

  public UserInfoResponse getMe() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.ME_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);

    BaseResponse<UserInfoResponse> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<UserInfoResponse>>() {});

    return extractData(baseResponse);
  }

  public Set<String> getPermissions() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.PERMISSIONS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);

    BaseResponse<Set<String>> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<Set<String>>>() {});

    return extractData(baseResponse);
  }

  public PermissionCheckResponse checkPermission(String permission)
      throws IOException, InterruptedException {
    String url =
        ApiConfig.CHECK_PERMISSION_URL
            + "?permission="
            + URLEncoder.encode(permission, StandardCharsets.UTF_8);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);

    BaseResponse<PermissionCheckResponse> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<PermissionCheckResponse>>() {});

    return extractData(baseResponse);
  }

  public void logout() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.LOGOUT_URL))
            .header("Authorization", bearerToken())
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

    HttpResponse<String> response = send(request);

    BaseResponse<Void> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<Void>>() {});

    if (!baseResponse.isSuccess()) {
      throw new IOException(baseResponse.getMessage());
    }
  }

  public static class ForgotPasswordResponse {
    private String maskedEmail;
    private java.time.LocalDateTime expiresAt;
    private String debugCode;

    public String getMaskedEmail() {
      return maskedEmail;
    }

    public void setMaskedEmail(String maskedEmail) {
      this.maskedEmail = maskedEmail;
    }

    public java.time.LocalDateTime getExpiresAt() {
      return expiresAt;
    }

    public void setExpiresAt(java.time.LocalDateTime expiresAt) {
      this.expiresAt = expiresAt;
    }

    public String getDebugCode() {
      return debugCode;
    }

    public void setDebugCode(String debugCode) {
      this.debugCode = debugCode;
    }
  }

  private record ForgotPasswordRequest(String email) {}

  private record VerifyResetCodeRequest(String email, String code) {}

  private record ResetPasswordRequest(String email, String code, String newPassword) {}
}
