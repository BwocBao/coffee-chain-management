package com.coffeechain.controller;

import com.coffeechain.dto.*;
import com.coffeechain.dto.request.CreateExportReceiptRequest;
import com.coffeechain.dto.request.CreateImportReceiptRequest;
import com.coffeechain.dto.response.*;
import com.coffeechain.security.AuthGuard;
import com.coffeechain.security.SessionUser;
import com.coffeechain.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.coffeechain.dto.request.CreateTransferReceiptRequest;

import java.util.List;

@Tag(name = "Kho - Nhap/Xuat kho", description = "API tao phieu nhap kho, xuat kho, cap nhat ton kho va lo hang")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    private final AuthGuard authGuard;

    public InventoryController(InventoryService inventoryService, AuthGuard authGuard) {
        this.inventoryService = inventoryService;
        this.authGuard = authGuard;
    }

    @Operation(
            summary = "Lay du lieu combobox cho man hinh nhap kho",
            description = "Tra ve danh sach kho, nha cung cap va nguyen lieu de frontend render form nhap kho."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lay du lieu thanh cong"),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen nhap kho", content = @Content)
    })
    @GetMapping("/imports/lookups")
    public ResponseEntity<BaseResponse<InventoryLookupResponse>> importLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requirePermission(authHeader, "INVENTORY:IMPORT");
        return ResponseEntity.ok(BaseResponse.ok("Lay du lieu nhap kho thanh cong", inventoryService.getImportLookup()));
    }

    @Operation(
            summary = "Lay du lieu combobox cho man hinh xuat kho",
            description = "Tra ve danh sach kho, nguyen lieu va loai xuat kho de frontend render form xuat kho."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lay du lieu thanh cong"),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen xuat kho", content = @Content)
    })
    @GetMapping("/exports/lookups")
    public ResponseEntity<BaseResponse<InventoryExportLookupResponse>> exportLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requirePermission(authHeader, "INVENTORY:EXPORT");
        return ResponseEntity.ok(BaseResponse.ok("Lay du lieu xuat kho thanh cong", inventoryService.getExportLookup()));
    }

    @Operation(
            summary = "Lay danh sach lo con ton de xuat kho",
            description = "Dung cho che do chon lo nang cao. Danh sach duoc sap xep theo FEFO: han su dung gan nhat truoc, sau do den ngay tao."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lay danh sach lo thanh cong"),
            @ApiResponse(responseCode = "400", description = "Thieu kho hoac nguyen lieu", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen xuat kho", content = @Content),
            @ApiResponse(responseCode = "404", description = "Khong tim thay kho hoac nguyen lieu", content = @Content)
    })
    @GetMapping("/exports/lots")
    public ResponseEntity<BaseResponse<List<InventoryLotResponse>>> exportLots(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Ma kho xuat", example = "1")
            @RequestParam Long maKho,
            @Parameter(description = "Ma nguyen lieu can xem lo", example = "2")
            @RequestParam Long maNguyenLieu
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:EXPORT");
        return ResponseEntity.ok(BaseResponse.ok(
                "Lay danh sach lo xuat kho thanh cong",
                inventoryService.getLotsForExport(maKho, maNguyenLieu, user)
        ));
    }

    @Operation(
            summary = "Tao phieu nhap kho",
            description = """
                    Tao phieu nhap trong PHIEUNHAP, luu chi tiet vao CHITIETPHIEUNHAP,
                    tao lo hang trong LOHANG_NGUYENLIEU, cong TONKHO va ghi NHATKY_KHO.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Thong tin phieu nhap kho",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateImportReceiptRequest.class),
                    examples = @ExampleObject(
                            name = "Nhap ca phe vao kho tong",
                            value = """
                                    {
                                      "maKho": 1,
                                      "maNhaCungCap": 1,
                                      "ghiChu": "Nhap bo sung dau ngay",
                                      "items": [
                                        {
                                          "maNguyenLieu": 1,
                                          "soLuongNhap": 1000,
                                          "donGiaNhap": 120000,
                                          "soLo": "LOT-001",
                                          "hanSuDung": "2027-12-31"
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tao phieu nhap thanh cong"),
            @ApiResponse(responseCode = "400", description = "Du lieu khong hop le", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen nhap kho", content = @Content),
            @ApiResponse(responseCode = "404", description = "Khong tim thay kho, nha cung cap hoac nguyen lieu", content = @Content)
    })
    @PostMapping("/imports")
    public ResponseEntity<BaseResponse<ImportReceiptResponse>> createImportReceipt(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateImportReceiptRequest request
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:IMPORT");
        return ResponseEntity.ok(BaseResponse.created(
                "Tao phieu nhap kho thanh cong",
                inventoryService.createImportReceipt(request, user)
        ));
    }

    @Operation(
            summary = "Tao phieu xuat kho",
            description = """
                    Tao phieu xuat trong PHIEUXUAT, luu chi tiet vao CHITIETPHIEUXUAT,
                    tru lo hang trong LOHANG_NGUYENLIEU, tru TONKHO va ghi NHATKY_KHO.
                    Mac dinh backend tu chon lo theo FEFO. Khi chonLoThuCong = true,
                    frontend gui danh sach loHangXuat cho tung dong nguyen lieu.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Thong tin phieu xuat kho",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = CreateExportReceiptRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Xuat FEFO tu dong",
                                    value = """
                                            {
                                              "maKho": 1,
                                              "loaiXuat": "TRAINING",
                                              "chonLoThuCong": false,
                                              "ghiChu": "Xuat nguyen lieu cho dao tao",
                                              "items": [
                                                {
                                                  "maNguyenLieu": 2,
                                                  "soLuongXuat": 300,
                                                  "donGiaXuat": 95
                                                }
                                              ]
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Xuat chon lo thu cong",
                                    value = """
                                            {
                                              "maKho": 1,
                                              "loaiXuat": "RETURN_SUPPLIER",
                                              "chonLoThuCong": true,
                                              "ghiChu": "Tra lai lo loi cho nha cung cap",
                                              "items": [
                                                {
                                                  "maNguyenLieu": 2,
                                                  "soLuongXuat": 300,
                                                  "donGiaXuat": 95,
                                                  "loHangXuat": [
                                                    {
                                                      "maLoHang": 5,
                                                      "soLuongXuat": 300
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tao phieu xuat thanh cong"),
            @ApiResponse(responseCode = "400", description = "Du lieu khong hop le hoac ton kho khong du", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen xuat kho", content = @Content),
            @ApiResponse(responseCode = "404", description = "Khong tim thay kho, nguyen lieu hoac lo hang", content = @Content)
    })
    @PostMapping("/exports")
    public ResponseEntity<BaseResponse<ExportReceiptResponse>> createExportReceipt(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateExportReceiptRequest request
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:EXPORT");
        return ResponseEntity.ok(BaseResponse.created(
                "Tao phieu xuat kho thanh cong",
                inventoryService.createExportReceipt(request, user)
        ));
    }

    @Operation(
            summary = "Lay ton kho kha dung cho man hinh xuat kho",
            description = "Frontend goi API nay sau khi chon kho xuat. Backend tra danh sach nguyen lieu con ton tai kho do, kem don vi tinh va so luong ton de nguoi dung lap phieu xuat. Can quyen INVENTORY:EXPORT. Response data la List<InventoryStockOptionResponse>."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lay ton kho xuat thanh cong"),
            @ApiResponse(responseCode = "400", description = "Thieu ma kho", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen xuat kho", content = @Content)
    })    @GetMapping("/exports/stock")
    public ResponseEntity<BaseResponse<List<InventoryStockOptionResponse>>> exportStock(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long maKho
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:EXPORT");
        return ResponseEntity.ok(BaseResponse.ok(
                "Lấy tồn kho xuất thành công",
                inventoryService.getExportStock(maKho, user)
        ));
    }

    @Operation(
            summary = "Lay danh sach ton kho",
            description = "Tra ve ton kho theo kho va nguyen lieu, co loc theo kho, nguyen lieu, tu khoa va trang thai ton kho."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lay danh sach ton kho thanh cong"),
            @ApiResponse(responseCode = "400", description = "Tham so phan trang hoac trang thai khong hop le", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen xem ton kho", content = @Content)
    })
    @GetMapping("/stock")
    public ResponseEntity<BaseResponse<PageResponse<InventoryResponse>>> stock(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Ma kho can loc", example = "1")
            @RequestParam(required = false) Long maKho,
            @Parameter(description = "Ma nguyen lieu can loc", example = "3")
            @RequestParam(required = false) Long maNguyenLieu,
            @Parameter(description = "Tu khoa tim theo ten kho hoac ten nguyen lieu", example = "sua")
            @RequestParam(required = false) String tuKhoa,
            @Parameter(description = "Trang thai ton kho: ON_DINH, TON_THAP, HET_HANG", example = "TON_THAP")
            @RequestParam(required = false) String trangThaiTonKho,
            @Parameter(description = "Trang hien tai, bat dau tu 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "So dong moi trang, toi da 100", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");
        return ResponseEntity.ok(BaseResponse.ok(
                "Lay danh sach ton kho thanh cong",
                inventoryService.getStock(maKho, maNguyenLieu, tuKhoa, trangThaiTonKho, page, size, user)
        ));
    }

    @Operation(
            summary = "Lay danh sach ton kho theo lo",
            description = "Tra ve cac lo hang con ton, sap xep theo kho, nguyen lieu va FEFO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lay danh sach lo ton kho thanh cong"),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen xem ton kho", content = @Content)
    })
    @GetMapping("/stock/lots")
    public ResponseEntity<BaseResponse<List<BatchInventoryResponse>>> stockLots(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Ma kho can loc", example = "1")
            @RequestParam(required = false) Long maKho,
            @Parameter(description = "Ma nguyen lieu can loc", example = "3")
            @RequestParam(required = false) Long maNguyenLieu
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");
        return ResponseEntity.ok(BaseResponse.ok(
                "Lay danh sach lo ton kho thanh cong",
                inventoryService.getBatchStock(maKho, maNguyenLieu, user)
        ));
    }

    @Operation(
            summary = "Lay tong quan ton kho",
            description = "Tra ve tong so nguyen lieu, tong so luong ton va so dong theo trang thai ton kho."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lay tong quan ton kho thanh cong"),
            @ApiResponse(responseCode = "401", description = "Chua dang nhap hoac token het han", content = @Content),
            @ApiResponse(responseCode = "403", description = "Khong co quyen xem ton kho", content = @Content)
    })
    @GetMapping("/stock/summary")
    public ResponseEntity<BaseResponse<TongQuanTonKhoResponse>> stockSummary(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Ma kho can xem tong quan", example = "1")
            @RequestParam(required = false) Long maKho
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:VIEW");
        return ResponseEntity.ok(BaseResponse.ok(
                "Lay tong quan ton kho thanh cong",
                inventoryService.getStockSummary(maKho, user)
        ));
    }

    @Operation(
            summary = "Lay du lieu combobox cho man hinh dieu chuyen kho",
            description = "Tra ve danh sach kho nguon va kho dich de frontend render form dieu chuyen kho."
    )
    @GetMapping("/transfers/lookups")
    public ResponseEntity<BaseResponse<InventoryTransferLookupResponse>> transferLookups(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authGuard.requirePermission(authHeader, "INVENTORY:TRANSFER");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lay du lieu dieu chuyen kho thanh cong",
                inventoryService.getTransferLookup()
        ));
    }

    @Operation(
            summary = "Lay danh sach nguyen lieu con ton de dieu chuyen",
            description = "Dung cho bang danh sach nguyen lieu theo kho nguon."
    )
    @GetMapping("/transfers/stock")
    public ResponseEntity<BaseResponse<List<InventoryStockOptionResponse>>> transferStock(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long maKhoNguon
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:TRANSFER");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lay ton kho nguon thanh cong",
                inventoryService.getTransferStock(maKhoNguon, user)
        ));
    }

    @Operation(
            summary = "Lay danh sach lo con ton de dieu chuyen kho",
            description = "Dung cho che do chon lo thu cong. Danh sach sap xep theo FEFO."
    )
    @GetMapping("/transfers/lots")
    public ResponseEntity<BaseResponse<List<InventoryLotResponse>>> transferLots(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long maKhoNguon,
            @RequestParam Long maNguyenLieu
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:TRANSFER");

        return ResponseEntity.ok(BaseResponse.ok(
                "Lay danh sach lo dieu chuyen thanh cong",
                inventoryService.getLotsForTransfer(maKhoNguon, maNguyenLieu, user)
        ));
    }

    @Operation(
            summary = "Tao phieu dieu chuyen kho",
            description = """
                Tao phieu dieu chuyen trong PHIEUDIEUCHUYEN, luu chi tiet vao CHITIETPHIEUDIEUCHUYEN,
                tru lo hang va TONKHO kho nguon, tao lo hang va cong TONKHO kho dich.
                Ghi NHATKY_KHO voi TRANSFER_OUT cho kho nguon va TRANSFER_IN cho kho dich.
                Mac dinh backend tu chon lo theo FEFO. Khi chonLoThuCong = true,
                frontend gui danh sach loHangDieuChuyen cho tung dong nguyen lieu.
                """
    )
    @PostMapping("/transfers")
    public ResponseEntity<BaseResponse<TransferReceiptResponse>> createTransferReceipt(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateTransferReceiptRequest request
    ) {
        SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:TRANSFER");

        return ResponseEntity.ok(BaseResponse.created(
                "Tao phieu dieu chuyen kho thanh cong",
                inventoryService.createTransferReceipt(request, user)
        ));
    }
}
