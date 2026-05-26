package com.coffeechain.controller;

import com.coffeechain.dto.BaseResponse;
import com.coffeechain.dto.request.CreatePosOrderRequest;
import com.coffeechain.dto.response.BankQrResponse;
import com.coffeechain.dto.response.PosOrderResponse;
import com.coffeechain.dto.response.PosProductResponse;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.service.InvoicePdfService;
import com.coffeechain.service.PosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "POS - Bán hàng tại quầy",
    description =
        """
                API phục vụ màn hình POS bán hàng tại quầy.

                Luồng cơ bản:
                1. Lấy danh sách sản phẩm đang bán.
                2. Tạo hóa đơn chờ thanh toán.
                3. Xem chi tiết hóa đơn.
                4. Thanh toán tiền mặt.

                Lưu ý:
                - Khi tạo hóa đơn, hệ thống chỉ tạo HOADON và CHITIETHOADON.
                - Chưa trừ kho tại thời điểm tạo hóa đơn.
                - Chỉ khi thanh toán thành công, hệ thống mới trừ kho theo công thức sản phẩm.
                """)
@RestController
@RequestMapping("/api/pos")
public class PosController {
  private final PosService posService;
  private final AuthGuard authGuard;
  private final InvoicePdfService invoicePdfService;

  public PosController(
      PosService posService, AuthGuard authGuard, InvoicePdfService invoicePdfService) {
    this.posService = posService;
    this.authGuard = authGuard;
    this.invoicePdfService = invoicePdfService;
  }

  @Operation(
      summary = "Lấy danh sách sản phẩm POS",
      description =
          """
                    Lấy danh sách sản phẩm có thể bán trên màn hình POS.

                    Backend chỉ trả về các sản phẩm:
                    - SANPHAM.trang_thai = AVAILABLE
                    - Có giá bán hiện tại
                    - Có ít nhất một dòng công thức trong CONGTHUC_SANPHAM

                    API này thường được gọi khi mở màn hình POS để hiển thị menu sản phẩm.

                    Yêu cầu quyền:
                    POS:VIEW
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lấy danh sách sản phẩm POS thành công"),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Không có quyền PRODUCT:VIEW",
        content = @Content)
  })
  @GetMapping("/products")
  public ResponseEntity<BaseResponse<List<PosProductResponse>>> getProducts(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader) {
    authGuard.requirePermission(authHeader, "PRODUCT:VIEW");

    return ResponseEntity.ok(
        BaseResponse.ok("Lấy danh sách sản phẩm POS thành công", posService.getProducts()));
  }

  @Operation(
      summary = "Tạo hóa đơn POS chờ thanh toán",
      description =
          """
                    Tạo hóa đơn POS từ giỏ hàng hiện tại.

                    Backend sẽ:
                    - Kiểm tra chi nhánh hợp lệ.
                    - Kiểm tra máy POS thuộc chi nhánh.
                    - Kiểm tra sản phẩm đang AVAILABLE và có công thức.
                    - Lấy giá bán hiện tại từ SANPHAM.gia_ban_hien_tai.
                    - Insert HOADON với trạng thái PENDING.
                    - Insert CHITIETHOADON.
                    - Tính tổng thanh toán và lưu vào HOADON.tong_thanh_toan.

                    Lưu ý:
                    - API này chưa trừ kho.
                    - Kho chỉ bị trừ sau khi thanh toán thành công.
                    - Trường ghiChu hiện chưa lưu nếu bảng HOADON chưa có cột ghi_chu.

                    Yêu cầu quyền:
                    POS:MANAGE
                    """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Thông tin hóa đơn POS cần tạo",
      required = true,
      content =
          @Content(
              schema = @Schema(implementation = CreatePosOrderRequest.class),
              examples =
                  @ExampleObject(
                      name = "Tạo hóa đơn POS",
                      value =
                          """
                                    {
                                      "maChiNhanh": 1,
                                      "maPos": 1,
                                      "items": [
                                        {
                                          "maSanPham": 1,
                                          "soLuong": 2
                                        },
                                        {
                                          "maSanPham": 3,
                                          "soLuong": 1
                                        }
                                      ],
                                      "ghiChu": "Ít đá"
                                    }
                                    """)))
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tạo hóa đơn POS thành công"),
    @ApiResponse(
        responseCode = "400",
        description = "Dữ liệu không hợp lệ hoặc sản phẩm không khả dụng",
        content = @Content),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Không có quyền ORDER:CREATE",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Không tìm thấy chi nhánh hoặc sản phẩm",
        content = @Content)
  })
  @PostMapping("/orders")
  public ResponseEntity<BaseResponse<PosOrderResponse>> createOrder(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @RequestBody CreatePosOrderRequest request) {
    SessionUser user = authGuard.requirePermission(authHeader, "ORDER:CREATE");

    return ResponseEntity.ok(
        BaseResponse.created("Tạo hóa đơn POS thành công", posService.createOrder(request, user)));
  }

  @Operation(
      summary = "Lấy chi tiết hóa đơn POS",
      description =
          """
                    Lấy thông tin chi tiết của một hóa đơn POS.

                    Response gồm:
                    - Thông tin hóa đơn chính.
                    - Trạng thái hóa đơn.
                    - Trạng thái thanh toán.
                    - Phương thức thanh toán.
                    - Tổng thanh toán.
                    - Danh sách sản phẩm trong hóa đơn.
                    - Thông tin payOS nếu hóa đơn đã tạo QR chuyển khoản.

                    API này có thể dùng cho:
                    - Màn hình xem lại hóa đơn.
                    - Frontend polling trạng thái thanh toán khi khách quét QR payOS.

                    Yêu cầu quyền:
                    POS:VIEW
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lấy chi tiết hóa đơn POS thành công"),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Không có quyền ORDER:VIEW",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Không tìm thấy hóa đơn", content = @Content)
  })
  @GetMapping("/orders/{maHoaDon}")
  public ResponseEntity<BaseResponse<PosOrderResponse>> getOrder(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long maHoaDon) {
    SessionUser user = authGuard.requirePermission(authHeader, "ORDER:VIEW");

    return ResponseEntity.ok(
        BaseResponse.ok("Lấy chi tiết hóa đơn POS thành công", posService.getOrder(maHoaDon, user)));
  }

  @Operation(
      summary = "Thanh toán hóa đơn bằng tiền mặt",
      description =
          """
                    Xác nhận thanh toán tiền mặt cho hóa đơn POS.

                    Chỉ xử lý nếu hóa đơn đang ở trạng thái:
                    - trang_thai_hoa_don = PENDING
                    - trang_thai_thanh_toan = PENDING

                    Backend sẽ chạy trong một transaction:
                    1. Lock HOADON.
                    2. Update phuong_thuc_thanh_toan = CASH.
                    3. Update trang_thai_thanh_toan = SUCCESS.
                    4. Trigger DB tự set thoi_gian_thanh_toan và chuyển hóa đơn sang PAID.
                    5. Trừ kho theo công thức sản phẩm.
                    6. Ghi BANHANG_TRUKHO.
                    7. Ghi NHATKY_KHO với SALE_DEDUCT.
                    8. Update trang_thai_hoa_don = COMPLETED.

                    Lưu ý:
                    - Nếu tồn kho không đủ, toàn bộ transaction rollback.
                    - Không trừ kho hai lần nếu hóa đơn đã hoàn tất.

                    Yêu cầu quyền:
                    ORDER:PAY
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Thanh toán tiền mặt thành công"),
    @ApiResponse(
        responseCode = "400",
        description = "Hóa đơn không ở trạng thái chờ thanh toán hoặc tồn kho không đủ",
        content = @Content),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Không có quyền ORDER:PAY",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Không tìm thấy hóa đơn hoặc kho chi nhánh",
        content = @Content)
  })
  @PostMapping("/orders/{maHoaDon}/pay-cash")
  public ResponseEntity<BaseResponse<PosOrderResponse>> payCash(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long maHoaDon) {
    SessionUser user = authGuard.requirePermission(authHeader, "ORDER:PAY");

    return ResponseEntity.ok(
        BaseResponse.ok("Thanh toán tiền mặt thành công", posService.payCash(maHoaDon, user)));
  }

  @Operation(
      summary = "Tao QR chuyen khoan payOS cho hoa don POS",
      description =
          """
                    Tao payment link/QR payOS cho hoa don dang cho thanh toan.

                    Dieu kien:
                    - HOADON.trang_thai_hoa_don = PENDING.
                    - HOADON.trang_thai_thanh_toan = PENDING.
                    - Neu da co THANHTOAN_PAYOS PENDING cho hoa don thi tra lai QR cu, khong tao giao dich moi.

                    API nay chi tao QR, chua tru kho va chua hoan tat hoa don.
                    Khi khach thanh toan thanh cong, payOS se goi POST /api/payments/payos/webhook.

                    Request class: khong co body.
                    Response class: BankQrResponse.
                    Quyen: ORDER:PAY.
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tao QR payOS thanh cong"),
    @ApiResponse(
        responseCode = "400",
        description = "Hoa don khong o trang thai cho thanh toan",
        content = @Content),
    @ApiResponse(
        responseCode = "401",
        description = "Chua dang nhap hoac token het han",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Khong co quyen ORDER:PAY",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Khong tim thay hoa don", content = @Content),
    @ApiResponse(responseCode = "502", description = "Khong tao duoc QR payOS", content = @Content)
  })
  @PostMapping("/orders/{maHoaDon}/create-bank-qr")
  public ResponseEntity<BaseResponse<BankQrResponse>> createBankQr(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long maHoaDon) {
    SessionUser user = authGuard.requirePermission(authHeader, "ORDER:PAY");

    return ResponseEntity.ok(
        BaseResponse.ok("Tao QR chuyen khoan payOS thanh cong", posService.createBankQr(maHoaDon, user)));
  }

  @Operation(
      summary = "Huy hoa don POS chua thanh toan",
      description =
          """
                    Huy hoa don POS khi khach khong tiep tuc thanh toan.

                    Chi cho huy neu:
                    - trang_thai_hoa_don = PENDING.
                    - trang_thai_thanh_toan = PENDING.

                    Backend se:
                    1. Lock HOADON.
                    2. Huy THANHTOAN_PAYOS PENDING neu co.
                    3. Update HOADON.trang_thai_hoa_don = CANCELLED.
                    4. Update HOADON.trang_thai_thanh_toan = FAILED.

                    Request class: khong co body.
                    Response class: PosOrderResponse.
                    Quyen: ORDER:CANCEL.
                    """)
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Huy hoa don POS thanh cong"),
    @ApiResponse(responseCode = "400", description = "Hoa don khong the huy", content = @Content),
    @ApiResponse(
        responseCode = "401",
        description = "Chua dang nhap hoac token het han",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Khong co quyen ORDER:CANCEL",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Khong tim thay hoa don", content = @Content)
  })
  @PostMapping("/orders/{maHoaDon}/cancel")
  public ResponseEntity<BaseResponse<PosOrderResponse>> cancelOrder(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @PathVariable Long maHoaDon) {
    SessionUser user = authGuard.requirePermission(authHeader, "ORDER:CANCEL");

    return ResponseEntity.ok(
        BaseResponse.ok("Huy hoa don POS thanh cong", posService.cancelOrder(maHoaDon, user)));
  }

  @Operation(
      summary = "In hóa đơn POS ra PDF",
      description =
          """
                Xuất hóa đơn POS thành file PDF để frontend có thể mở, tải xuống hoặc in.

                Chỉ cho in nếu hóa đơn đã thanh toán thành công:
                - trang_thai_hoa_don = COMPLETED
                - trang_thai_thanh_toan = SUCCESS

                API này trả về file PDF trực tiếp, không bọc BaseResponse.

                Frontend Swing có thể:
                - Enable nút "In hóa đơn" khi order COMPLETED + SUCCESS.
                - Khi bấm nút, gọi API này để lấy file PDF.

                Yêu cầu quyền:
                ORDER:VIEW
                """)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Xuất PDF hóa đơn thành công",
        content = @Content(mediaType = "application/pdf")),
    @ApiResponse(
        responseCode = "400",
        description = "Hóa đơn chưa thanh toán thành công",
        content = @Content),
    @ApiResponse(
        responseCode = "401",
        description = "Chưa đăng nhập hoặc token hết hạn",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Không có quyền ORDER:VIEW",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Không tìm thấy hóa đơn", content = @Content)
  })
  @GetMapping(value = "/orders/{maHoaDon}/invoice.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> printInvoicePdf(
      @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false)
          String authHeader,
      @Parameter(description = "Mã hóa đơn POS cần in", example = "361", required = true)
          @PathVariable
          Long maHoaDon) {
    SessionUser user = authGuard.requirePermission(authHeader, "ORDER:VIEW");
    posService.requireOrderAccess(maHoaDon, user);

    byte[] pdfBytes = invoicePdfService.generateInvoicePdf(maHoaDon);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=hoa-don-" + maHoaDon + ".pdf")
        .body(pdfBytes);
  }
}
