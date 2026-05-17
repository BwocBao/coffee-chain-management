package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.ForgotPasswordRequest;
import com.coffeechain.dto.ForgotPasswordResponse;
import com.coffeechain.dto.ResetPasswordRequest;
import com.coffeechain.dto.VerifyResetCodeRequest;
import com.coffeechain.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "AUTH - Quen mat khau",
        description = "API gui ma xac nhan va dat lai mat khau bang email."
)
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;


    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Operation(
            summary = "Gui ma xac nhan quen mat khau",
            description = """
                    Luong hoat dong:
                    1. Frontend gui email nguoi dung nhap len API nay.
                    2. Backend chuan hoa email: trim va dua ve chu thuong.
                    3. Backend kiem tra email co hop le khong.
                    4. Backend tim tai khoan theo email trong database.
                    5. Neu tai khoan ton tai va dang ACTIVE, backend tao ma xac nhan 6 chu so.
                    6. Ma xac nhan duoc luu tam tren server voi han su dung ngan.
                    7. Backend gui ma qua email neu da cau hinh SMTP.
                    8. Neu chua cau hinh SMTP, ma co the duoc in ra console hoac tra ve debug code tuy cau hinh.
                    9. Frontend nhan response va chuyen nguoi dung sang man hinh nhap ma xac nhan.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Da xu ly yeu cau gui ma"),
            @ApiResponse(responseCode = "400", description = "Email khong hop le", content = @Content),
            @ApiResponse(responseCode = "403", description = "Tai khoan bi khoa hoac khong hoat dong", content = @Content),
            @ApiResponse(responseCode = "404", description = "Email chua ton tai trong he thong", content = @Content)
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<BaseResponse<ForgotPasswordResponse>> forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {
        /*
         * request gom email nguoi dung nhap.
         *
         * Service se:
         * - validate email
         * - tim user theo email
         * - kiem tra trang thai user phai la ACTIVE
         * - tao ma reset 6 chu so
         * - luu ma reset vao bo nho tam
         * - gui ma qua email hoac console debug
         *
         * ForgotPasswordResponse thuong gom:
         * - email da duoc che bot ky tu
         * - thoi diem het han cua ma
         * - debugCode neu app dang bat cau hinh tra code de test local
         */
        ForgotPasswordResponse response = passwordResetService.requestReset(request);

        return ResponseEntity.ok(
                BaseResponse.ok("Neu email ton tai, ma xac nhan da duoc gui", response)
        );
    }

    @Operation(
            summary = "Kiem tra ma xac nhan quen mat khau",
            description = """
                    Luong hoat dong:
                    1. Frontend gui email va ma xac nhan 6 chu so len API nay.
                    2. Backend chuan hoa email va ma xac nhan.
                    3. Backend kiem tra ma co ton tai trong bo nho tam khong.
                    4. Backend kiem tra ma da het han chua.
                    5. Backend kiem tra ma da dung roi hay chua.
                    6. Backend kiem tra so lan nhap sai co vuot gioi han khong.
                    7. Neu ma dung, backend danh dau ma da duoc verify.
                    8. Frontend nhan response thanh cong va cho nguoi dung nhap mat khau moi.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ma xac nhan hop le"),
            @ApiResponse(responseCode = "400", description = "Ma khong dung, khong ton tai hoac da het han", content = @Content)
    })
    @PostMapping("/forgot-password/verify")
    public ResponseEntity<BaseResponse<Void>> verifyResetCode(
            @RequestBody VerifyResetCodeRequest request
    ) {
        /*
         * request gom:
         * - email
         * - code: ma xac nhan 6 chu so
         *
         * API nay chi dung de kiem tra ma truoc.
         * Sau khi verify thanh cong, frontend co the hien form nhap mat khau moi.
         */
        passwordResetService.verifyCode(request);

        return ResponseEntity.ok(
                BaseResponse.ok("Ma xac nhan hop le", null)
        );
    }

    @Operation(
            summary = "Dat lai mat khau",
            description = """
                    Luong hoat dong:
                    1. Frontend gui email, ma xac nhan va mat khau moi len API nay.
                    2. Backend chuan hoa email va ma xac nhan.
                    3. Backend kiem tra lai ma xac nhan mot lan nua de tranh reset trai phep.
                    4. Backend kiem tra mat khau moi co hop le khong.
                    5. Backend ma hoa mat khau moi bang PasswordUtil.
                    6. Backend cap nhat mat khau moi vao bang NGUOIDUNG theo email.
                    7. Sau khi doi mat khau thanh cong, backend danh dau ma da dung va xoa ma khoi bo nho tam.
                    8. Frontend cho nguoi dung quay lai man hinh dang nhap.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dat lai mat khau thanh cong"),
            @ApiResponse(responseCode = "400", description = "Du lieu khong hop le, ma sai hoac ma da het han", content = @Content)
    })
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        /*
         * request gom:
         * - email
         * - code: ma xac nhan 6 chu so
         * - newPassword: mat khau moi
         *
         * Service se validate code truoc, sau do moi cho doi mat khau.
         * Lam nhu vay de dam bao nguoi khong co ma xac nhan thi khong the reset password.
         */
        passwordResetService.resetPassword(request);


        return ResponseEntity.ok(
                BaseResponse.ok("Dat lai mat khau thanh cong", null)
        );
    }
}