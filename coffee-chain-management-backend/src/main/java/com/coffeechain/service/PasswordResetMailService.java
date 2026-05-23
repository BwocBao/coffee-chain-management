package com.coffeechain.service;

import com.coffeechain.exception.AppException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetMailService {
  private static final Logger log = LoggerFactory.getLogger(PasswordResetMailService.class);

  private final ObjectProvider<JavaMailSender> mailSenderProvider;

  @Value("${app.mail.enabled:false}")
  private boolean mailEnabled;

  @Value("${app.mail.from:no-reply@phungloc.local}")
  private String from;

  public PasswordResetMailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
    this.mailSenderProvider = mailSenderProvider;
  }

  public boolean sendResetCode(String email, String code, int validMinutes) {
    JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

    if (!mailEnabled) {
      log.info(
          "Password reset code for {} is {}. Code expires in {} minutes.",
          email,
          code,
          validMinutes);
      return false;
    }

    if (mailSender == null) {
      throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Chua cau hinh dich vu gui email");
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();

      /*
       * true = multipart message.
       * UTF-8 để email hiển thị tiếng Việt không bị lỗi font.
       */
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(from);
      helper.setTo(email);
      helper.setSubject("Mã xác nhận đặt lại mật khẩu Phụng Lộc");

      String plainText =
          """
                Mã xác nhận đặt lại mật khẩu của bạn là: %s

                Mã này có hiệu lực trong %d phút.
                Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                """
              .formatted(code, validMinutes);

      String htmlContent = buildResetPasswordEmailHtml(code, validMinutes);

      /*
       * setText(plainText, htmlContent)
       * - plainText: fallback nếu email client không hỗ trợ HTML
       * - htmlContent: giao diện email đẹp
       */
      helper.setText(plainText, htmlContent);

      mailSender.send(message);
      return true;
    } catch (Exception ex) {
      log.warn("Cannot send password reset email to {}.", email, ex);
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Khong gui duoc email xac nhan. Vui long kiem tra cau hinh SMTP");
    }
  }

  public boolean isMailEnabled() {
    return mailEnabled;
  }

  private String buildResetPasswordEmailHtml(String code, int validMinutes) {
    return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Mã xác nhận đặt lại mật khẩu</title>
            </head>
            <body style="margin:0; padding:0; background:#FFF4D8; font-family:Arial, Helvetica, sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background:#FFF4D8; padding:32px 0;">
                    <tr>
                        <td align="center">
                            <table width="560" cellpadding="0" cellspacing="0" border="0"
                                   style="background:#FFFFFF; border-radius:18px; overflow:hidden; box-shadow:0 12px 32px rgba(139,74,36,0.18);">

                                <!-- Header -->
                                <tr>
                                    <td style="background:#C67C4E; padding:26px 32px; text-align:center;">
                                        <div style="font-size:13px; letter-spacing:1.5px; color:#FFF4D8; font-weight:bold;">
                                            QUẢN TRỊ HỆ THỐNG PHỤNG LỘC ☕
                                        </div>
                                        <div style="font-size:26px; color:#FFFFFF; font-weight:bold; margin-top:8px;">
                                            Đặt lại mật khẩu
                                        </div>
                                    </td>
                                </tr>

                                <!-- Content -->
                                <tr>
                                    <td style="padding:34px 42px 20px 42px; color:#1F1F1F;">
                                        <div style="font-size:18px; font-weight:bold; margin-bottom:12px;">
                                            Xin chào,
                                        </div>

                                        <div style="font-size:15px; line-height:1.7; color:#5F5F5F;">
                                            Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn
                                            trên hệ thống quản lý chuỗi cà phê Phụng Lộc.
                                        </div>

                                        <div style="margin:28px 0 10px 0; text-align:center;">
                                            <div style="font-size:13px; color:#8B4A24; font-weight:bold; margin-bottom:10px;">
                                                MÃ XÁC NHẬN CỦA BẠN
                                            </div>

                                            <div style="
                                                display:inline-block;
                                                background:#FFF4D8;
                                                border:2px dashed #C67C4E;
                                                border-radius:14px;
                                                padding:16px 34px;
                                                color:#8B4A24;
                                                font-size:34px;
                                                font-weight:bold;
                                                letter-spacing:8px;">
                                                %s
                                            </div>
                                        </div>

                                        <div style="
                                            margin:24px 0;
                                            background:#FFF8EA;
                                            border-left:5px solid #C67C4E;
                                            border-radius:10px;
                                            padding:16px 18px;
                                            color:#5F5F5F;
                                            font-size:14px;
                                            line-height:1.6;">
                                            Mã này có hiệu lực trong
                                            <strong style="color:#8B4A24;">%d phút</strong>.
                                            Vui lòng không chia sẻ mã này cho bất kỳ ai.
                                        </div>

                                        <div style="font-size:14px; line-height:1.7; color:#727272;">
                                            Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                                            Mật khẩu hiện tại của bạn sẽ không thay đổi.
                                        </div>
                                    </td>
                                </tr>

                                <!-- Divider -->
                                <tr>
                                    <td style="padding:0 42px;">
                                        <div style="height:1px; background:#E6E6E6;"></div>
                                    </td>
                                </tr>

                                <!-- Footer -->
                                <tr>
                                    <td style="padding:22px 42px 30px 42px; text-align:center;">
                                        <div style="font-size:13px; color:#8B4A24; font-weight:bold;">
                                            Phụng Lộc Coffee Management
                                        </div>
                                        <div style="font-size:12px; color:#9A9A9A; margin-top:6px;">
                                            Email tự động từ hệ thống. Vui lòng không phản hồi email này.
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """
        .formatted(code, validMinutes);
  }
}
