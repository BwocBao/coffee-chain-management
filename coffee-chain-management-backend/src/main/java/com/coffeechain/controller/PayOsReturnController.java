package com.coffeechain.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayOsReturnController {

    @GetMapping(value = "/api/payments/payos/return", produces = MediaType.TEXT_HTML_VALUE)
    public String payOsReturn() {
        return """
        <html>
          <body style="font-family: Arial; text-align: center; margin-top: 80px;">
            <h2>Thanh toán đã được ghi nhận</h2>
            <p>Vui lòng quay lại Swagger/Postman để kiểm tra trạng thái hóa đơn.</p>
          </body>
        </html>
        """;
    }

    @GetMapping(value = "/api/payments/payos/cancel", produces = MediaType.TEXT_HTML_VALUE)
    public String payOsCancel() {
        return """
        <html>
          <body style="font-family: Arial; text-align: center; margin-top: 80px;">
            <h2>Bạn đã hủy thanh toán</h2>
            <p>Hóa đơn vẫn chưa được thanh toán.</p>
          </body>
        </html>
        """;
    }
}