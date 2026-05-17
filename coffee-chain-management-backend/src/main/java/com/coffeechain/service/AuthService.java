package com.coffeechain.service;

import com.coffeechain.dto.LoginRequest;
import com.coffeechain.dto.LoginResponse;
import com.coffeechain.dto.UserInfoResponse;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.NguoiDungRecord;
import com.coffeechain.repository.NguoiDungRepository;
import com.coffeechain.security.PasswordUtil;
import com.coffeechain.security.SessionUser;
import com.coffeechain.security.TokenStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AuthService {
    private final NguoiDungRepository nguoiDungRepository;
    private final TokenStore tokenStore;

    @Value("${app.auth.session-hours:2}")
    private int sessionHours;

    public AuthService(NguoiDungRepository nguoiDungRepository, TokenStore tokenStore) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.tokenStore = tokenStore;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.getTenDangNhap()) || isBlank(request.getMatKhau())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên đăng nhập và mật khẩu");
        }

        NguoiDungRecord user = nguoiDungRepository
                .findByTenDangNhap(request.getTenDangNhap().trim())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu"));

        if (!"ACTIVE".equalsIgnoreCase(user.getTrangThai())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản không hoạt động hoặc đã bị khóa");
        }

        if (!PasswordUtil.verifyPassword(request.getMatKhau(), user.getMatKhau())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu");
        }

        Set<String> permissions = nguoiDungRepository.findPermissionsByUserId(user.getMaNguoiDung());

        SessionUser sessionUser = new SessionUser();
        sessionUser.setMaNguoiDung(user.getMaNguoiDung());
        sessionUser.setTenDangNhap(user.getTenDangNhap());
        sessionUser.setMaVaiTro(user.getMaVaiTro());
        sessionUser.setTenVaiTro(user.getTenVaiTro());
        sessionUser.setMaChiNhanh(user.getMaChiNhanh());
        sessionUser.setTenChiNhanh(user.getTenChiNhanh());
        sessionUser.setPermissions(permissions);
        sessionUser.setExpiredAt(LocalDateTime.now().plusHours(sessionHours));

        String token = tokenStore.createSession(sessionUser);

        return new LoginResponse(token, toUserInfo(sessionUser));
    }

    public UserInfoResponse toUserInfo(SessionUser user) {
        return new UserInfoResponse(
                user.getMaNguoiDung(),
                user.getTenDangNhap(),
                user.getMaVaiTro(),
                user.getTenVaiTro(),
                user.getMaChiNhanh(),
                user.getTenChiNhanh(),
                user.getPermissions(),
                user.getExpiredAt()
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
