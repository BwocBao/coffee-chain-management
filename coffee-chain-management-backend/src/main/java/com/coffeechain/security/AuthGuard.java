package com.coffeechain.security;

import com.coffeechain.exception.AppException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthGuard {
  private final TokenStore tokenStore;

  public AuthGuard(TokenStore tokenStore) {
    this.tokenStore = tokenStore;
  }

  public SessionUser requireLogin(String authorizationHeader) {
    String token = extractToken(authorizationHeader);
    SessionUser user = tokenStore.getSession(token);

    if (user == null) {
      throw new AppException(
          HttpStatus.UNAUTHORIZED, "Chưa đăng nhập hoặc phiên đăng nhập đã hết hạn");
    }

    return user;
  }

  public SessionUser requirePermission(String authorizationHeader, String permission) {
    SessionUser user = requireLogin(authorizationHeader);

    if (permission == null || permission.isBlank()) {
      throw new AppException(HttpStatus.FORBIDDEN, "Chưa khai báo quyền truy cập");
    }

    if (!user.hasPermission(permission)) {
      throw new AppException(HttpStatus.FORBIDDEN, "Không có quyền: " + permission);
    }

    return user;
  }

  public SessionUser requireAnyPermission(String authorizationHeader, String... permissions) {
    SessionUser user = requireLogin(authorizationHeader);

    if (permissions == null || permissions.length == 0) {
      throw new AppException(HttpStatus.FORBIDDEN, "Chưa khai báo quyền truy cập");
    }

    boolean allowed =
        Arrays.stream(permissions)
            .filter(p -> p != null && !p.isBlank())
            .anyMatch(user::hasPermission);

    if (!allowed) {
      throw new AppException(HttpStatus.FORBIDDEN, "Không có quyền phù hợp");
    }

    return user;
  }

  public SessionUser requireRole(String authorizationHeader, String... roleNames) {
    SessionUser user = requireLogin(authorizationHeader);

    if (roleNames == null || roleNames.length == 0) {
      throw new AppException(HttpStatus.FORBIDDEN, "Chưa khai báo vai trò truy cập");
    }

    String currentRole = user.getTenVaiTro();

    boolean allowed =
        currentRole != null
            && Arrays.stream(roleNames)
                .filter(role -> role != null && !role.isBlank())
                .anyMatch(role -> role.equalsIgnoreCase(currentRole));

    if (!allowed) {
      throw new AppException(
          HttpStatus.FORBIDDEN, "Vai trò không được phép truy cập chức năng này");
    }

    return user;
  }

  public void requireSameBranchOrAdmin(SessionUser user, Long maChiNhanh) {
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
    }

    if ("ADMIN".equalsIgnoreCase(user.getTenVaiTro())) {
      return;
    }

    if (maChiNhanh == null
        || user.getMaChiNhanh() == null
        || !user.getMaChiNhanh().equals(maChiNhanh)) {
      throw new AppException(HttpStatus.FORBIDDEN, "Không được thao tác dữ liệu chi nhánh khác");
    }
  }

  public String extractToken(String authorizationHeader) {
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Thiếu Authorization token");
    }

    String header = authorizationHeader.trim();

    if (!header.toLowerCase().startsWith("bearer ")) {
      throw new AppException(
          HttpStatus.UNAUTHORIZED, "Authorization header phải có dạng Bearer <token>");
    }

    String token = header.substring("Bearer ".length()).trim();

    if (token.isBlank()) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ");
    }

    return token;
  }
}
