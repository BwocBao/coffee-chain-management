package com.coffeechain.service;

import com.coffeechain.dto.ForgotPasswordRequest;
import com.coffeechain.dto.ForgotPasswordResponse;
import com.coffeechain.dto.ResetPasswordRequest;
import com.coffeechain.dto.VerifyResetCodeRequest;
import com.coffeechain.exception.AppException;
import com.coffeechain.repository.NguoiDungRecord;
import com.coffeechain.repository.NguoiDungRepository;
import com.coffeechain.security.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    /*
     * SecureRandom dung de sinh ma xac nhan 6 chu so.
     * Dung SecureRandom an toan hon Random thong thuong vi ma nay lien quan den bao mat tai khoan.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /*
     * Repository de truy van va cap nhat bang NGUOIDUNG.
     */
    private final NguoiDungRepository nguoiDungRepository;

    /*
     * Service gui email reset password.
     * Neu chua cau hinh SMTP, service co the in ma ra console de test local.
     */
    private final PasswordResetMailService mailService;

    /*
     * resetCodes la noi luu ma xac nhan tam thoi trong RAM cua server.
     *
     * Key: email cua nguoi dung
     * Value: doi tuong ResetCode gom:
     * - code: ma 6 chu so
     * - expiresAt: thoi gian het han
     * - attempts: so lan nhap sai
     * - verified: da verify thanh cong chua
     * - used: ma da duoc dung de doi mat khau chua
     *
     * Dung ConcurrentHashMap de an toan hon khi co nhieu request cung luc.
     */
    private final Map<String, ResetCode> resetCodes = new ConcurrentHashMap<>();

    /*
     * Thoi gian het han cua ma reset.
     * Lay tu application.properties:
     * app.auth.password-reset.code-minutes=10
     *
     * Neu khong cau hinh thi mac dinh la 10 phut.
     */
    @Value("${app.auth.password-reset.code-minutes:10}")
    private int codeMinutes;

    /*
     * So lan nhap sai toi da.
     * Lay tu application.properties:
     * app.auth.password-reset.max-attempts=5
     *
     * Neu user nhap sai qua so lan nay, ma se bi xoa va bat buoc gui lai ma moi.
     */
    @Value("${app.auth.password-reset.max-attempts:5}")
    private int maxAttempts;

    /*
     * Cau hinh co tra ma reset ve response hay khong.
     *
     * Nen de false khi chay that.
     * Chi nen true khi test local de frontend de lay ma ma khong can email.
     */
    @Value("${app.auth.password-reset.return-code-in-response:false}")
    private boolean returnCodeInResponse;

    /*
     * Constructor injection.
     * Spring tu dong truyen NguoiDungRepository va PasswordResetMailService vao day.
     */
    public PasswordResetService(NguoiDungRepository nguoiDungRepository, PasswordResetMailService mailService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.mailService = mailService;
    }

    /*
     * API buoc 1: Nguoi dung bam "Quen mat khau" va nhap email.
     *
     * Luong xu ly:
     * 1. Lay email tu request.
     * 2. Chuan hoa email: trim, lower-case.
     * 3. Kiem tra email hop le.
     * 4. Tim user theo email trong database.
     * 5. Neu email khong ton tai thi bao loi.
     * 6. Neu tai khoan khong ACTIVE thi khong cho reset.
     * 7. Tao ma xac nhan 6 chu so.
     * 8. Luu ma vao RAM kem thoi gian het han.
     * 9. Gui ma qua email.
     * 10. Tra ve email da che bot va thoi gian het han.
     */
    public ForgotPasswordResponse requestReset(ForgotPasswordRequest request) {
        /*
         * Neu request null thi lay email null.
         * normalizeEmail se bien null thanh chuoi rong "".
         */
        String email = normalizeEmail(request == null ? null : request.getEmail());

        /*
         * Kiem tra email rong/sai format co @ hay khong.
         */
        validateEmail(email);

        /*
         * Tao thoi gian het han cua ma.
         * Vi du codeMinutes = 10 thi ma co han trong 10 phut.
         */
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(codeMinutes);

        /*
         * Tim tai khoan theo email trong database.
         */
        Optional<NguoiDungRecord> user = nguoiDungRepository.findByEmail(email);

        /*
         * debugCode chi dung cho test local.
         * Neu SMTP chua cau hinh hoac cau hinh cho phep tra code,
         * backend co the tra ma nay ve response.
         */
        String debugCode = null;

        /*
         * Neu khong co email trong he thong thi khong cho reset.
         */
        if (user.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Email chua ton tai trong he thong");
        }

        /*
         * Chi tai khoan ACTIVE moi duoc reset mat khau.
         * Tai khoan LOCKED/INACTIVE thi chan lai.
         */
        if (!"ACTIVE".equalsIgnoreCase(user.get().getTrangThai())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Tai khoan dang bi khoa hoac khong hoat dong");
        }

        /*
         * Sinh ma 6 chu so, vi du: 038291.
         */
        String code = generateCode();

        /*
         * Luu ma vao RAM.
         *
         * Neu email nay da tung yeu cau reset truoc do,
         * put() se ghi de ma cu bang ma moi.
         */
        resetCodes.put(email, new ResetCode(code, expiresAt));

        /*
         * Gui ma qua email.
         * mailSent = true neu gui thanh cong.
         */
        boolean mailSent = mailService.sendResetCode(email, code, codeMinutes);

        /*
         * Neu cau hinh returnCodeInResponse = true thi tra code ve response de test.
         * Hoac neu mail chua bat va gui mail that bai thi cung tra code de test local.
         *
         * Khi chay that thi nen dam bao returnCodeInResponse = false.
         */
        debugCode = (returnCodeInResponse || (!mailService.isMailEnabled() && !mailSent)) ? code : null;

        /*
         * Tra ve email da mask, thoi gian het han va debugCode neu co.
         */
        return new ForgotPasswordResponse(maskEmail(email), expiresAt, debugCode);
    }

    /*
     * API buoc 2: Kiem tra ma xac nhan.
     *
     * API nay dung khi frontend co man hinh rieng:
     * - Nhap email
     * - Nhap ma xac nhan
     * - Neu ma dung moi hien man hinh dat mat khau moi
     *
     * API nay khong doi mat khau, chi verify ma.
     */
    public void verifyCode(VerifyResetCodeRequest request) {
        /*
         * Lay va chuan hoa email.
         */
        String email = normalizeEmail(request == null ? null : request.getEmail());
        validateEmail(email);

        /*
         * Lay va chuan hoa code.
         */
        String code = normalizeCode(request == null ? null : request.getCode());

        /*
         * Kiem tra code.
         *
         * markVerified = true nghia la neu code dung,
         * resetCode.verified se duoc set thanh true.
         */
        validateResetCode(email, code, true);
    }

    /*
     * API buoc 3: Dat lai mat khau.
     *
     * Luong xu ly:
     * 1. Lay email va code tu request.
     * 2. Kiem tra email hop le.
     * 3. Kiem tra code co dung, con han, chua qua so lan sai khong.
     * 4. Kiem tra mat khau moi.
     * 5. Hash mat khau moi.
     * 6. Cap nhat mat khau vao database.
     * 7. Danh dau ma da dung.
     * 8. Xoa ma khoi RAM.
     */
    public void resetPassword(ResetPasswordRequest request) {
        /*
         * Lay email tu request va chuan hoa.
         */
        String email = normalizeEmail(request == null ? null : request.getEmail());
        validateEmail(email);

        /*
         * Lay code tu request va chuan hoa.
         */
        String code = normalizeCode(request == null ? null : request.getCode());

        /*
         * Reset password van phai kiem tra lai code.
         *
         * Ly do:
         * Du frontend da goi /verify truoc do, hacker van co the goi truc tiep API reset.
         * Vi vay backend bat buoc validate code lai o buoc doi mat khau.
         */
        ResetCode resetCode = validateResetCode(email, code, true);

        /*
         * Kiem tra mat khau moi.
         * Hien tai yeu cau toi thieu 6 ky tu.
         */
        String newPassword = request == null ? null : request.getNewPassword();
        if (newPassword == null || newPassword.length() < 6) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Mat khau moi phai co it nhat 6 ky tu");
        }

        /*
         * Hash mat khau moi truoc khi luu database.
         * Tuyet doi khong luu plain text password.
         */
        String hash = PasswordUtil.hashPassword(newPassword);

        /*
         * Cap nhat mat khau theo email.
         * updated la so dong bi update.
         */
        int updated = nguoiDungRepository.updatePasswordByEmail(email, hash);

        /*
         * Neu updated = 0 nghia la khong co dong nao duoc cap nhat.
         */
        if (updated == 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Khong cap nhat duoc mat khau");
        }

        /*
         * Danh dau ma da duoc su dung.
         */
        resetCode.used = true;

        /*
         * Xoa ma khoi RAM.
         * Sau khi doi mat khau thanh cong, ma reset khong duoc dung lai.
         */
        resetCodes.remove(email);
    }

    /*
     * Ham kiem tra ma reset.
     *
     * Ham nay duoc dung boi ca:
     * - verifyCode()
     * - resetPassword()
     *
     * Cac dieu kien can kiem tra:
     * 1. Code co dung dinh dang 6 ky tu khong.
     * 2. Email nay co ma reset trong RAM khong.
     * 3. Ma da bi dung chua.
     * 4. Ma da het han chua.
     * 5. User da nhap sai qua so lan cho phep chua.
     * 6. Code user nhap co khop code server luu khong.
     */
    private ResetCode validateResetCode(String email, String code, boolean markVerified) {
        /*
         * Code phai khac null va co dung 6 ky tu.
         * Vi generateCode() tao ma 6 chu so.
         */
        if (code == null || code.length() != 6) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Ma xac nhan khong hop le");
        }

        /*
         * Lay ma reset dang luu trong RAM theo email.
         */
        ResetCode resetCode = resetCodes.get(email);

        /*
         * Neu resetCode null: user chua yeu cau reset hoac ma da bi xoa.
         * Neu used = true: ma da duoc dung de doi mat khau.
         * Neu expiresAt < now: ma da het han.
         *
         * Khi gap cac truong hop nay thi xoa khoi RAM cho sach.
         */
        if (resetCode == null || resetCode.used || resetCode.expiresAt.isBefore(LocalDateTime.now())) {
            resetCodes.remove(email);
            throw new AppException(HttpStatus.BAD_REQUEST, "Ma xac nhan da het han hoac khong ton tai");
        }

        /*
         * Kiem tra so lan nhap sai.
         *
         * Vi du maxAttempts = 5:
         * - Nhap sai lan 1: attempts = 1
         * - Nhap sai lan 2: attempts = 2
         * - ...
         * - Khi attempts >= 5 thi xoa ma va bat nguoi dung gui lai ma moi.
         */
        if (resetCode.attempts >= maxAttempts) {
            resetCodes.remove(email);
            throw new AppException(HttpStatus.BAD_REQUEST, "Ban da nhap sai qua so lan cho phep. Vui long gui lai ma moi");
        }

        /*
         * Neu code nguoi dung nhap khong giong code server luu,
         * tang so lan nhap sai len 1.
         */
        if (!resetCode.code.equals(code)) {
            resetCode.attempts++;
            throw new AppException(HttpStatus.BAD_REQUEST, "Ma xac nhan khong dung");
        }

        /*
         * Neu code dung va markVerified = true,
         * danh dau ma da duoc verify.
         *
         * Bien verified nay huu ich neu sau nay ban muon bat buoc:
         * - phai goi /verify thanh cong truoc
         * - roi moi duoc goi /reset
         *
         * Hien tai resetPassword() van validate code truc tiep,
         * nen verified chua phai dieu kien bat buoc.
         */
        if (markVerified) {
            resetCode.verified = true;
        }

        /*
         * Tra ve resetCode de ham resetPassword() co the danh dau used = true sau khi doi mat khau.
         */
        return resetCode;
    }

    /*
     * Chuan hoa email:
     * - null thanh ""
     * - bo khoang trang dau/cuoi
     * - chuyen ve chu thuong
     *
     * Lam nhu vay de Email@Demo.com va email@demo.com duoc xem la cung mot email.
     */
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /*
     * Chuan hoa ma code:
     * - null thanh ""
     * - bo khoang trang dau/cuoi
     */
    private String normalizeCode(String code) {
        return code == null ? "" : code.trim();
    }

    /*
     * Kiem tra email co hop le co ban hay khong.
     * Hien tai chi check:
     * - khong rong
     * - co ky tu @
     * Neu muon chat hon, co the dung regex email.
     */
    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email khong hop le");
        }
    }

    /*
     * Sinh ma 6 chu so.
     * RANDOM.nextInt(1_000_000) tao so tu 0 den 999999.
     * String.format("%06d", ...) dam bao luon co 6 chu so.
     * Vi du:
     * - 12 thanh "000012"
     * - 9281 thanh "009281"
     * - 381920 thanh "381920"
     */
    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /*
     * Che bot email khi tra ve frontend.
     * Vi du:
     * dat@gmail.com -> d***@gmail.com
     * Muc dich:
     * - Bao mat thong tin nguoi dung hon
     * - Van giup nguoi dung biet ma da gui ve email nao
     */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + email.substring(Math.max(at, 0));
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    /*
     * Ham nay dung de tu dong don RAM.
     * Cu moi 60 giay, server quet resetCodes mot lan.
     * Ma nao da het han hoac da dung thi xoa khoi RAM.
     * Luu y:
     * Muon @Scheduled chay duoc, phai them @EnableScheduling vao class main Spring Boot.
     */
//    @Scheduled(fixedRate = 60_000)
//    public void cleanupExpiredResetCodes() {
//        LocalDateTime now = LocalDateTime.now();
//
//        resetCodes.entrySet().removeIf(entry -> {
//            ResetCode resetCode = entry.getValue();
//            return resetCode.used || resetCode.expiresAt.isBefore(now);
//        });
//    }

    /*
     * Class noi bo dung de luu thong tin ma reset trong RAM.
     * Moi email se co mot ResetCode tai mot thoi diem.
     */
    private static class ResetCode {

        /*
         * Ma xac nhan 6 chu so.
         */
        private final String code;

        private final LocalDateTime expiresAt;

        /*
         * So lan nguoi dung da nhap sai ma.
         */
        private int attempts;

        /*
         * true neu user da verify code thanh cong.
         */
        private boolean verified;

        /*
         * true neu code da duoc dung de doi mat khau.
         */
        private boolean used;

        private ResetCode(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;

            /*
             * attempts, verified, used khong can set ro rang vi mac dinh:
             * int attempts = 0
             * boolean verified = false
             * boolean used = false
             */
        }
    }
}