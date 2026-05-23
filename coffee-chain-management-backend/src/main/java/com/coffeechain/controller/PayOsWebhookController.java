package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.response.PayOsWebhookResponse;
import com.coffeechain.service.PayOsPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Webhook thanh toan payOS",
    description =
        "Endpoint public de payOS goi khi giao dich QR thay doi trang thai. API khong dung AuthGuard, ma xac thuc bang checksum/signature payOS.")
@RestController
@RequestMapping("/api/payments/payos")
public class PayOsWebhookController {
  private final PayOsPaymentService payOsPaymentService;

  public PayOsWebhookController(PayOsPaymentService payOsPaymentService) {
    this.payOsPaymentService = payOsPaymentService;
  }

  @Operation(
      summary = "Nhan webhook thanh toan payOS",
      description =
          """
                    payOS goi API nay sau khi khach quet QR va thanh toan/huy/thanh toan that bai.
                    Backend verify webhook bang payOS SDK, tim THANHTOAN_PAYOS theo orderCode, cap nhat HOADON va tru kho neu thanh toan thanh cong.

                    Luong thanh cong:
                    1. Verify webhook.
                    2. Lock THANHTOAN_PAYOS theo orderCode.
                    3. Lock HOADON.
                    4. Neu HOADON da COMPLETED thi tra OK, khong tru kho lai.
                    5. Neu amount khop: update THANHTOAN_PAYOS = PAID.
                    6. Update HOADON phuong_thuc_thanh_toan = BANK_TRANSFER, trang_thai_thanh_toan = SUCCESS.
                    7. Goi PosInventoryDeductionService de tru kho FEFO, ghi BANHANG_TRUKHO va NHATKY_KHO.
                    8. Update HOADON = COMPLETED.

                    Request class: raw JSON webhook cua payOS.
                    Response class: PayOsWebhookResponse.
                    """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Raw webhook JSON tu payOS",
      required = true,
      content =
          @Content(
              schema = @Schema(implementation = Object.class),
              examples =
                  @ExampleObject(
                      value =
                          """
                            {
                              "code": "00",
                              "desc": "success",
                              "success": true,
                              "data": {
                                "orderCode": 150001,
                                "amount": 85000,
                                "description": "PL15",
                                "reference": "FT123456",
                                "paymentLinkId": "abc"
                              },
                              "signature": "payos-signature"
                            }
                            """)))
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Webhook hop le va da xu ly xong"),
    @ApiResponse(
        responseCode = "400",
        description = "Webhook khong hop le hoac verify that bai",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Khong tim thay giao dich payOS/hoa don",
        content = @Content)
  })
  @PostMapping("/webhook")
  public ResponseEntity<BaseResponse<PayOsWebhookResponse>> webhook(@RequestBody String rawBody) {
    return ResponseEntity.ok(
        BaseResponse.ok(
            "Xu ly webhook payOS thanh cong", payOsPaymentService.handleWebhook(rawBody)));
  }
}
