package com.coffeechain.service;

import com.coffeechain.config.ApiConfig;
import com.coffeechain.dto.BaseResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UserApiClient extends ApiClientSupport {

  public UserLookupResponse getCreateUserLookups() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.USER_LOOKUPS_URL))
            .header("Authorization", bearerToken())
            .GET()
            .build();

    HttpResponse<String> response = send(request);

    BaseResponse<UserLookupResponse> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<UserLookupResponse>>() {});

    return extractData(baseResponse);
  }

  public CreateUserResponse createUser(CreateUserRequest requestBody)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.USERS_URL))
            .header("Authorization", bearerToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(toJson(requestBody), StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response = send(request);

    BaseResponse<CreateUserResponse> baseResponse =
        readBaseResponse(response, new TypeReference<BaseResponse<CreateUserResponse>>() {});

    return extractData(baseResponse);
  }

  public static class UserLookupResponse {
    private List<OptionDto> roles = new ArrayList<>();
    private List<OptionDto> branches = new ArrayList<>();

    public List<OptionDto> getRoles() {
      return roles;
    }

    public void setRoles(List<OptionDto> roles) {
      this.roles = roles;
    }

    public List<OptionDto> getBranches() {
      return branches;
    }

    public void setBranches(List<OptionDto> branches) {
      this.branches = branches;
    }
  }

  public static class OptionDto {
    private Long id;
    private String name;
    private String description;

    public OptionDto() {}

    public OptionDto(Long id, String name, String description) {
      this.id = id;
      this.name = name;
      this.description = description;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name == null ? "" : name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  public static class CreateUserRequest {
    private String tenDangNhap;
    private String matKhau;
    private String email;
    private String tenVaiTro;
    private Long maChiNhanh;

    public String getTenDangNhap() {
      return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
      this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
      return matKhau;
    }

    public void setMatKhau(String matKhau) {
      this.matKhau = matKhau;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getTenVaiTro() {
      return tenVaiTro;
    }

    public void setTenVaiTro(String tenVaiTro) {
      this.tenVaiTro = tenVaiTro;
    }

    public Long getMaChiNhanh() {
      return maChiNhanh;
    }

    public void setMaChiNhanh(Long maChiNhanh) {
      this.maChiNhanh = maChiNhanh;
    }
  }

  public static class CreateUserResponse {
    private String tenDangNhap;
    private String tenVaiTro;
    private Long maChiNhanh;

    public String getTenDangNhap() {
      return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
      this.tenDangNhap = tenDangNhap;
    }

    public String getTenVaiTro() {
      return tenVaiTro;
    }

    public void setTenVaiTro(String tenVaiTro) {
      this.tenVaiTro = tenVaiTro;
    }

    public Long getMaChiNhanh() {
      return maChiNhanh;
    }

    public void setMaChiNhanh(Long maChiNhanh) {
      this.maChiNhanh = maChiNhanh;
    }
  }
}
