# LUỒNG CHI TIẾT CÁC CHỨC NĂNG CHÍNH XÁC

Tài liệu này mô tả luồng từ frontend Swing đến backend Spring Boot cho các chức năng đang có trong project. Mỗi luồng ghi rõ: người dùng bấm nút nào, frame gọi hàm nào, API client gọi endpoint nào, request/response class nào được dùng, backend controller/service xử lý gì và trả dữ liệu về đâu.

Quy ước chung:

- Frontend nằm trong `coffee-chain-management-frontend/src/main/java/com/coffeechain`.
- Backend nằm trong `coffee-chain-management-backend/src/main/java/com/coffeechain`.
- URL gốc frontend dùng trong `com.coffeechain.config.ApiConfig`: `http://localhost:8080`.
- Các API cần đăng nhập gửi header `Authorization: Bearer <token>` thông qua `SessionManager.getToken()` và `ApiClientSupport.bearerToken()`.
- Response chuẩn backend là `BaseResponse<T>` gồm `success`, `statusCode`, `message`, `data`.
- Khi Swagger hiển thị `Response class: BaseResponse<LoginResponse>` thì payload thực tế nằm trong `data`.

## 1. Điều hướng tổng thể

### 1.1. Đăng nhập vào hệ thống

Frontend:

1. Mở app tại `LoginFrame`.
2. Người dùng nhập username/password.
3. Bấm nút `Sign in`.
4. `LoginFrame.doLogin()` chạy trong background thread.
5. `AuthApiClient.login(username, password)` được gọi.

API:

- Method: `POST /api/auth/login`
- Request class frontend/backend: `LoginRequest`
- Response class: `BaseResponse<LoginResponse>`

Request body:

```json
{
  "tenDangNhap": "admin",
  "matKhau": "admin123"
}
```

Backend:

1. `AuthController.login(LoginRequest request)` nhận request.
2. Gọi `AuthService.login(request)`.
3. `AuthService` kiểm tra username/password qua `NguoiDungRepository.findByTenDangNhap`.
4. Kiểm tra password bằng `PasswordUtil.verifyPassword`.
5. Nếu hợp lệ, tạo token qua `TokenStore.createToken`.
6. Trả `LoginResponse` gồm `token` và `UserInfoResponse user`.

Frontend nhận `LoginResponse`, gọi `SessionManager.saveSession(response.getToken(), response.getUser())`, mở `MenuTongFrame` và đóng login.

### 1.2. Đăng xuất

Frontend:

- Các header/menu gọi `FrameNavigator.logout(owner, authApiClient)` hoặc `AuthApiClient.logout()`.

API:

- Method: `POST /api/auth/logout`
- Request class: không có body
- Response class: `BaseResponse<Void>`

Backend:

1. `AuthController.logout(authHeader)` đọc token.
2. Xóa token khỏi `TokenStore`.
3. Trả thành công.

Frontend xóa session trong `SessionManager.clearSession()` và mở lại `LoginFrame`.

### 1.3. Load thông tin user/quyền hiện tại

Frontend:

- `AuthApiClient.me()` gọi khi cần user hiện tại.
- `AuthApiClient.permissions()` hoặc dữ liệu đã lưu trong `SessionManager` dùng để render menu theo quyền.
- `PermissionUtil.hasAny(...)` được các frame dùng để ẩn/hiện chức năng.

API:

- `GET /api/auth/me` → `BaseResponse<UserInfoResponse>`
- `GET /api/auth/permissions` → `BaseResponse<Set<String>>`
- `GET /api/auth/check?permission=INVENTORY:IMPORT` → `BaseResponse<PermissionCheckResponse>`

Backend dùng `AuthGuard` để xác thực token và lấy quyền từ `SessionUser.permissions`.

## 2. Quên mật khẩu

Frontend frame: `ForgotPasswordFrame`.

### 2.1. Gửi mã xác nhận

Người dùng bấm `Forgot Password?` ở `LoginFrame`, nhập email, bấm `Gửi mã`.

Frontend:

- `sendCodeButton.addActionListener(e -> requestCode())`.
- `ForgotPasswordFrame.requestCode()` gọi `AuthApiClient.forgotPassword(email)`.

API:

- Method: `POST /api/auth/forgot-password`
- Request class: `ForgotPasswordRequest`
- Response class: `BaseResponse<ForgotPasswordResponse>`

Request:

```json
{
  "email": "admin@phungloc.local"
}
```

Backend:

1. `PasswordResetController.forgotPassword(ForgotPasswordRequest request)`.
2. Kiểm tra email tồn tại trong `NGUOIDUNG`.
3. Sinh mã xác nhận, lưu tạm và gửi mail qua JavaMail nếu cấu hình mail đúng.
4. Trả `ForgotPasswordResponse`.

### 2.2. Xác nhận mã

Frontend bấm `Xác nhận mã`, `ForgotPasswordFrame.verifyCode()` gọi `AuthApiClient.verifyResetCode(email, code)`.

API:

- Method: `POST /api/auth/forgot-password/verify`
- Request class: `VerifyResetCodeRequest`
- Response class: `BaseResponse<Void>`

### 2.3. Đổi mật khẩu

Frontend bấm `Đổi mật khẩu`, `ForgotPasswordFrame.resetPassword()` gọi `AuthApiClient.resetPassword(email, code, newPassword)`.

API:

- Method: `POST /api/auth/forgot-password/reset`
- Request class: `ResetPasswordRequest`
- Response class: `BaseResponse<Void>`

Backend kiểm tra mã, hash mật khẩu mới, cập nhật `NGUOIDUNG.mat_khau`, khóa mã đã dùng.

## 3. Menu và phân quyền hiển thị

### 3.1. Menu tổng

Frontend frame: `MenuTongFrame`.

Luồng:

1. Sau login, `MenuTongFrame` đọc quyền từ `SessionManager.getUser().getPermissions()`.
2. `buildCards()` gọi `PermissionUtil.hasAny(...)`.
3. Tùy quyền mà thêm card:
   - `Quản lý kho` → `FrameNavigator.open(this, new KhoMenuFrame())`
   - `Quản lý POS` → `QuanLyPOSFrame` hoặc placeholder
   - `Quản trị hệ thống` → `QuanTriHeThongFrame`
   - `Quản lý chi nhánh` → `QuanLyChiNhanhFrame` khi frame được nối thật

Không gọi API trực tiếp, dùng dữ liệu quyền từ session.

### 3.2. Menu kho

Frontend frame: `KhoMenuFrame`.

Card và frame đích:

- `Nhập kho` → `new NhapKhoFrame()`
- `Xuất kho` → `new XuatKhoFrame()`
- `Điều chuyển kho` → `new DieuChuyenKhoFrame()`
- `Kiểm kho` → `new KiemKhoFrame()`
- `Báo cáo hao hụt` → `new BaoCaoHaoHutFrame()`
- `Xem tồn kho` → `new XemTonKhoFrame()`
- `Theo dõi HSD` → `new TheoDoiHanSuDungFrame()`
- `Lịch sử` → `new TraCuuLichSuKhoFrame()`
- `Quản lý kho` → `new QuanLyKhoFrame()`
- `Quản lý nguyên liệu` → `new QuanLyNguyenLieuFrame()`
- `Quản lý nhà cung cấp` → `new QuanLyNhaCungCapFrame()`
- `Quản lý đơn vị tính` → `new QuanLyDonViTinhFrame()`

Mỗi card có quyền riêng qua `PermissionUtil`.
## 4. Nhập kho

Frontend frame: `NhapKhoFrame`.
Frontend client: `InventoryApiClient`.
Backend controller/service: `InventoryController`, `InventoryService`.

### 4.1. Mở màn hình nhập kho

Frontend:

1. User bấm card `Nhập kho` trong `KhoMenuFrame`.
2. `KhoMenuFrame.handleMenuClick()` mở `new NhapKhoFrame()`.
3. Constructor `NhapKhoFrame()` dựng UI rồi gọi hàm load dữ liệu ban đầu.
4. `InventoryApiClient.getImportLookup()` được gọi.

API:

- Method: `GET /api/inventory/imports/lookups`
- Request class: không có body
- Response class: `BaseResponse<InventoryLookupResponse>`

Backend:

1. `InventoryController.importLookups(authHeader)` kiểm tra quyền `INVENTORY:IMPORT`.
2. Gọi `InventoryService.getImportLookup()`.
3. Service lấy danh sách kho nhập, nhà cung cấp, nguyên liệu.
4. Trả `InventoryLookupResponse` gồm `warehouses`, `suppliers`, `ingredients`.

Frontend nhận response và đổ dữ liệu vào combo kho, combo nhà cung cấp, bảng nguyên liệu và combo nguyên liệu.

### 4.2. Tìm nguyên liệu trong màn nhập

- Người dùng nhập vào ô tìm kiếm hoặc bấm `Tìm`.
- `NhapKhoFrame.filterIngredientTable(keyword)` lọc local trên danh sách `allIngredients` đã load từ lookup.
- Không gọi API.

### 4.3. Thêm dòng vào phiếu nhập

1. Chọn nguyên liệu.
2. Nhập số lượng, đơn giá, số lô, hạn sử dụng.
3. Bấm `Thêm vào phiếu`.
4. `NhapKhoFrame.addLine()` validate dữ liệu.
5. Tạo dòng trong model bảng phiếu nhập local.

Không gọi API cho đến khi bấm lưu phiếu.

### 4.4. Lưu phiếu nhập

Frontend:

1. Bấm `Lưu phiếu`.
2. `NhapKhoFrame.saveReceipt()` gom dữ liệu form.
3. Tạo `CreateImportReceiptRequest` gồm `maKho`, `maNhaCungCap`, `ghiChu`, `items`.
4. Gọi `InventoryApiClient.createImportReceipt(request)`.

API:

- Method: `POST /api/inventory/imports`
- Request class: `CreateImportReceiptRequest`
- Item class: `CreateImportReceiptItemRequest`
- Response class: `BaseResponse<ImportReceiptResponse>`

Request mẫu:

```json
{
  "maKho": 1,
  "maNhaCungCap": 1,
  "ghiChu": "Nhập bổ sung đầu ngày",
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
```

Backend:

1. `InventoryController.createImportReceipt(authHeader, request)` kiểm tra quyền `INVENTORY:IMPORT`.
2. Gọi `InventoryService.createImportReceipt(request, user)`.
3. Service validate kho, nhà cung cấp, nguyên liệu, số lượng, đơn giá.
4. Tạo `PHIEUNHAP`.
5. Tạo `CHITIETPHIEUNHAP` cho từng item.
6. Tạo/cập nhật `LOHANG_NGUYENLIEU`.
7. Cộng `TONKHO`.
8. Ghi `NHATKY_KHO` loại `IMPORT`.
9. Trả `ImportReceiptResponse`.

## 5. Xuất kho

Frontend frame: `XuatKhoFrame`.
Frontend client: `InventoryApiClient`.

### 5.1. Mở màn hình xuất kho

1. User bấm `Xuất kho` trong `KhoMenuFrame`.
2. Mở `XuatKhoFrame`.
3. Frame gọi `InventoryApiClient.getExportLookup()`.

API:

- Method: `GET /api/inventory/exports/lookups`
- Request class: không có body
- Response class: `BaseResponse<InventoryExportLookupResponse>`

Backend:

- `InventoryController.exportLookups()` kiểm tra `INVENTORY:EXPORT`.
- `InventoryService.getExportLookup()` trả danh sách kho, nguyên liệu, loại xuất.

### 5.2. Chọn kho xuất và load tồn nguồn

Frontend:

- Khi đổi `warehouseCombo`, listener gọi hàm load tồn theo kho.
- `InventoryApiClient.getExportStock(maKho)` gọi API.

API:

- Method: `GET /api/inventory/exports/stock?maKho={id}`
- Request class: không có body
- Response class: `BaseResponse<List<InventoryStockOptionResponse>>`

Backend:

- `InventoryController.exportStock(authHeader, maKho)` kiểm tra `INVENTORY:EXPORT`.
- `InventoryService.getExportStock(maKho, user)` trả nguyên liệu còn tồn tại kho được phép.

### 5.3. Chọn lô thủ công

1. Tick `Chọn lô thủ công`.
2. Chọn nguyên liệu.
3. `XuatKhoFrame.refreshLotsIfManual()` gọi `InventoryApiClient.getExportLots(maKho, maNguyenLieu)`.

API:

- Method: `GET /api/inventory/exports/lots?maKho={id}&maNguyenLieu={id}`
- Request class: không có body
- Response class: `BaseResponse<List<InventoryLotResponse>>`

Backend trả danh sách lô còn tồn, sắp xếp theo FEFO: `han_su_dung ASC NULLS LAST`, rồi `ngay_tao`.

### 5.4. Lưu phiếu xuất

Frontend:

1. Bấm `Thêm vào phiếu` để đưa dòng vào bảng local.
2. Bấm `Lưu phiếu`.
3. `XuatKhoFrame.saveReceipt()` tạo `CreateExportReceiptRequest`.
4. Gọi `InventoryApiClient.createExportReceipt(request)`.

API:

- Method: `POST /api/inventory/exports`
- Request class: `CreateExportReceiptRequest`
- Item class: `CreateExportReceiptItemRequest`
- Lot selection class: `ExportLotSelectionRequest`
- Response class: `BaseResponse<ExportReceiptResponse>`

Request FEFO tự động:

```json
{
  "maKho": 1,
  "loaiXuat": "TRAINING",
  "chonLoThuCong": false,
  "ghiChu": "Xuất đào tạo",
  "items": [
    {
      "maNguyenLieu": 2,
      "soLuongXuat": 300,
      "donGiaXuat": 95
    }
  ]
}
```

Backend:

1. `InventoryController.createExportReceipt()` kiểm tra `INVENTORY:EXPORT`.
2. `InventoryService.createExportReceipt(request, user)` validate tồn.
3. Nếu `chonLoThuCong=false`, backend tự phân bổ lô theo FEFO.
4. Nếu `chonLoThuCong=true`, backend dùng danh sách `loHangXuat` frontend gửi.
5. Tạo `PHIEUXUAT`, `CHITIETPHIEUXUAT`.
6. Trừ `LOHANG_NGUYENLIEU`.
7. Trừ `TONKHO`.
8. Ghi `NHATKY_KHO` loại xuất tương ứng.
9. Trả `ExportReceiptResponse`.

## 6. Điều chuyển kho

Frontend frame: `DieuChuyenKhoFrame`.
Frontend client: `InventoryApiClient`.
Backend: `InventoryController`, `InventoryService`.

### 6.1. Mở màn hình điều chuyển

1. User bấm `Điều chuyển kho`.
2. Mở `DieuChuyenKhoFrame`.
3. Frame gọi `InventoryApiClient.getTransferLookup()`.

API:

- Method: `GET /api/inventory/transfers/lookups`
- Request class: không có body
- Response class: `BaseResponse<InventoryTransferLookupResponse>`

Backend kiểm tra `INVENTORY:TRANSFER` và trả danh sách kho nguồn/kho đích.

### 6.2. Chọn kho nguồn để load tồn

- Listener `sourceCombo.addActionListener(e -> loadStockForSource())`.
- `loadStockForSource()` gọi `InventoryApiClient.getTransferStock(maKhoNguon)`.

API:

- Method: `GET /api/inventory/transfers/stock?maKho={id}`
- Response class: `BaseResponse<List<InventoryStockOptionResponse>>`

### 6.3. Chọn lô thủ công cho điều chuyển

- Tick `Chọn lô thủ công` hoặc đổi nguyên liệu.
- `DieuChuyenKhoFrame.loadLotsForSelectedIngredient()` gọi `InventoryApiClient.getTransferLots(maKhoNguon, maNguyenLieu)`.

API:

- Method: `GET /api/inventory/transfers/lots?maKho={id}&maNguyenLieu={id}`
- Response class: `BaseResponse<List<InventoryLotResponse>>`

### 6.4. Lưu phiếu điều chuyển

1. Bấm `Thêm vào phiếu` → `DieuChuyenKhoFrame.addLine()`.
2. Bấm `Lưu phiếu` → `DieuChuyenKhoFrame.saveTransfer()`.
3. Tạo `CreateTransferReceiptRequest`.
4. Gọi `InventoryApiClient.createTransferReceipt(request)`.

API:

- Method: `POST /api/inventory/transfers`
- Request class: `CreateTransferReceiptRequest`
- Item class: `CreateTransferReceiptItemRequest`
- Lot selection class: `TransferLotSelectionRequest`
- Response class: `BaseResponse<TransferReceiptResponse>`

Request mẫu:

```json
{
  "maKhoNguon": 1,
  "maKhoDich": 2,
  "chonLoThuCong": false,
  "ghiChu": "Điều chuyển về chi nhánh",
  "items": [
    {
      "maNguyenLieu": 2,
      "soLuongDieuChuyen": 3000
    }
  ]
}
```

Backend:

1. `InventoryController.createTransferReceipt()` kiểm tra `INVENTORY:TRANSFER`.
2. `InventoryService.createTransferReceipt(request, user)` validate kho nguồn/đích khác nhau.
3. Nếu không chọn lô, tự phân bổ lô nguồn theo FEFO.
4. Tạo `PHIEUDIEUCHUYEN`, `CHITIETPHIEUDIEUCHUYEN`.
5. Trừ lô và tồn kho nguồn.
6. Tạo/cộng lô tại kho đích.
7. Cộng tồn kho đích.
8. Ghi `NHATKY_KHO` `TRANSFER_OUT` và `TRANSFER_IN`.
9. Trả `TransferReceiptResponse`.
6. Nếu database không đổi sau thao tác kho, kiểm tra service có cập nhật đủ: chứng từ nghiệp vụ, chi tiết chứng từ, `LOHANG_NGUYENLIEU`, `TONKHO`, `NHATKY_KHO`.
## 7. Xem tồn kho

### 7.1 Frontend mở màn hình

Từ `KhoMenuFrame`, khi bấm card `Xem tồn kho`, hàm điều hướng mở `XemTonKhoFrame`.

`XemTonKhoFrame` khởi tạo các vùng chính:

- Header và nút `Quay lại`.
- Các thẻ tổng quan tồn kho.
- Bộ lọc: từ khóa, kho, nguyên liệu, trạng thái.
- Bảng `Danh sách tồn kho`.
- Bảng `Lô hàng còn tồn`.
- Phân trang.

Khi frame mở, luồng chính gọi:

```text
XemTonKhoFrame.<init>()
-> loadLookups()
-> loadData(false)
```

### 7.2 Load combobox

Frontend gọi:

```text
InventoryApiClient.getImportLookups()
```

API:

```http
GET /api/inventory/imports/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<InventoryLookupResponse>
```

`InventoryLookupResponse` gồm:

- `warehouses`: danh sách kho.
- `suppliers`: danh sách nhà cung cấp, màn này có thể không dùng.
- `ingredients`: danh sách nguyên liệu, `description` là ký hiệu đơn vị tính.

Frontend dùng `warehouses` để render combobox kho và `ingredients` để render combobox nguyên liệu.

### 7.3 Bấm nút Lọc

Khi bấm `Lọc`, frontend gọi:

```text
XemTonKhoFrame.loadData(false)
-> InventoryApiClient.getInventoryStock(...)
-> InventoryApiClient.getInventoryLots(...)
-> InventoryApiClient.getInventorySummary(...)
```

API danh sách tồn kho:

```http
GET /api/inventory/stock?maKho=&maNguyenLieu=&keyword=&trangThai=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body, dùng query params.

Query params:

- `maKho`: lọc theo kho, bỏ trống là tất cả kho.
- `maNguyenLieu`: lọc theo nguyên liệu, bỏ trống là tất cả nguyên liệu.
- `keyword`: tìm theo tên kho hoặc nguyên liệu.
- `trangThai`: trạng thái tồn kho, ví dụ `ON_DINH`, `TON_THAP`, `HET_HANG`.
- `page`: trang hiện tại, bắt đầu từ 0.
- `size`: số dòng mỗi trang.

Response class:

```text
BaseResponse<PageResponse<InventoryStockResponse>>
```

`InventoryStockResponse` thể hiện một dòng tồn kho theo kho và nguyên liệu, thường gồm:

- mã tồn kho.
- mã kho, tên kho, loại kho.
- mã nguyên liệu, tên nguyên liệu.
- đơn vị tính.
- số lượng tồn.
- mức tồn tối thiểu.
- trạng thái tồn kho.
- lần cập nhật cuối.

Backend xử lý:

```text
InventoryController.getInventoryStock(...)
-> InventoryService.getInventoryStock(...)
-> InventoryRepository truy vấn TONKHO
   JOIN KHO
   JOIN NGUYENLIEU
   JOIN DONVITINH
-> tính trạng thái tồn kho
-> trả PageResponse<InventoryStockResponse>
```

### 7.4 Load bảng Lô hàng còn tồn

Frontend gọi API:

```http
GET /api/inventory/stock/lots?maKho=&maNguyenLieu=
Authorization: Bearer <token>
```

Request class: không có body, dùng query params.

Response class:

```text
BaseResponse<List<InventoryLotResponse>>
```

`InventoryLotResponse` gồm các thông tin lô:

- `maLoHang`.
- `maKho`, `tenKho`.
- `maNguyenLieu`, `tenNguyenLieu`.
- `kyHieu` hoặc đơn vị tính.
- `soLuongConLai`.
- `trangThai`.
- `hanSuDung`.
- `ngayTao`.

Backend xử lý:

```text
InventoryController.getInventoryStockLots(...)
-> InventoryService.getInventoryStockLots(...)
-> InventoryRepository truy vấn LOHANG_NGUYENLIEU
   JOIN KHO
   JOIN NGUYENLIEU
   JOIN DONVITINH
WHERE so_luong_con_lai > 0
ORDER BY han_su_dung ASC NULLS LAST, ngay_tao ASC
```

### 7.5 Load thẻ tổng quan

Frontend gọi API:

```http
GET /api/inventory/stock/summary?maKho=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<InventoryStockSummaryResponse>
```

Response dùng để render các ô tổng quan như:

- tổng số lượng tồn.
- số dòng tồn kho.
- số nguyên liệu tồn thấp.
- số nguyên liệu hết hàng.
- số nguyên liệu ổn định.

### 7.6 Bấm Reset

Frontend gọi:

```text
XemTonKhoFrame.resetFilters()
-> clear từ khóa
-> chọn lại tất cả kho/nguyên liệu/trạng thái
-> page = 0
-> loadData(false)
```

Không gọi API riêng cho reset. Reset chỉ là thao tác local rồi gọi lại API danh sách.

## 8. Theo dõi hạn sử dụng

### 8.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Theo dõi HSD`, frontend mở `TheoDoiHanSuDungFrame`.

Khi frame khởi tạo:

```text
TheoDoiHanSuDungFrame.<init>()
-> loadLookups()
-> loadData(false)
```

Mục tiêu màn hình:

- Theo dõi lô còn tồn theo hạn sử dụng.
- Cảnh báo lô sắp hết hạn.
- Rà soát và cập nhật trạng thái lô quá hạn nếu backend có hỗ trợ.

### 8.2 Load combobox

Frontend gọi:

```text
InventoryApiClient.getExpiryLookups()
```

API:

```http
GET /api/inventory/expiry/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<ExpiryLookupResponse>
```

`ExpiryLookupResponse` gồm:

- `warehouses`: danh sách kho.
- `ingredients`: danh sách nguyên liệu.
- các option lọc trạng thái hoặc mức cảnh báo nếu backend trả về.

Backend xử lý:

```text
ExpiryController.getLookups()
-> ExpiryService.getLookups()
-> truy vấn kho/nguyên liệu cần hiển thị trên màn hình
```

### 8.3 Bấm Lọc

Frontend gọi:

```text
TheoDoiHanSuDungFrame.loadData(false)
-> InventoryApiClient.getExpiryLots(...)
-> InventoryApiClient.getExpiryStatistics(...)
```

API danh sách lô theo hạn sử dụng:

```http
GET /api/inventory/expiry/lots?maKho=&maNguyenLieu=&keyword=&trangThaiLo=&mucCanhBao=&daysToExpire=&onlyAvailable=
Authorization: Bearer <token>
```

Request class: không có body, dùng query params.

Query params thường dùng:

- `maKho`: lọc theo kho.
- `maNguyenLieu`: lọc theo nguyên liệu.
- `keyword`: tìm theo tên kho, tên nguyên liệu hoặc mã lô.
- `trangThaiLo`: trạng thái lô, ví dụ `ACTIVE`, `EXPIRED`, `USED_UP`.
- `mucCanhBao`: ví dụ `SAP_HET_HAN`, `DA_HET_HAN`, `KHONG_CO_HSD`.
- `daysToExpire`: ngưỡng số ngày cảnh báo.
- `onlyAvailable`: nếu true thì chỉ lấy lô còn tồn.

Response class:

```text
BaseResponse<List<ExpiryLotResponse>>
```

`ExpiryLotResponse` gồm:

- mã lô.
- kho.
- nguyên liệu.
- đơn vị tính.
- số lượng còn lại.
- hạn sử dụng.
- còn bao nhiêu ngày.
- trạng thái lô.
- mức cảnh báo.

Backend xử lý:

```text
ExpiryController.getExpiryLots(...)
-> ExpiryService.getExpiryLots(...)
-> InventoryRepository hoặc ExpiryRepository truy vấn LOHANG_NGUYENLIEU
   JOIN KHO
   JOIN NGUYENLIEU
   JOIN DONVITINH
-> tính số ngày còn lại dựa trên han_su_dung
-> phân loại cảnh báo
```

### 8.4 Load thống kê hạn sử dụng

Frontend gọi:

```http
GET /api/inventory/expiry/statistics?maKho=&maNguyenLieu=&daysToExpire=&onlyAvailable=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<ExpiryStatisticsResponse>
```

Response dùng cho các thẻ tổng quan:

- tổng số lô.
- số lô đang hoạt động.
- số lô sắp hết hạn.
- số lô đã hết hạn.
- số lô không có hạn sử dụng.

### 8.5 Bấm Rà soát HSD

Frontend gọi:

```text
TheoDoiHanSuDungFrame.refreshExpiredLots()
-> InventoryApiClient.refreshExpiryLots()
```

API:

```http
POST /api/inventory/expiry/refresh
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<ExpiryRefreshResponse>
```

Backend xử lý:

```text
ExpiryController.refreshExpiredLots()
-> ExpiryService.refreshExpiredLots()
-> tìm các lô có han_su_dung < ngày hiện tại và còn tồn
-> cập nhật trạng thái hoặc trả số lượng lô được rà soát
```

Sau khi API thành công, frontend gọi lại `loadData(false)` để bảng và thẻ thống kê đồng bộ.

## 9. Tra cứu lịch sử kho

### 9.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Lịch sử`, frontend mở `TraCuuLichSuKhoFrame`.

Luồng khởi tạo:

```text
TraCuuLichSuKhoFrame.<init>()
-> loadLookups()
-> loadData(false)
```

Mục tiêu màn hình:

- Xem lịch sử nhập, xuất, điều chuyển, kiểm kho, hao hụt, bán hàng trừ kho.
- Lọc theo kho, nguyên liệu, loại giao dịch, thời gian.

### 9.2 Load combobox

Frontend gọi:

```text
InventoryApiClient.getHistoryLookups()
```

API:

```http
GET /api/inventory/history/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<InventoryHistoryLookupResponse>
```

Response gồm:

- danh sách kho.
- danh sách nguyên liệu.
- danh sách loại giao dịch.

### 9.3 Bấm Lọc

Frontend gọi:

```text
TraCuuLichSuKhoFrame.loadData(false)
-> InventoryApiClient.getInventoryHistory(...)
```

API:

```http
GET /api/inventory/history?maKho=&maNguyenLieu=&loaiGiaoDich=&keyword=&fromDate=&toDate=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body, dùng query params.

Response class:

```text
BaseResponse<PageResponse<InventoryHistoryResponse>>
```

`InventoryHistoryResponse` đại diện một dòng nhật ký kho, gồm:

- mã nhật ký.
- kho.
- nguyên liệu.
- loại giao dịch.
- tên chứng từ.
- mã chứng từ.
- số lượng thay đổi.
- số lượng trước.
- số lượng sau.
- người thao tác.
- thời gian thao tác.

Backend xử lý:

```text
InventoryHistoryController.getHistory(...)
-> InventoryHistoryService.getHistory(...)
-> InventoryHistoryRepository truy vấn NHATKY_KHO
   JOIN KHO
   JOIN NGUYENLIEU
   LEFT JOIN NGUOIDUNG
-> áp dụng bộ lọc
-> trả PageResponse<InventoryHistoryResponse>
```

### 9.4 Load tổng quan lịch sử

Frontend gọi:

```http
GET /api/inventory/history/summary?maKho=&maNguyenLieu=&fromDate=&toDate=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<InventoryHistorySummaryResponse>
```

Response dùng để hiển thị tổng số giao dịch, tổng nhập, tổng xuất, tổng điều chuyển hoặc các chỉ số lịch sử khác.
## 10. Báo cáo hao hụt

### 10.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Báo cáo hao hụt`, frontend mở `BaoCaoHaoHutFrame`.

Luồng khởi tạo:

```text
BaoCaoHaoHutFrame.<init>()
-> loadLookups()
-> loadLotsAndHistory()
```

Màn hình chia thành:

- Thông tin báo cáo: lọc kho, nguyên liệu, loại hao hụt để xem lịch sử.
- Bảng lô hàng còn tồn.
- Bảng phiếu hao hụt gần đây.
- Chi tiết hao hụt: chọn lô, nhập số lượng hao hụt, chọn loại hao hụt dùng để lưu.
- Ghi chú và nút lưu báo cáo.

Lưu ý luồng hiện tại: combobox `Loại hao hụt` ở phần thông tin phía trên dùng để lọc lịch sử. Combobox `Loại hao hụt` trong phần chi tiết mới là loại được gửi khi bấm `Lưu báo cáo`.

### 10.2 Load dữ liệu combobox

Frontend gọi:

```text
InventoryApiClient.getWastageLookups()
```

API:

```http
GET /api/inventory/wastages/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<WastageLookupResponse>
```

`WastageLookupResponse` gồm:

- danh sách kho.
- danh sách nguyên liệu.
- danh sách loại hao hụt.

Backend xử lý:

```text
WastageController.getLookups()
-> WastageService.getLookups()
-> lấy option kho, nguyên liệu, loại hao hụt
```

### 10.3 Chọn kho/nguyên liệu hoặc bấm Tải lại lô

Frontend gọi:

```text
BaoCaoHaoHutFrame.loadLotsAndHistory()
-> InventoryApiClient.getWastageLots(...)
-> InventoryApiClient.getWastages(...)
```

API lấy lô còn tồn:

```http
GET /api/inventory/wastages/lots?maKho=&maNguyenLieu=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<List<WastageLotResponse>>
```

`WastageLotResponse` gồm:

- mã lô.
- mã kho, tên kho.
- mã nguyên liệu, tên nguyên liệu.
- đơn vị tính.
- số lượng còn lại.
- hạn sử dụng.
- trạng thái lô.

Backend xử lý:

```text
WastageController.getAvailableLots(...)
-> WastageService.getAvailableLots(...)
-> truy vấn LOHANG_NGUYENLIEU còn tồn theo kho/nguyên liệu
```

API lấy phiếu hao hụt gần đây:

```http
GET /api/inventory/wastages?maKho=&maNguyenLieu=&loaiHaoHut=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<WastageResponse>>
```

### 10.4 Bấm Lưu báo cáo

Frontend gọi:

```text
BaoCaoHaoHutFrame.saveWastage()
-> đọc selected lot từ lotCombo
-> đọc số lượng hao hụt
-> đọc loại hao hụt ở combobox chi tiết
-> đọc ghi chú
-> InventoryApiClient.createWastage(request)
```

API:

```http
POST /api/inventory/wastages
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
CreateWastageRequest
```

Body mẫu:

```json
{
  "maKho": 1,
  "maNguyenLieu": 3,
  "maLoHang": 23,
  "soLuongHaoHut": 50,
  "loaiHaoHut": "SPILL",
  "ghiChu": "Đổ vỡ trong ca làm việc"
}
```

Response class:

```text
BaseResponse<WastageResponse>
```

Backend xử lý:

```text
WastageController.createWastage(request)
-> WastageService.createWastage(request)
-> kiểm tra quyền WASTAGE:CREATE hoặc quyền tương ứng
-> kiểm tra kho/nguyên liệu/lô tồn tại
-> kiểm tra số lượng hao hụt > 0 và không vượt số lượng còn lại
-> INSERT PHIEUHAOHUT
-> trừ LOHANG_NGUYENLIEU.so_luong_con_lai
-> trừ TONKHO.so_luong_ton
-> INSERT NHATKY_KHO với loai_giao_dich = WASTAGE
-> trả WastageResponse
```

Sau khi lưu thành công, frontend:

```text
-> clear input số lượng/ghi chú nếu cần
-> loadLotsAndHistory()
-> phiếu mới xuất hiện ở bảng Phiếu hao hụt gần đây
```

## 11. Kiểm kho

### 11.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Kiểm kho`, frontend mở `KiemKhoFrame`.

Luồng khởi tạo:

```text
KiemKhoFrame.<init>()
-> loadLookups()
-> loadSystemStockAndHistory()
```

Màn hình gồm:

- Thông tin kiểm kho: kho kiểm, nguyên liệu, ngày kiểm, người kiểm.
- Bảng `Tồn hệ thống theo lô`.
- Bảng `Phiếu kiểm kho gần đây`.
- Chi tiết kiểm kho: chọn lô, nhập thực tế, chọn hướng xử lý, nhập lý do.
- Bảng `Dòng kiểm kho trong phiếu`.
- Ghi chú và các nút: mở phiếu, hủy phiếu, xóa dòng, hủy nhập, lưu nháp, hoàn tất.

Phân quyền đã đơn giản hóa:

- `STOCKTAKE:VIEW`: xem danh sách và dữ liệu kiểm kho.
- `STOCKTAKE:MANAGE`: tạo, lưu nháp, hoàn tất, cập nhật và hủy mềm phiếu kiểm kho.

### 11.2 Load combobox

Frontend gọi:

```text
InventoryApiClient.getStocktakeLookups()
```

API:

```http
GET /api/inventory/stocktakes/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<StocktakeLookupResponse>
```

`StocktakeLookupResponse` gồm:

- danh sách kho user được phép kiểm.
- danh sách nguyên liệu.
- trạng thái phiếu kiểm kho.
- hướng xử lý chênh lệch.

Backend xử lý:

```text
StocktakeController.getLookups()
-> StocktakeService.getLookups()
-> lọc kho theo quyền và chi nhánh user nếu là quản lý chi nhánh
```

### 11.3 Bấm Tải lại

Frontend gọi:

```text
KiemKhoFrame.loadSystemStockAndHistory()
-> InventoryApiClient.getStocktakeSystemStock(...)
-> InventoryApiClient.getStocktakes(...)
```

API lấy tồn hệ thống theo lô:

```http
GET /api/inventory/stocktakes/system-stock?maKho=&maNguyenLieu=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<List<StocktakeSystemStockResponse>>
```

`StocktakeSystemStockResponse` gồm:

- mã lô.
- kho.
- nguyên liệu.
- đơn vị tính.
- số lượng hệ thống.
- hạn sử dụng.
- trạng thái lô.

Backend xử lý:

```text
StocktakeController.getSystemStock(...)
-> StocktakeService.getSystemStock(...)
-> truy vấn LOHANG_NGUYENLIEU còn tồn theo kho/nguyên liệu
```

API lấy phiếu kiểm kho gần đây:

```http
GET /api/inventory/stocktakes?maKho=&maNguyenLieu=&trangThai=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<StocktakeResponse>>
```

### 11.4 Chọn lô và nhập thực tế

Khi chọn một lô trong combobox chi tiết, frontend gọi local:

```text
KiemKhoFrame.updateStockFieldsFromCombo()
```

Frontend tự fill:

- `Hệ thống`: số lượng tồn hệ thống.
- `Hạn sử dụng`: hạn của lô.
- `Chênh lệch`: tính sau khi nhập thực tế.

Khi nhập `Thực tế`, frontend tính:

```text
chenhLech = soLuongThucTe - soLuongHeThong
```

Không gọi API ở bước này.

### 11.5 Bấm Thêm

Frontend gọi:

```text
KiemKhoFrame.addOrUpdateLine()
```

Luồng local:

- Lấy lô đang chọn.
- Lấy số lượng thực tế.
- Tính chênh lệch.
- Lấy hướng xử lý và lý do.
- Thêm hoặc cập nhật dòng trong danh sách draftLines.
- Render lại bảng `Dòng kiểm kho trong phiếu`.

Chưa gọi backend.

### 11.6 Bấm Lưu nháp

Frontend gọi:

```text
KiemKhoFrame.saveStocktake(false)
-> InventoryApiClient.saveStocktake(request)
```

API:

```http
POST /api/inventory/stocktakes
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
SaveStocktakeRequest
```

Body mẫu:

```json
{
  "maKho": 1,
  "ghiChu": "Kiểm cuối ngày",
  "complete": false,
  "items": [
    {
      "maLoHang": 23,
      "maNguyenLieu": 3,
      "soLuongHeThong": 7000,
      "soLuongThucTe": 6950,
      "huongXuLy": "NO_ACTION",
      "lyDoChenhLech": "Lệch nhỏ sau ca"
    }
  ]
}
```

Response class:

```text
BaseResponse<StocktakeResponse>
```

Backend xử lý khi `complete = false`:

```text
StocktakeController.saveStocktake(request)
-> StocktakeService.saveStocktake(request)
-> kiểm tra quyền STOCKTAKE:MANAGE
-> tạo PHIEUKIEMKHO trạng thái DRAFT
-> tạo CHITIETPHIEUKIEMKHO
-> không điều chỉnh tồn kho ngay
-> trả StocktakeResponse
```

### 11.7 Bấm Hoàn tất

Frontend gọi:

```text
KiemKhoFrame.saveStocktake(true)
-> InventoryApiClient.saveStocktake(request)
```

API giống lưu nháp:

```http
POST /api/inventory/stocktakes
```

Request class vẫn là:

```text
SaveStocktakeRequest
```

Điểm khác: `complete = true`.

Backend xử lý khi `complete = true`:

```text
StocktakeService.saveStocktake(request)
-> tạo PHIEUKIEMKHO trạng thái COMPLETED
-> tạo CHITIETPHIEUKIEMKHO
-> tùy huongXuLy:
   NO_ACTION: chỉ ghi nhận chênh lệch
   ADJUST_INVENTORY: cập nhật LOHANG_NGUYENLIEU và TONKHO theo thực tế
   CREATE_WASTAGE: có thể tạo chứng từ hao hụt hoặc ghi nhận hướng xử lý theo logic backend
-> INSERT NHATKY_KHO nếu có thay đổi tồn
-> trả StocktakeResponse
```

### 11.8 Bấm Mở phiếu

Khi chọn một dòng trong bảng `Phiếu kiểm kho gần đây` rồi bấm `Mở phiếu`, frontend gọi:

```text
KiemKhoFrame.openSelectedStocktake()
-> InventoryApiClient.getStocktakeDetail(maPhieuKiemKho)
```

API:

```http
GET /api/inventory/stocktakes/{maPhieuKiemKho}
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<StocktakeResponse>
```

Backend xử lý:

```text
StocktakeController.getStocktakeDetail(maPhieuKiemKho)
-> StocktakeService.getStocktakeDetail(maPhieuKiemKho)
-> load PHIEUKIEMKHO và CHITIETPHIEUKIEMKHO
-> trả StocktakeResponse có danh sách dòng
```

Frontend dùng response để fill lại `draftLines`, ghi chú, kho, ngày kiểm và bảng dòng kiểm kho.

### 11.9 Bấm Hủy phiếu

Khi chọn phiếu rồi bấm `Hủy phiếu`, frontend gọi:

```text
KiemKhoFrame.cancelSelectedStocktake()
-> InventoryApiClient.cancelStocktake(maPhieuKiemKho)
```

API:

```http
DELETE /api/inventory/stocktakes/{maPhieuKiemKho}
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<StocktakeResponse>
```

Backend xử lý:

```text
StocktakeController.cancelStocktake(maPhieuKiemKho)
-> StocktakeService.cancelStocktake(maPhieuKiemKho)
-> kiểm tra quyền STOCKTAKE:MANAGE
-> chuyển trạng thái phiếu sang CANCELLED hoặc trạng thái hủy mềm tương ứng
-> không xóa vật lý dữ liệu
-> trả StocktakeResponse
```

Ghi chú nghiệp vụ: nếu backend đang thiết kế hủy phiếu không đảo tồn thì hủy chỉ là xóa mềm trạng thái. Nếu muốn hủy phiếu hoàn tất phải hoàn ngược tồn kho, backend cần có logic reverse riêng và phải ghi nhật ký đảo giao dịch.

## 12. Quản lý nhà cung cấp

### 12.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Quản lý nhà cung cấp`, frontend mở `QuanLyNhaCungCapFrame`.

Luồng khởi tạo:

```text
QuanLyNhaCungCapFrame.<init>()
-> loadSuppliers()
```

Màn hình gồm:

- Tìm kiếm.
- Bảng danh sách nhà cung cấp.
- Form chi tiết.
- Các nút thêm mới, lưu, xóa mềm hoặc đổi trạng thái, làm mới.

### 12.2 Load danh sách

Frontend gọi:

```text
QuanLyNhaCungCapFrame.loadSuppliers()
-> SupplierApiClient.getSuppliers(keyword, status, page, size)
```

API:

```http
GET /api/suppliers?keyword=&status=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<SupplierResponse>>
```

Backend xử lý:

```text
SupplierController.getSuppliers(...)
-> SupplierService.getSuppliers(...)
-> SupplierRepository query NHACUNGCAP
```

### 12.3 Chọn dòng trong bảng

Khi click một nhà cung cấp, frontend chỉ thao tác local:

```text
fillForm(selectedSupplier)
```

Không gọi API nếu danh sách đã đủ dữ liệu.

### 12.4 Bấm Lưu

Frontend gọi:

```text
QuanLyNhaCungCapFrame.saveSupplier()
-> nếu chưa có id: SupplierApiClient.createSupplier(request)
-> nếu có id: SupplierApiClient.updateSupplier(id, request)
```

API tạo mới:

```http
POST /api/suppliers
Authorization: Bearer <token>
Content-Type: application/json
```

API cập nhật:

```http
PUT /api/suppliers/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
SupplierRequest
```

Body mẫu:

```json
{
  "tenNhaCungCap": "Cà Phê Cao Nguyên",
  "soDienThoai": "0901000001",
  "email": "caphe@example.com",
  "diaChi": "Lâm Đồng",
  "trangThai": "ACTIVE"
}
```

Response class:

```text
BaseResponse<SupplierResponse>
```

Backend xử lý:

```text
SupplierController.create/update
-> SupplierService.create/update
-> validate dữ liệu
-> INSERT/UPDATE NHACUNGCAP
-> trả SupplierResponse
```

### 12.5 Bấm Xóa hoặc Ngưng hoạt động

Frontend gọi:

```text
QuanLyNhaCungCapFrame.deleteSupplier()
-> SupplierApiClient.deleteSupplier(id)
```

API:

```http
DELETE /api/suppliers/{id}
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<Void> hoặc BaseResponse<SupplierResponse>
```

Backend thường xử lý theo hướng xóa mềm, cập nhật trạng thái nhà cung cấp thay vì xóa vật lý để không mất liên kết với phiếu nhập.
## 13. Quản lý thông tin kho

### 13.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Quản lý kho`, frontend mở `QuanLyKhoFrame`.

Luồng khởi tạo:

```text
QuanLyKhoFrame.<init>()
-> loadLookups()
-> loadWarehouses()
```

Màn hình gồm:

- Tìm kiếm.
- Lọc loại kho và trạng thái.
- Bảng danh sách kho.
- Form chi tiết kho.
- Nút lưu, làm mới, ngưng hoạt động hoặc kích hoạt tùy trạng thái.

### 13.2 Load lookup

Frontend gọi:

```text
WarehouseApiClient.getWarehouseLookups()
```

API:

```http
GET /api/warehouses/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<WarehouseLookupResponse>
```

Response thường gồm:

- danh sách chi nhánh để gắn kho chi nhánh.
- danh sách loại kho.
- danh sách trạng thái.

### 13.3 Load danh sách kho

Frontend gọi:

```text
QuanLyKhoFrame.loadWarehouses()
-> WarehouseApiClient.getWarehouses(keyword, loaiKho, trangThai, page, size)
```

API:

```http
GET /api/warehouses?keyword=&loaiKho=&trangThai=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<WarehouseResponse>>
```

Backend xử lý:

```text
WarehouseController.getWarehouses(...)
-> WarehouseService.getWarehouses(...)
-> WarehouseRepository query KHO
   LEFT JOIN CHINHANH
-> trả PageResponse<WarehouseResponse>
```

### 13.4 Bấm Lưu

Frontend gọi:

```text
QuanLyKhoFrame.saveWarehouse()
-> nếu chưa có id: WarehouseApiClient.createWarehouse(request)
-> nếu có id: WarehouseApiClient.updateWarehouse(id, request)
```

API tạo mới:

```http
POST /api/warehouses
Authorization: Bearer <token>
Content-Type: application/json
```

API cập nhật:

```http
PUT /api/warehouses/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
WarehouseRequest
```

Body mẫu:

```json
{
  "tenKho": "Kho Bến Thành",
  "loaiKho": "BRANCH",
  "maChiNhanh": 2,
  "diaChi": "12 Nguyễn Huệ, Quận 1, TP.HCM",
  "trangThai": "ACTIVE"
}
```

Response class:

```text
BaseResponse<WarehouseResponse>
```

Backend xử lý:

```text
WarehouseController.create/update
-> WarehouseService.create/update
-> validate loại kho CENTRAL/BRANCH và chi nhánh đi kèm
-> INSERT/UPDATE KHO
-> trả WarehouseResponse
```

### 13.5 Bấm Ngưng hoạt động

Frontend gọi:

```text
QuanLyKhoFrame.deactivateWarehouse()
-> WarehouseApiClient.deleteWarehouse(id)
```

API:

```http
DELETE /api/warehouses/{id}
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<Void> hoặc BaseResponse<WarehouseResponse>
```

Backend nên xóa mềm bằng trạng thái vì kho đã có liên kết tồn kho, lô hàng, chứng từ nhập/xuất/điều chuyển.

## 14. Quản lý nguyên liệu

### 14.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Quản lý nguyên liệu`, frontend mở `QuanLyNguyenLieuFrame`.

Luồng khởi tạo:

```text
QuanLyNguyenLieuFrame.<init>()
-> loadLookups()
-> loadIngredients()
```

Màn hình gồm:

- Tìm kiếm nguyên liệu.
- Lọc đơn vị tính, trạng thái.
- Bảng danh sách nguyên liệu.
- Form chi tiết nguyên liệu.

### 14.2 Load lookup

Frontend gọi:

```text
IngredientApiClient.getIngredientLookups()
```

API:

```http
GET /api/ingredients/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<IngredientLookupResponse>
```

Response gồm:

- danh sách đơn vị tính.
- danh sách trạng thái nguyên liệu.

### 14.3 Load danh sách nguyên liệu

Frontend gọi:

```text
QuanLyNguyenLieuFrame.loadIngredients()
-> IngredientApiClient.getIngredients(keyword, maDonViTinh, trangThai, page, size)
```

API:

```http
GET /api/ingredients?keyword=&maDonViTinh=&trangThai=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<IngredientResponse>>
```

Backend xử lý:

```text
IngredientController.getIngredients(...)
-> IngredientService.getIngredients(...)
-> IngredientRepository query NGUYENLIEU
   JOIN DONVITINH
-> trả PageResponse<IngredientResponse>
```

### 14.4 Bấm Lưu

Frontend gọi:

```text
QuanLyNguyenLieuFrame.saveIngredient()
-> nếu chưa có id: IngredientApiClient.createIngredient(request)
-> nếu có id: IngredientApiClient.updateIngredient(id, request)
```

API tạo mới:

```http
POST /api/ingredients
Authorization: Bearer <token>
Content-Type: application/json
```

API cập nhật:

```http
PUT /api/ingredients/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
IngredientRequest
```

Body mẫu:

```json
{
  "tenNguyenLieu": "Sữa tươi",
  "maDonViTinh": 2,
  "mucTonToiThieu": 10000,
  "trangThai": "ACTIVE"
}
```

Response class:

```text
BaseResponse<IngredientResponse>
```

Backend xử lý:

```text
IngredientController.create/update
-> IngredientService.create/update
-> validate tên, đơn vị tính, mức tồn tối thiểu
-> INSERT/UPDATE NGUYENLIEU
-> trả IngredientResponse
```

### 14.5 Bấm Ngưng hoạt động

Frontend gọi:

```text
QuanLyNguyenLieuFrame.deactivateIngredient()
-> IngredientApiClient.deleteIngredient(id)
```

API:

```http
DELETE /api/ingredients/{id}
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<Void> hoặc BaseResponse<IngredientResponse>
```

Backend nên xóa mềm vì nguyên liệu có liên kết với tồn kho, lô hàng, công thức sản phẩm và chứng từ.

## 15. Quản lý đơn vị tính

### 15.1 Frontend mở màn hình

Từ `KhoMenuFrame`, bấm card `Quản lý đơn vị tính`, frontend mở `QuanLyDonViTinhFrame`.

Luồng khởi tạo:

```text
QuanLyDonViTinhFrame.<init>()
-> loadUnits()
```

### 15.2 Load danh sách đơn vị tính

Frontend gọi:

```text
UnitApiClient.getUnits(keyword, page, size)
```

API:

```http
GET /api/units?keyword=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<UnitResponse>>
```

Backend xử lý:

```text
UnitController.getUnits(...)
-> UnitService.getUnits(...)
-> UnitRepository query DONVITINH
```

### 15.3 Bấm Lưu

Frontend gọi:

```text
QuanLyDonViTinhFrame.saveUnit()
-> nếu chưa có id: UnitApiClient.createUnit(request)
-> nếu có id: UnitApiClient.updateUnit(id, request)
```

API tạo mới:

```http
POST /api/units
Authorization: Bearer <token>
Content-Type: application/json
```

API cập nhật:

```http
PUT /api/units/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
UnitRequest
```

Body mẫu:

```json
{
  "tenDonViTinh": "Gram",
  "kyHieu": "g"
}
```

Response class:

```text
BaseResponse<UnitResponse>
```

Backend xử lý:

```text
UnitController.create/update
-> UnitService.create/update
-> validate tên và ký hiệu không trùng
-> INSERT/UPDATE DONVITINH
-> trả UnitResponse
```

### 15.4 Bấm Xóa

Frontend gọi:

```text
QuanLyDonViTinhFrame.deleteUnit()
-> UnitApiClient.deleteUnit(id)
```

API:

```http
DELETE /api/units/{id}
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<Void>
```

Nếu đơn vị tính đã được nguyên liệu sử dụng, backend nên chặn xóa hoặc chỉ cho xóa khi không còn liên kết.

## 16. Quản lý chi nhánh

### 16.1 Frontend mở màn hình

Từ `MenuTongFrame` hoặc menu phù hợp, bấm card `Quản lý chi nhánh`, frontend mở `QuanLyChiNhanhFrame`.

Luồng khởi tạo:

```text
QuanLyChiNhanhFrame.<init>()
-> loadStatistics()
-> loadBranches()
```

Màn hình gồm:

- Tìm kiếm.
- Lọc trạng thái.
- Bảng chi nhánh.
- Form chi tiết chi nhánh.
- Thẻ thống kê tổng quan.

### 16.2 Load thống kê

Frontend gọi:

```text
BranchApiClient.getBranchStatistics()
```

API:

```http
GET /api/branches/statistics
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<BranchStatisticsResponse>
```

Response dùng cho các ô tổng quan như tổng chi nhánh, đang hoạt động, tạm ngưng.

### 16.3 Load danh sách chi nhánh

Frontend gọi:

```text
QuanLyChiNhanhFrame.loadBranches()
-> BranchApiClient.getBranches(keyword, trangThai, page, size)
```

API:

```http
GET /api/branches?keyword=&trangThai=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<BranchResponse>>
```

Backend xử lý:

```text
BranchController.getBranches(...)
-> BranchService.getBranches(...)
-> BranchRepository query CHINHANH
```

### 16.4 Bấm Lưu

Frontend gọi:

```text
QuanLyChiNhanhFrame.saveBranch()
-> nếu chưa có id: BranchApiClient.createBranch(request)
-> nếu có id: BranchApiClient.updateBranch(id, request)
```

API tạo mới:

```http
POST /api/branches
Authorization: Bearer <token>
Content-Type: application/json
```

API cập nhật:

```http
PUT /api/branches/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
BranchRequest
```

Body mẫu:

```json
{
  "tenChiNhanh": "Bến Thành",
  "diaChi": "12 Nguyễn Huệ, Quận 1, TP.HCM",
  "soDienThoai": "02838220001",
  "trangThai": "ACTIVE"
}
```

Response class:

```text
BaseResponse<BranchResponse>
```

Backend xử lý:

```text
BranchController.create/update
-> BranchService.create/update
-> validate dữ liệu chi nhánh
-> INSERT/UPDATE CHINHANH
-> trả BranchResponse
```

### 16.5 Bấm Đổi trạng thái hoặc Xóa mềm

Frontend gọi:

```text
QuanLyChiNhanhFrame.changeStatus()
-> BranchApiClient.changeStatus(id, request)
```

API có thể là một trong các endpoint backend hiện có:

```http
PATCH /api/branches/{id}/status
```

hoặc:

```http
DELETE /api/branches/{id}
```

Request class nếu dùng PATCH:

```text
BranchStatusRequest
```

Response class:

```text
BaseResponse<BranchResponse>
```

Backend không nên xóa vật lý chi nhánh vì chi nhánh liên kết kho, người dùng, hóa đơn và chứng từ.

## 17. Tạo tài khoản người dùng

### 17.1 Frontend mở màn hình

Từ `QuanTriHeThongFrame`, bấm card `Tạo tài khoản`, frontend mở `TaoTaiKhoanFrame`.

Luồng khởi tạo:

```text
TaoTaiKhoanFrame.<init>()
-> loadLookups()
```

Màn hình gồm:

- Thông tin đăng nhập.
- Thông tin cá nhân.
- Vai trò.
- Chi nhánh nếu vai trò cần gắn chi nhánh.
- Nút tạo tài khoản.

### 17.2 Load lookup

Frontend gọi:

```text
UserApiClient.getUserLookups()
```

API:

```http
GET /api/users/lookups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<UserLookupResponse>
```

`UserLookupResponse` gồm:

- danh sách vai trò.
- danh sách chi nhánh.
- danh sách trạng thái nếu có.

### 17.3 Chọn vai trò

Frontend gọi local:

```text
TaoTaiKhoanFrame.updateBranchStateByRole()
```

Nếu vai trò là quản lý chi nhánh hoặc thu ngân, UI bật combobox chi nhánh. Nếu vai trò admin hoặc quản lý kho, chi nhánh có thể không bắt buộc.

Không gọi API ở bước chọn vai trò.

### 17.4 Bấm Tạo tài khoản

Frontend gọi:

```text
TaoTaiKhoanFrame.handleCreateUser()
-> UserApiClient.createUser(request)
```

API:

```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
CreateUserRequest
```

Body mẫu:

```json
{
  "tenDangNhap": "qlcn_benthanh",
  "matKhau": "123456",
  "hoTen": "Quản lý Bến Thành",
  "email": "qlcn.benthanh@example.com",
  "soDienThoai": "0900000001",
  "maVaiTro": 3,
  "maChiNhanh": 2,
  "trangThai": "ACTIVE"
}
```

Response class:

```text
BaseResponse<UserResponse>
```

Backend xử lý:

```text
UserController.createUser(request)
-> UserService.createUser(request)
-> validate username/email không trùng
-> mã hóa mật khẩu nếu backend có password encoder
-> INSERT NGUOIDUNG
-> gắn vai trò/chi nhánh
-> trả UserResponse
```

## 18. Phân quyền và bảo mật

### 18.1 Frontend mở màn hình

Từ `QuanTriHeThongFrame`, bấm card `Phân quyền`, frontend mở `PhanQuyenBaoMatFrame`.

Luồng khởi tạo:

```text
PhanQuyenBaoMatFrame.<init>()
-> loadRoles()
-> loadPermissionGroups()
-> loadRolePermissions(selectedRole)
```

Màn hình gồm:

- Card danh sách role: Admin, Quản lý kho, Quản lý chi nhánh, Thu ngân.
- Danh sách quyền nhóm theo chức năng.
- Checkbox quyền.
- Nút tải lại và lưu phân quyền.

### 18.2 Load danh sách role

Frontend gọi:

```text
RbacApiClient.getRoles()
```

API:

```http
GET /api/rbac/roles
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<List<RoleResponse>>
```

### 18.3 Load nhóm quyền

Frontend gọi:

```text
RbacApiClient.getPermissionGroups()
```

API:

```http
GET /api/rbac/permissions/groups
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<List<PermissionGroupResponse>>
```

Backend xử lý:

```text
RbacController.getPermissionGroups()
-> RbacService.getPermissionGroups()
-> query CHUCNANG, QUYEN
-> gom quyền theo module
```

### 18.4 Chọn role

Khi click một role card, frontend gọi:

```text
PhanQuyenBaoMatFrame.selectRole(role)
-> RbacApiClient.getRolePermissions(roleId)
```

API:

```http
GET /api/rbac/roles/{roleId}/permissions
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<RolePermissionResponse>
```

Response gồm role và danh sách permission đang được gán.

### 18.5 Bấm Lưu phân quyền

Frontend gọi:

```text
PhanQuyenBaoMatFrame.savePermissions()
-> gom danh sách permissionId đang tick
-> RbacApiClient.updateRolePermissions(roleId, request)
```

API:

```http
PUT /api/rbac/roles/{roleId}/permissions
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
UpdateRolePermissionsRequest
```

Body mẫu:

```json
{
  "permissionIds": [1, 2, 3, 10, 11]
}
```

Response class:

```text
BaseResponse<RolePermissionResponse>
```

Backend xử lý:

```text
RbacController.updateRolePermissions(roleId, request)
-> RbacService.updateRolePermissions(roleId, request)
-> xóa quyền cũ trong VAITRO_QUYEN của role
-> insert danh sách quyền mới
-> trả role kèm danh sách quyền sau cập nhật
```

Sau khi lưu, user đang đăng nhập nên logout/login lại hoặc gọi refresh session nếu muốn quyền mới có hiệu lực ngay ở frontend.
## 19. Quản lý sản phẩm và công thức POS

### 19.1 Trạng thái frontend hiện tại

Backend đã có `RecipeController` cho nhóm API sản phẩm/công thức. Frontend menu POS hiện tại có thể vẫn đang là placeholder hoặc chưa hoàn thiện đầy đủ như các màn kho.

Nếu trong `QuanLyPOSFrame` hoặc frame sản phẩm đã nối API, luồng chuẩn nên đi theo mô hình sau:

```text
POS/Product Frame
-> RecipeApiClient
-> RecipeController
-> RecipeService
-> RecipeRepository
```

### 19.2 Load danh sách sản phẩm

API dự kiến:

```http
GET /api/recipes/products?keyword=&status=&page=&size=
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<PageResponse<ProductResponse>>
```

Backend xử lý:

```text
RecipeController.getProducts(...)
-> RecipeService.getProducts(...)
-> query SANPHAM
```

### 19.3 Tạo hoặc cập nhật sản phẩm

API tạo:

```http
POST /api/recipes/products
Authorization: Bearer <token>
Content-Type: application/json
```

API cập nhật:

```http
PUT /api/recipes/products/{maSanPham}
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
ProductRequest
```

Body mẫu:

```json
{
  "tenSanPham": "Cà phê sữa đá",
  "giaBanHienTai": 35000,
  "trangThai": "AVAILABLE"
}
```

Response class:

```text
BaseResponse<ProductResponse>
```

### 19.4 Load công thức sản phẩm

API:

```http
GET /api/recipes/products/{maSanPham}/ingredients
Authorization: Bearer <token>
```

Request class: không có body.

Response class:

```text
BaseResponse<List<ProductRecipeItemResponse>>
```

Backend xử lý:

```text
RecipeController.getProductRecipe(maSanPham)
-> RecipeService.getProductRecipe(maSanPham)
-> query CONGTHUC_SANPHAM
   JOIN NGUYENLIEU
   JOIN DONVITINH
```

### 19.5 Lưu công thức sản phẩm

API:

```http
PUT /api/recipes/products/{maSanPham}/ingredients
Authorization: Bearer <token>
Content-Type: application/json
```

Request class:

```text
UpdateProductRecipeRequest
```

Body mẫu:

```json
{
  "items": [
    {
      "maNguyenLieu": 2,
      "soLuongCan": 20
    },
    {
      "maNguyenLieu": 3,
      "soLuongCan": 40
    }
  ]
}
```

Response class:

```text
BaseResponse<List<ProductRecipeItemResponse>>
```

Backend xử lý:

```text
RecipeController.updateProductRecipe(maSanPham, request)
-> RecipeService.updateProductRecipe(maSanPham, request)
-> validate sản phẩm và nguyên liệu tồn tại
-> xóa hoặc cập nhật CONGTHUC_SANPHAM cũ
-> insert công thức mới
-> trả danh sách công thức sau cập nhật
```

## 20. Mapping nhanh frontend -> API client -> backend

Bảng này dùng để tra nhanh khi cần debug một nút trên UI đang đi tới đâu.

| Chức năng | Frame frontend | Hàm frontend chính | API client | Backend controller | Backend service |
|---|---|---|---|---|---|
| Đăng nhập | `LoginFrame` | `doLogin()` | `AuthApiClient` | `AuthController` | `AuthService` |
| Quên mật khẩu | `ForgotPasswordFrame` | `requestCode()`, `verifyCode()`, `resetPassword()` | `AuthApiClient` | `AuthController` | `PasswordResetService` hoặc service auth tương ứng |
| Nhập kho | `NhapKhoFrame` | `loadLookups()`, `addLine()`, `saveReceipt()` | `InventoryApiClient` | `InventoryController` | `InventoryService` |
| Xuất kho | `XuatKhoFrame` | `loadStock()`, `loadLots()`, `saveReceipt()` | `InventoryApiClient` | `InventoryController` | `InventoryService` |
| Điều chuyển kho | `DieuChuyenKhoFrame` | `loadStock()`, `loadLots()`, `saveTransfer()` | `InventoryApiClient` | `InventoryController` | `InventoryService` |
| Xem tồn kho | `XemTonKhoFrame` | `loadData(false)` | `InventoryApiClient` | `InventoryController` | `InventoryService` |
| Theo dõi HSD | `TheoDoiHanSuDungFrame` | `loadData(false)`, `refreshExpiredLots()` | `InventoryApiClient` | `ExpiryController` | `ExpiryService` |
| Lịch sử kho | `TraCuuLichSuKhoFrame` | `loadData(false)` | `InventoryApiClient` | `InventoryHistoryController` | `InventoryHistoryService` |
| Báo cáo hao hụt | `BaoCaoHaoHutFrame` | `loadLotsAndHistory()`, `saveWastage()` | `InventoryApiClient` | `WastageController` | `WastageService` |
| Kiểm kho | `KiemKhoFrame` | `loadSystemStockAndHistory()`, `saveStocktake()`, `openSelectedStocktake()`, `cancelSelectedStocktake()` | `InventoryApiClient` | `StocktakeController` | `StocktakeService` |
| Nhà cung cấp | `QuanLyNhaCungCapFrame` | `loadSuppliers()`, `saveSupplier()`, `deleteSupplier()` | `SupplierApiClient` | `SupplierController` | `SupplierService` |
| Kho | `QuanLyKhoFrame` | `loadWarehouses()`, `saveWarehouse()`, `deactivateWarehouse()` | `WarehouseApiClient` | `WarehouseController` | `WarehouseService` |
| Nguyên liệu | `QuanLyNguyenLieuFrame` | `loadIngredients()`, `saveIngredient()`, `deactivateIngredient()` | `IngredientApiClient` | `IngredientController` | `IngredientService` |
| Đơn vị tính | `QuanLyDonViTinhFrame` | `loadUnits()`, `saveUnit()`, `deleteUnit()` | `UnitApiClient` | `UnitController` | `UnitService` |
| Chi nhánh | `QuanLyChiNhanhFrame` | `loadBranches()`, `saveBranch()`, `changeStatus()` | `BranchApiClient` | `BranchController` | `BranchService` |
| Tạo tài khoản | `TaoTaiKhoanFrame` | `handleCreateUser()` | `UserApiClient` | `UserController` | `UserService` |
| Phân quyền | `PhanQuyenBaoMatFrame` | `loadRoles()`, `loadPermissionGroups()`, `savePermissions()` | `RbacApiClient` | `RbacController` | `RbacService` |
| Sản phẩm/công thức | POS/Product frame | tùy frame hiện tại | `RecipeApiClient` nếu đã có | `RecipeController` | `RecipeService` |

## 21. Mapping nhanh endpoint chính

| Nhóm | Method | Endpoint | Request class | Response class |
|---|---|---|---|---|
| Auth | POST | `/api/auth/login` | `LoginRequest` | `BaseResponse<LoginResponse>` |
| Auth | POST | `/api/auth/logout` | không body | `BaseResponse<Void>` |
| Auth | GET | `/api/auth/me` | không body | `BaseResponse<UserSessionResponse>` hoặc response user hiện có |
| Auth | POST | `/api/auth/forgot-password` | `ForgotPasswordRequest` | `BaseResponse<ForgotPasswordResponse>` |
| Auth | POST | `/api/auth/forgot-password/verify` | `VerifyResetCodeRequest` | `BaseResponse<Void>` |
| Auth | POST | `/api/auth/forgot-password/reset` | `ResetPasswordRequest` | `BaseResponse<Void>` |
| Nhập kho | GET | `/api/inventory/imports/lookups` | không body | `BaseResponse<InventoryLookupResponse>` |
| Nhập kho | POST | `/api/inventory/imports` | `CreateImportReceiptRequest` | `BaseResponse<ImportReceiptResponse>` |
| Xuất kho | GET | `/api/inventory/exports/lookups` | không body | `BaseResponse<InventoryExportLookupResponse>` |
| Xuất kho | GET | `/api/inventory/exports/stock` | query params | `BaseResponse<List<InventoryStockOptionResponse>>` |
| Xuất kho | GET | `/api/inventory/exports/lots` | query params | `BaseResponse<List<InventoryLotResponse>>` |
| Xuất kho | POST | `/api/inventory/exports` | `CreateExportReceiptRequest` | `BaseResponse<ExportReceiptResponse>` |
| Điều chuyển | GET | `/api/inventory/transfers/lookups` | không body | `BaseResponse<InventoryTransferLookupResponse>` |
| Điều chuyển | GET | `/api/inventory/transfers/stock` | query params | `BaseResponse<List<InventoryStockOptionResponse>>` |
| Điều chuyển | GET | `/api/inventory/transfers/lots` | query params | `BaseResponse<List<InventoryLotResponse>>` |
| Điều chuyển | POST | `/api/inventory/transfers` | `CreateTransferReceiptRequest` | `BaseResponse<TransferReceiptResponse>` |
| Tồn kho | GET | `/api/inventory/stock` | query params | `BaseResponse<PageResponse<InventoryStockResponse>>` |
| Tồn kho | GET | `/api/inventory/stock/lots` | query params | `BaseResponse<List<InventoryLotResponse>>` |
| Tồn kho | GET | `/api/inventory/stock/summary` | query params | `BaseResponse<InventoryStockSummaryResponse>` |
| HSD | GET | `/api/inventory/expiry/lookups` | không body | `BaseResponse<ExpiryLookupResponse>` |
| HSD | GET | `/api/inventory/expiry/lots` | query params | `BaseResponse<List<ExpiryLotResponse>>` |
| HSD | GET | `/api/inventory/expiry/statistics` | query params | `BaseResponse<ExpiryStatisticsResponse>` |
| HSD | POST | `/api/inventory/expiry/refresh` | không body | `BaseResponse<ExpiryRefreshResponse>` |
| Lịch sử | GET | `/api/inventory/history/lookups` | không body | `BaseResponse<InventoryHistoryLookupResponse>` |
| Lịch sử | GET | `/api/inventory/history` | query params | `BaseResponse<PageResponse<InventoryHistoryResponse>>` |
| Lịch sử | GET | `/api/inventory/history/summary` | query params | `BaseResponse<InventoryHistorySummaryResponse>` |
| Hao hụt | GET | `/api/inventory/wastages/lookups` | không body | `BaseResponse<WastageLookupResponse>` |
| Hao hụt | GET | `/api/inventory/wastages/lots` | query params | `BaseResponse<List<WastageLotResponse>>` |
| Hao hụt | GET | `/api/inventory/wastages` | query params | `BaseResponse<PageResponse<WastageResponse>>` |
| Hao hụt | POST | `/api/inventory/wastages` | `CreateWastageRequest` | `BaseResponse<WastageResponse>` |
| Kiểm kho | GET | `/api/inventory/stocktakes/lookups` | không body | `BaseResponse<StocktakeLookupResponse>` |
| Kiểm kho | GET | `/api/inventory/stocktakes/system-stock` | query params | `BaseResponse<List<StocktakeSystemStockResponse>>` |
| Kiểm kho | GET | `/api/inventory/stocktakes` | query params | `BaseResponse<PageResponse<StocktakeResponse>>` |
| Kiểm kho | GET | `/api/inventory/stocktakes/{id}` | không body | `BaseResponse<StocktakeResponse>` |
| Kiểm kho | POST | `/api/inventory/stocktakes` | `SaveStocktakeRequest` | `BaseResponse<StocktakeResponse>` |
| Kiểm kho | DELETE | `/api/inventory/stocktakes/{id}` | không body | `BaseResponse<StocktakeResponse>` |
| Nhà cung cấp | GET | `/api/suppliers` | query params | `BaseResponse<PageResponse<SupplierResponse>>` |
| Nhà cung cấp | POST | `/api/suppliers` | `SupplierRequest` | `BaseResponse<SupplierResponse>` |
| Nhà cung cấp | PUT | `/api/suppliers/{id}` | `SupplierRequest` | `BaseResponse<SupplierResponse>` |
| Nhà cung cấp | DELETE | `/api/suppliers/{id}` | không body | `BaseResponse<Void>` hoặc `BaseResponse<SupplierResponse>` |
| Kho | GET | `/api/warehouses/lookups` | không body | `BaseResponse<WarehouseLookupResponse>` |
| Kho | GET | `/api/warehouses` | query params | `BaseResponse<PageResponse<WarehouseResponse>>` |
| Kho | POST | `/api/warehouses` | `WarehouseRequest` | `BaseResponse<WarehouseResponse>` |
| Kho | PUT | `/api/warehouses/{id}` | `WarehouseRequest` | `BaseResponse<WarehouseResponse>` |
| Kho | DELETE | `/api/warehouses/{id}` | không body | `BaseResponse<Void>` hoặc `BaseResponse<WarehouseResponse>` |
| Nguyên liệu | GET | `/api/ingredients/lookups` | không body | `BaseResponse<IngredientLookupResponse>` |
| Nguyên liệu | GET | `/api/ingredients` | query params | `BaseResponse<PageResponse<IngredientResponse>>` |
| Nguyên liệu | POST | `/api/ingredients` | `IngredientRequest` | `BaseResponse<IngredientResponse>` |
| Nguyên liệu | PUT | `/api/ingredients/{id}` | `IngredientRequest` | `BaseResponse<IngredientResponse>` |
| Nguyên liệu | DELETE | `/api/ingredients/{id}` | không body | `BaseResponse<Void>` hoặc `BaseResponse<IngredientResponse>` |
| Đơn vị tính | GET | `/api/units` | query params | `BaseResponse<PageResponse<UnitResponse>>` |
| Đơn vị tính | POST | `/api/units` | `UnitRequest` | `BaseResponse<UnitResponse>` |
| Đơn vị tính | PUT | `/api/units/{id}` | `UnitRequest` | `BaseResponse<UnitResponse>` |
| Đơn vị tính | DELETE | `/api/units/{id}` | không body | `BaseResponse<Void>` |
| Chi nhánh | GET | `/api/branches/statistics` | không body | `BaseResponse<BranchStatisticsResponse>` |
| Chi nhánh | GET | `/api/branches` | query params | `BaseResponse<PageResponse<BranchResponse>>` |
| Chi nhánh | POST | `/api/branches` | `BranchRequest` | `BaseResponse<BranchResponse>` |
| Chi nhánh | PUT | `/api/branches/{id}` | `BranchRequest` | `BaseResponse<BranchResponse>` |
| Chi nhánh | PATCH/DELETE | `/api/branches/{id}/status` hoặc `/api/branches/{id}` | `BranchStatusRequest` hoặc không body | `BaseResponse<BranchResponse>` |
| User | GET | `/api/users/lookups` | không body | `BaseResponse<UserLookupResponse>` |
| User | GET | `/api/users` | query params | `BaseResponse<PageResponse<UserResponse>>` |
| User | POST | `/api/users` | `CreateUserRequest` | `BaseResponse<UserResponse>` |
| RBAC | GET | `/api/rbac/roles` | không body | `BaseResponse<List<RoleResponse>>` |
| RBAC | GET | `/api/rbac/permissions/groups` | không body | `BaseResponse<List<PermissionGroupResponse>>` |
| RBAC | GET | `/api/rbac/roles/{id}/permissions` | không body | `BaseResponse<RolePermissionResponse>` |
| RBAC | PUT | `/api/rbac/roles/{id}/permissions` | `UpdateRolePermissionsRequest` | `BaseResponse<RolePermissionResponse>` |

## 22. Quyền chính theo chức năng

| Chức năng | Quyền xem | Quyền thao tác |
|---|---|---|
| Nhập kho | `INVENTORY:VIEW` | `INVENTORY:IMPORT` |
| Xuất kho | `INVENTORY:VIEW` | `INVENTORY:EXPORT` |
| Điều chuyển kho | `INVENTORY:VIEW` | `INVENTORY:TRANSFER` |
| Xem tồn kho | `INVENTORY:VIEW` | không có thao tác ghi |
| Theo dõi HSD | `INVENTORY:VIEW` | có thể dùng quyền quản lý kho nếu rà soát cập nhật trạng thái |
| Lịch sử kho | `INVENTORY:VIEW` | không có thao tác ghi |
| Báo cáo hao hụt | `WASTAGE:VIEW` hoặc `INVENTORY:VIEW` | `WASTAGE:CREATE` |
| Kiểm kho | `STOCKTAKE:VIEW` | `STOCKTAKE:MANAGE` |
| Nhà cung cấp | `SUPPLIER:VIEW` | `SUPPLIER:CREATE`, `SUPPLIER:UPDATE`, `SUPPLIER:DELETE` |
| Kho | `WAREHOUSE:VIEW` hoặc `INVENTORY:VIEW` tùy backend | quyền create/update/delete kho nếu đã khai báo |
| Nguyên liệu | `INGREDIENT:VIEW` | `INGREDIENT:CREATE`, `INGREDIENT:UPDATE`, `INGREDIENT:DELETE` |
| Đơn vị tính | `UNIT:VIEW` | `UNIT:CREATE`, `UNIT:UPDATE`, `UNIT:DELETE` |
| Chi nhánh | `BRANCH:VIEW` | `BRANCH:CREATE`, `BRANCH:UPDATE`, `BRANCH:DELETE` |
| Tài khoản | `USER:VIEW` | `USER:CREATE`, `USER:UPDATE`, `USER:DELETE` |
| Phân quyền | `ROLE:VIEW` | `ROLE:CREATE`, `ROLE:UPDATE`, `ROLE:DELETE` hoặc quyền tương ứng trong backend |

## 23. Ghi chú debug nhanh

### 23.1 Frontend gọi API nhưng không thấy dữ liệu

Kiểm tra theo thứ tự:

1. Token có tồn tại trong `SessionManager` không.
2. URL trong `ApiConfig` có đúng endpoint backend không.
3. API client có gắn header `Authorization: Bearer <token>` không.
4. Backend controller có endpoint đúng method/path không.
5. User có quyền tương ứng không.
6. Dữ liệu seed có tồn tại và trạng thái phù hợp không.

### 23.2 Combobox hiển thị xấu hoặc bị đè viền

Các frame Swing đang dùng style custom cho combobox. Khi thêm frame mới nên tái sử dụng helper style combobox đã dùng ở `XuatKhoFrame`, `DieuChuyenKhoFrame`, `QuanLyKhoFrame` hoặc các common component hiện có.

Không nên dùng `JComboBox` mặc định rồi set border thủ công nhiều lần vì dễ bị tình trạng nội dung che viền.

### 23.3 Search field bị chữ đè icon

Các ô tìm kiếm có icon kính lúp cần set margin hoặc border padding trái đủ rộng. Luồng đúng là:

```text
custom search field paint icon ở bên trái
-> set margin/border left khoảng 38-44px
-> text bắt đầu sau icon
```

Nếu chữ bắt đầu từ x = 0, kiểm tra lại helper tạo field trong frame tương ứng.

### 23.4 ORA-00942 khi seed

Lỗi `ORA-00942 table or view does not exist` nghĩa là seed đang chạy trước khi tạo bảng hoặc chạy sai schema/user.

Cách kiểm tra:

```sql
SELECT table_name FROM user_tables ORDER BY table_name;
```

Nếu không thấy bảng như `VAITRO`, `CHUCNANG`, `QUYEN`, cần chạy file tạo schema trước file seed.

### 23.5 ORA-02290 khi thêm quyền

Lỗi check constraint `CK_QUYEN_HANH_DONG` xảy ra khi thêm action chưa nằm trong constraint, ví dụ `MANAGE` nhưng constraint chỉ cho `VIEW`, `CREATE`, `UPDATE`, `DELETE`, `IMPORT`, `EXPORT`, ...

Nếu muốn dùng action mới, phải sửa cấu trúc constraint. Nếu không muốn sửa cấu trúc bảng, dùng action đã có trong constraint hoặc cập nhật logic seed/backend về action hợp lệ.

### 23.6 Quyền mới không có hiệu lực ngay

Sau khi cập nhật phân quyền role, token/session frontend có thể vẫn giữ danh sách quyền cũ. Cách chắc nhất là logout rồi login lại để `SessionManager` nhận quyền mới.

## 24. Ghi chú về độ chính xác tài liệu

Tài liệu này được viết theo luồng code hiện tại của frontend/backend và quy ước class trong project. Nếu sau này đổi tên endpoint, đổi request/response DTO hoặc tách service, cần cập nhật lại đúng các mục:

- Mapping frontend -> API client -> controller.
- Mapping endpoint -> request class -> response class.
- Quyền cần có theo chức năng.
- Ghi chú nghiệp vụ ở các màn có cập nhật tồn kho như nhập, xuất, điều chuyển, hao hụt và kiểm kho.

## 25. Luồng chi tiết theo hàm frontend

Phần này ghi kỹ hơn theo đúng thứ tự chạy trong frontend: mở màn hình -> constructor -> build giao diện -> bind events -> load lookup -> user thao tác -> gọi API backend. Nếu cần debug, nên đọc phần này trước rồi quay lại các mục API ở trên.

## 25.1 Nhập kho - `NhapKhoFrame`

### 25.1.1 Mở màn hình

Luồng điều hướng:

```text
KhoMenuFrame.handleMenuClick(...)
-> FrameNavigator.open(this, new NhapKhoFrame()) hoặc new NhapKhoFrame().setVisible(true)
-> NhapKhoFrame.NhapKhoFrame()
```

Constructor:

```text
NhapKhoFrame()
-> setTitle("Phụng Lộc - Nhập kho")
-> setDefaultCloseOperation(DISPOSE_ON_CLOSE)
-> setSize(...)
-> tạo root panel
-> buildHeader()
-> buildReceiptInfo()
-> buildIngredientPicker()
-> buildTables()
-> buildQuickInputRow()
-> buildNoteAndFooter()
-> bindAmountPreview()
-> loadLookups()
```

Ý nghĩa từng hàm build:

```text
buildHeader()
-> dựng tiêu đề màn hình
-> dựng nút Quay lại
-> backButton.addActionListener(...)
   -> new KhoMenuFrame().setVisible(true)
   -> dispose()

buildReceiptInfo()
-> dựng card thông tin phiếu nhập
-> addCombo(warehouseCombo)
-> addCombo(supplierCombo)
-> addField(createdDateField)
-> addField(creatorField)

buildIngredientPicker()
-> dựng ô tìm kiếm nguyên liệu
-> searchButton.addActionListener(...)
   -> filterIngredientTable(searchField.getText())
-> searchField.getDocument().addDocumentListener(...)
   -> insertUpdate/removeUpdate/changedUpdate
   -> filterIngredientTable(searchField.getText())

buildTables()
-> configureTable(ingredientTable)
-> configureTable(importTable)
-> ingredientTable selection listener
   -> fillSelectedIngredientToDetail()
-> importTable selection listener nếu có xóa dòng

buildQuickInputRow()
-> dựng combo nguyên liệu
-> dựng field số lượng, đơn giá, số lô, hạn sử dụng
-> addToListButton.addActionListener(...)
   -> addLine()

buildNoteAndFooter()
-> dựng noteArea
-> dựng totalLabel
-> saveButton.addActionListener(...)
   -> saveReceipt()
-> cancelButton.addActionListener(...)
   -> resetReceipt()
```

### 25.1.2 Load dữ liệu ban đầu

Sau khi build xong UI, constructor gọi:

```text
loadLookups()
```

Chi tiết:

```text
loadLookups()
-> setFormEnabled(false)
-> statusLabel = "Đang tải dữ liệu nhập kho..."
-> new SwingWorker<InventoryLookupDto, Void>()
   -> doInBackground()
      -> inventoryApiClient.getImportLookups()
      -> GET /api/inventory/imports/lookups
   -> done()
      -> lookup = get()
      -> warehouseCombo.setModel(lookup.getWarehouses())
      -> supplierCombo.setModel(lookup.getSuppliers())
      -> allIngredients = lookup.getIngredients()
      -> ingredientCombo.setModel(allIngredients)
      -> populateIngredientTable(allIngredients)
      -> setFormEnabled(true)
```

Backend call chain:

```text
GET /api/inventory/imports/lookups
-> InventoryController.getImportLookups()
-> InventoryService.getImportLookups()
-> repository lấy warehouses, suppliers, ingredients
-> trả BaseResponse<InventoryLookupDto>
```

Response frontend nhận:

```text
InventoryLookupDto
-> warehouses: List<OptionDto>
-> suppliers: List<OptionDto>
-> ingredients: List<OptionDto>
```

### 25.1.3 Tìm nguyên liệu

Người dùng gõ vào ô tìm kiếm hoặc bấm nút `Tìm`:

```text
searchField DocumentListener hoặc searchButton ActionListener
-> filterIngredientTable(keyword)
-> nếu keyword rỗng:
   -> populateIngredientTable(allIngredients)
-> nếu có keyword:
   -> lọc allIngredients theo id hoặc name
   -> populateIngredientTable(filtered)
```

Không gọi backend ở bước này. Dữ liệu tìm kiếm là danh sách nguyên liệu đã load từ `loadLookups()`.

### 25.1.4 Chọn nguyên liệu ở bảng trái

```text
ingredientTable selection changed
-> fillSelectedIngredientToDetail()
-> đọc maNguyenLieu từ ingredientTable
-> tìm OptionDto tương ứng trong ingredientCombo
-> ingredientCombo.setSelectedItem(item)
```

Không gọi backend. Đây chỉ là sync bảng trái sang form chi tiết.

### 25.1.5 Nhập số lượng/đơn giá

Khi nhập `Số lượng` hoặc `Đơn giá`, event chạy:

```text
quantityField DocumentListener
priceField DocumentListener
-> updateAmountPreview()
-> parse quantity và price
-> amountPreviewLabel = quantity * price
```

Không gọi backend. Đây chỉ là preview thành tiền cho dòng chuẩn bị thêm.

### 25.1.6 Bấm Thêm vào phiếu

```text
addToListButton.addActionListener(...)
-> addLine()
```

Chi tiết `addLine()`:

```text
addLine()
-> đọc ingredientCombo.getSelectedItem()
-> đọc quantityField
-> đọc priceField
-> đọc lotField
-> parseExpiryDate(expiryField.getText())
-> validate:
   -> nguyên liệu phải được chọn
   -> số lượng > 0
   -> đơn giá >= 0 hoặc theo rule hiện tại
   -> hạn sử dụng đúng yyyy-MM-dd nếu có nhập
-> tạo ImportLine local
-> lines.add(line)
-> importModel.addRow(...)
-> updateTotal()
-> clearLineInputs()
```

Không gọi backend ở bước này. Dòng mới chỉ nằm trong biến local `lines` và bảng `importTable`.

### 25.1.7 Bấm Xóa dòng

Nếu có nút xóa dòng hoặc thao tác xóa:

```text
removeSelectedImportLine()
-> kiểm tra importTable selected row
-> lines.remove(row)
-> importModel.removeRow(row)
-> updateTotal()
```

Không gọi backend.

### 25.1.8 Bấm Lưu phiếu

```text
saveButton.addActionListener(...)
-> saveReceipt()
```

Chi tiết `saveReceipt()`:

```text
saveReceipt()
-> đọc warehouseCombo selected item
-> đọc supplierCombo selected item
-> kiểm tra lines không rỗng
-> tạo CreateImportReceiptRequest request
   -> request.setMaKho(warehouse.getId())
   -> request.setMaNhaCungCap(supplier.getId())
   -> request.setGhiChu(noteArea.getText())
   -> request.setItems(...)
-> mỗi ImportLine local chuyển thành CreateImportReceiptItemRequest
   -> maNguyenLieu
   -> soLuongNhap
   -> donGiaNhap
   -> soLo
   -> hanSuDung
-> saveButton.setEnabled(false)
-> setFormEnabled(false)
-> new SwingWorker<ImportReceiptDto, Void>()
   -> doInBackground()
      -> inventoryApiClient.createImportReceipt(request)
      -> POST /api/inventory/imports
   -> done()
      -> saveButton.setEnabled(true)
      -> setFormEnabled(true)
      -> nếu thành công:
         -> show message mã phiếu nhập
         -> resetReceipt()
      -> nếu lỗi:
         -> showError hoặc JOptionPane
```

Backend call chain:

```text
POST /api/inventory/imports
-> InventoryController.createImportReceipt(CreateImportReceiptRequest request)
-> InventoryService.createImportReceipt(request)
-> validate kho, nhà cung cấp, items
-> INSERT PHIEUNHAP
-> INSERT CHITIETPHIEUNHAP
-> INSERT LOHANG_NGUYENLIEU
-> UPSERT/CẬP NHẬT TONKHO tăng tồn
-> INSERT NHATKY_KHO loại IMPORT
-> trả BaseResponse<ImportReceiptDto>
```

Sau khi thành công:

```text
resetReceipt()
-> lines.clear()
-> importModel.setRowCount(0)
-> noteArea.setText("")
-> updateTotal()
-> clearLineInputs()
```

## 25.2 Xuất kho - `XuatKhoFrame`

### 25.2.1 Mở màn hình

Luồng điều hướng:

```text
KhoMenuFrame.handleMenuClick(...)
-> mở XuatKhoFrame
-> XuatKhoFrame.XuatKhoFrame()
```

Constructor:

```text
XuatKhoFrame()
-> setTitle("Phụng Lộc - Xuất kho")
-> tạo root panel
-> buildHeader()
-> buildReceiptInfo()
-> buildIngredientPicker()
-> buildTables()
-> buildQuickInputRow()
-> buildLotSection()
-> buildNoteAndFooter()
-> bindEvents()
-> loadLookups()
```

Build UI:

```text
buildHeader()
-> tiêu đề + nút Quay lại

buildReceiptInfo()
-> combo Kho xuất: warehouseCombo
-> combo Loại xuất: exportTypeCombo
-> field Ngày xuất
-> field Người tạo
-> checkbox Chọn lô thủ công: manualLotCheckBox

buildIngredientPicker()
-> searchField + searchButton
-> searchButton -> filterIngredientTable(...)
-> DocumentListener -> filterIngredientTable(...)

buildTables()
-> bảng nguyên liệu còn tồn: ingredientTable
-> bảng dòng xuất: exportTable
-> ingredientTable selection -> fillSelectedIngredientToDetail()

buildQuickInputRow()
-> combo nguyên liệu ingredientCombo
-> field số lượng quantityField
-> nút Thêm vào phiếu -> addLine()

buildLotSection()
-> bảng lô lotTable
-> người dùng nhập số lượng xuất theo từng lô nếu bật thủ công

buildNoteAndFooter()
-> noteArea
-> totalLabel
-> saveButton -> saveReceipt()
-> cancelButton -> resetReceipt()
```

### 25.2.2 Bind events

Sau khi build UI, constructor gọi:

```text
bindEvents()
```

Chi tiết:

```text
bindEvents()
-> warehouseCombo.addActionListener(...)
   -> loadStockForSelectedWarehouse()
   -> refreshLotsIfManual()

-> ingredientCombo.addActionListener(...)
   -> refreshLotsIfManual()

-> manualLotCheckBox.addActionListener(...)
   -> handleManualModeChanged()

-> lotModel.addTableModelListener(...)
   -> updateManualQuantityFromLots()
```

Ý nghĩa:

- Đổi kho thì tồn nguồn thay đổi, nên phải load lại nguyên liệu còn tồn.
- Đổi nguyên liệu thì nếu đang chọn lô thủ công phải load lại lô của nguyên liệu đó.
- Tick chọn lô thủ công thì bật bảng lô và load lô.
- Nhập số lượng trong bảng lô thì tổng số lượng dòng xuất được đồng bộ về input.

### 25.2.3 Load lookup ban đầu

Constructor gọi:

```text
loadLookups()
```

Chi tiết:

```text
loadLookups()
-> setFormEnabled(false)
-> statusLabel = "Đang tải dữ liệu xuất kho..."
-> SwingWorker<InventoryExportLookupDto, Void>
   -> doInBackground()
      -> inventoryApiClient.getExportLookups()
      -> GET /api/inventory/exports/lookups
   -> done()
      -> warehouseCombo.setModel(lookup.getWarehouses())
      -> exportTypeCombo.setModel(lookup.getExportTypes())
      -> setFormEnabled(true)
      -> loadStockForSelectedWarehouse()
```

Backend call chain:

```text
GET /api/inventory/exports/lookups
-> InventoryController.getExportLookups()
-> InventoryService.getExportLookups()
-> lấy danh sách kho được xuất và loại xuất
-> trả BaseResponse<InventoryExportLookupDto>
```

### 25.2.4 Sau khi load lookup: load tồn theo kho

```text
loadStockForSelectedWarehouse()
```

Chi tiết:

```text
loadStockForSelectedWarehouse()
-> warehouse = warehouseCombo.getSelectedItem()
-> nếu warehouse null thì return
-> statusLabel = "Đang tải tồn kho..."
-> SwingWorker<List<InventoryStockDto>, Void>
   -> doInBackground()
      -> inventoryApiClient.getExportStock(warehouse.getId())
      -> GET /api/inventory/exports/stock?maKho=<id>
   -> done()
      -> allIngredients = get()
      -> ingredientCombo.setModel(allIngredients)
      -> populateIngredientTable(allIngredients)
      -> statusLabel = "Đã tải ... nguyên liệu còn tồn"
      -> refreshLotsIfManual()
```

Backend call chain:

```text
GET /api/inventory/exports/stock?maKho=...
-> InventoryController.getExportStock(...)
-> InventoryService.getExportStock(...)
-> query TONKHO theo kho, chỉ lấy nguyên liệu còn tồn
-> trả BaseResponse<List<InventoryStockDto>>
```

### 25.2.5 Chọn kho xuất

Người dùng đổi `warehouseCombo`:

```text
warehouseCombo ActionListener
-> loadStockForSelectedWarehouse()
-> refreshLotsIfManual()
```

Kết quả:

```text
loadStockForSelectedWarehouse()
-> load lại bảng nguyên liệu theo kho mới
-> reset ingredientCombo theo kho mới
-> nếu đang manual mode thì refreshLotsIfManual() sẽ load lô theo kho mới + nguyên liệu đang chọn
```

### 25.2.6 Chọn nguyên liệu

Có 2 cách chọn:

```text
Cách 1: chọn trong ingredientCombo
-> ingredientCombo ActionListener
-> refreshLotsIfManual()

Cách 2: click bảng ingredientTable
-> fillSelectedIngredientToDetail()
-> ingredientCombo.setSelectedItem(...)
-> refreshLotsIfManual()
```

Nếu chưa bật `manualLotCheckBox`, `refreshLotsIfManual()` return sớm, không gọi API lô.

### 25.2.7 Tick chọn lô thủ công

```text
manualLotCheckBox ActionListener
-> handleManualModeChanged()
```

Chi tiết:

```text
handleManualModeChanged()
-> nếu đang bật thủ công và phiếu đã có lines:
   -> hỏi confirm vì đổi chế độ có thể làm lệch dòng đã thêm
   -> nếu user không đồng ý:
      -> set checkbox về trạng thái cũ
      -> return
   -> nếu đồng ý:
      -> resetReceipt()
-> nếu bật thủ công:
   -> lotTable.setEnabled(true)
   -> refreshLotsIfManual()
-> nếu tắt thủ công:
   -> lotModel.setRowCount(0)
   -> quantityField.setEditable(true)
```

### 25.2.8 Load lô thủ công

```text
refreshLotsIfManual()
```

Chi tiết:

```text
refreshLotsIfManual()
-> nếu manualLotCheckBox chưa selected:
   -> return
-> warehouse = selected warehouse
-> ingredient = selected ingredient
-> nếu thiếu warehouse/ingredient:
   -> lotModel.setRowCount(0)
   -> return
-> SwingWorker<List<InventoryLotDto>, Void>
   -> doInBackground()
      -> inventoryApiClient.getExportLots(warehouseId, ingredientId)
      -> GET /api/inventory/exports/lots?maKho=&maNguyenLieu=
   -> done()
      -> populateLotTable(lots)
```

Backend call chain:

```text
GET /api/inventory/exports/lots?maKho=...&maNguyenLieu=...
-> InventoryController.getExportLots(...)
-> InventoryService.getExportLots(...)
-> query LOHANG_NGUYENLIEU còn tồn
-> ORDER BY han_su_dung ASC, ngay_tao ASC để gợi ý FEFO
-> trả BaseResponse<List<InventoryLotDto>>
```

### 25.2.9 Nhập số lượng trong bảng lô

Người dùng nhập `SL xuất` ở `lotTable`:

```text
lotModel TableModelListener
-> updateManualQuantityFromLots()
```

Chi tiết:

```text
updateManualQuantityFromLots()
-> duyệt từng row lotModel
-> đọc cột SL xuất
-> parse BigDecimal
-> cộng tổng số lượng hợp lệ
-> quantityField.setText(total)
```

Không gọi backend. Đây chỉ là cộng tổng từ các lô thủ công.

### 25.2.10 Bấm Thêm vào phiếu

```text
addToListButton ActionListener
-> addLine()
```

Chi tiết:

```text
addLine()
-> đọc ingredient đang chọn
-> đọc quantityField
-> validate số lượng > 0
-> nếu manualLotCheckBox selected:
   -> collectManualLotSelections(requiredQty)
      -> duyệt lotModel
      -> lấy các dòng có SL xuất > 0
      -> validate không vượt số lượng còn lại của lô
      -> validate tổng lô = quantityField hoặc theo rule hiện tại
      -> trả List<ExportLotSelectionRequest>
   -> nếu collectManualLotSelections trả null:
      -> return
-> nếu không thủ công:
   -> manualLots = empty
   -> backend sẽ tự FEFO
-> tạo ExportLine local
-> lines.add(line)
-> populate/exportModel.addRow(...)
-> updateTotal()
-> clearLineInputs()
-> nếu thủ công:
   -> refreshLotsIfManual()
```

Không gọi backend ở bước thêm dòng. Dòng chỉ nằm ở local `lines`.

### 25.2.11 Bấm Lưu phiếu

```text
saveButton ActionListener
-> saveReceipt()
```

Chi tiết:

```text
saveReceipt()
-> đọc warehouseCombo
-> đọc exportTypeCombo
-> validate có kho, loại xuất, lines không rỗng
-> tạo CreateExportReceiptRequest
   -> maKho
   -> loaiXuat
   -> chonLoThuCong = manualLotCheckBox.isSelected()
   -> ghiChu
   -> items
-> mỗi ExportLine -> CreateExportReceiptItemRequest
   -> maNguyenLieu
   -> soLuongXuat
   -> manualLots nếu chọn lô thủ công
-> saveButton.setEnabled(false)
-> setFormEnabled(false)
-> SwingWorker<ExportReceiptDto, Void>
   -> doInBackground()
      -> inventoryApiClient.createExportReceipt(request)
      -> POST /api/inventory/exports
   -> done()
      -> saveButton.setEnabled(true)
      -> setFormEnabled(true)
      -> nếu thành công:
         -> show message mã phiếu xuất
         -> resetReceipt()
         -> loadStockForSelectedWarehouse()
         -> nếu manual mode: refreshLotsIfManual()
      -> nếu lỗi: showError
```

Backend call chain:

```text
POST /api/inventory/exports
-> InventoryController.createExportReceipt(CreateExportReceiptRequest request)
-> InventoryService.createExportReceipt(request)
-> validate kho, loại xuất, items
-> nếu chonLoThuCong = false:
   -> tự phân bổ lô theo FEFO
   -> ORDER BY han_su_dung ASC, ngay_tao ASC
-> nếu chonLoThuCong = true:
   -> dùng danh sách lotSelections frontend gửi lên
-> INSERT PHIEUXUAT
-> INSERT CHITIETPHIEUXUAT
-> trừ LOHANG_NGUYENLIEU
-> trừ TONKHO
-> INSERT NHATKY_KHO loại EXPORT
-> trả BaseResponse<ExportReceiptDto>
```

### 25.2.12 Bấm Hủy

```text
cancelButton ActionListener
-> nếu lines đang có dữ liệu có thể confirm
-> resetReceipt()
```

`resetReceipt()`:

```text
resetReceipt()
-> lines.clear()
-> exportModel.setRowCount(0)
-> noteArea.setText("")
-> lotModel.setRowCount(0)
-> updateTotal()
-> clearLineInputs()
```

Không gọi backend.

## 25.3 Điều chuyển kho - `DieuChuyenKhoFrame`

### 25.3.1 Mở màn hình

```text
KhoMenuFrame.handleMenuClick(...)
-> mở DieuChuyenKhoFrame
-> DieuChuyenKhoFrame.DieuChuyenKhoFrame()
```

Constructor:

```text
DieuChuyenKhoFrame()
-> setTitle("Phụng Lộc - Điều chuyển kho")
-> tạo root panel
-> buildHeader()
-> buildReceiptInfo()
-> buildTables()
-> buildInputRow()
-> buildLotSection()
-> buildFooter()
-> bindEvents()
-> loadLookups()
```

Build UI:

```text
buildHeader()
-> tiêu đề + nút Quay lại

buildReceiptInfo()
-> sourceCombo: kho nguồn
-> destinationCombo: kho đích
-> dateField: ngày chuyển
-> creatorField: người tạo
-> manualLotCheckBox: chọn lô thủ công

buildTables()
-> ingredientTable: danh sách nguyên liệu còn tồn ở kho nguồn
-> transferTable: dòng điều chuyển đã thêm
-> ingredientTable selection -> selectIngredientFromTable()

buildInputRow()
-> ingredientCombo
-> quantityField
-> addButton -> addLine()
-> removeButton -> removeLine()

buildLotSection()
-> lotTable để nhập SL chuyển theo từng lô nếu chọn thủ công

buildFooter()
-> noteArea
-> cancelButton -> resetForm()
-> saveButton -> saveTransfer()
```

### 25.3.2 Bind events

```text
bindEvents()
-> sourceCombo.addActionListener(...)
   -> nếu !loading: loadStockForSource()

-> ingredientCombo.addActionListener(...)
   -> loadLotsForSelectedIngredient()

-> manualLotCheckBox.addActionListener(...)
   -> loadLotsForSelectedIngredient()
```

Điểm khác với xuất kho:

- Điều chuyển có `sourceCombo` và `destinationCombo`.
- Đổi kho nguồn mới cần load lại tồn nguồn.
- Kho đích không làm thay đổi danh sách nguyên liệu nguồn, nhưng khi lưu backend sẽ kiểm tra kho đích khác kho nguồn.

### 25.3.3 Load lookup ban đầu

```text
loadLookups()
-> loading = true
-> setEnabledForm(false)
-> statusLabel = "Đang tải dữ liệu điều chuyển..."
-> SwingWorker<InventoryTransferLookupDto, Void>
   -> doInBackground()
      -> inventoryApiClient.getTransferLookups()
      -> GET /api/inventory/transfers/lookups
   -> done()
      -> sourceCombo.setModel(data.getSourceWarehouses())
      -> destinationCombo.setModel(data.getDestinationWarehouses())
      -> loading = false
      -> setEnabledForm(true)
      -> loadStockForSource()
```

Backend call chain:

```text
GET /api/inventory/transfers/lookups
-> InventoryController.getTransferLookups()
-> InventoryService.getTransferLookups()
-> lấy danh sách kho nguồn/kho đích user được phép thao tác
-> trả BaseResponse<InventoryTransferLookupDto>
```

### 25.3.4 Chọn kho nguồn

```text
sourceCombo ActionListener
-> nếu !loading:
   -> loadStockForSource()
```

Chi tiết:

```text
loadStockForSource()
-> source = selectedOption(sourceCombo)
-> nếu source null thì return
-> loading = true
-> statusLabel = "Đang tải tồn kho nguồn..."
-> SwingWorker<List<InventoryStockDto>, Void>
   -> doInBackground()
      -> inventoryApiClient.getTransferStock(source.getId())
      -> GET /api/inventory/transfers/stock?maKhoNguon=<id>
   -> done()
      -> sourceIngredients = get()
      -> populateIngredientTable(sourceIngredients)
      -> ingredientCombo.setModel(sourceIngredients)
      -> loading = false
      -> loadLotsForSelectedIngredient()
      -> statusLabel = "Đã tải ... nguyên liệu còn tồn."
```

Backend call chain:

```text
GET /api/inventory/transfers/stock?maKhoNguon=...
-> InventoryController.getTransferStock(...)
-> InventoryService.getTransferStock(...)
-> query TONKHO của kho nguồn, chỉ lấy nguyên liệu còn tồn
-> trả BaseResponse<List<InventoryStockDto>>
```

### 25.3.5 Chọn nguyên liệu

Có 2 hướng:

```text
Click ingredientTable
-> selectIngredientFromTable()
-> lấy id nguyên liệu từ bảng
-> set selected item trong ingredientCombo
-> loadLotsForSelectedIngredient()

Chọn ingredientCombo
-> ingredientCombo ActionListener
-> loadLotsForSelectedIngredient()
```

### 25.3.6 Tick chọn lô thủ công

```text
manualLotCheckBox ActionListener
-> loadLotsForSelectedIngredient()
```

Chi tiết:

```text
loadLotsForSelectedIngredient()
-> nếu manualLotCheckBox chưa selected:
   -> lotModel.setRowCount(0)
   -> return
-> source = selected sourceCombo
-> ingredient = selectedIngredient()
-> nếu thiếu source/ingredient:
   -> lotModel.setRowCount(0)
   -> return
-> SwingWorker<List<InventoryLotDto>, Void>
   -> doInBackground()
      -> inventoryApiClient.getTransferLots(sourceId, ingredientId)
      -> GET /api/inventory/transfers/lots?maKho=&maNguyenLieu=
   -> done()
      -> populateLotTable(rows)
```

Backend call chain:

```text
GET /api/inventory/transfers/lots?maKho=...&maNguyenLieu=...
-> InventoryController.getTransferLots(...)
-> InventoryService.getTransferLots(...)
-> query LOHANG_NGUYENLIEU còn tồn ở kho nguồn
-> trả BaseResponse<List<InventoryLotDto>>
```

### 25.3.7 Bấm Thêm vào phiếu

```text
addButton ActionListener
-> addLine()
```

Chi tiết:

```text
addLine()
-> ingredient = selectedIngredient()
-> qty = parsePositive(quantityField.getText())
-> validate ingredient != null
-> validate qty > 0
-> validate qty <= ingredient.getSoLuongTon()
-> nếu manualLotCheckBox selected:
   -> manualLots = collectManualLots()
   -> validate manualLots không rỗng nếu đang thủ công
   -> validate tổng manualLots hợp lệ theo qty
-> nếu không thủ công:
   -> manualLots empty, backend tự FEFO
-> tạo TransferLine
   -> maNguyenLieu
   -> tenNguyenLieu
   -> dvt
   -> soLuong
   -> manualLots
-> lines.add(line)
-> populateTransferTable()
-> quantityField.setText("")
```

Không gọi backend.

### 25.3.8 Nhập số lượng ở bảng lô thủ công

Trong điều chuyển hiện tại, số lượng lô thủ công được đọc khi bấm `Thêm vào phiếu`, qua:

```text
collectManualLots()
-> duyệt lotModel
-> đọc cột SL chuyển
-> nếu có nhập thì parsePositive(...)
-> tạo ManualLot(lotId, qty)
-> trả List<ManualLot>
```

Nếu muốn giống xuất kho hơn, có thể bổ sung `TableModelListener` để tự cộng tổng vào `quantityField`, nhưng hiện tại luồng đang là: người dùng nhập số lượng tổng ở `quantityField`, rồi nhập chi tiết lô, sau đó `addLine()` validate.

### 25.3.9 Bấm Xóa dòng

```text
removeButton ActionListener
-> removeLine()
```

Chi tiết:

```text
removeLine()
-> row = transferTable.getSelectedRow()
-> nếu row không hợp lệ: warn(...)
-> lines.remove(row)
-> populateTransferTable()
```

Không gọi backend.

### 25.3.10 Bấm Lưu phiếu

```text
saveButton ActionListener
-> saveTransfer()
```

Chi tiết:

```text
saveTransfer()
-> source = selectedOption(sourceCombo)
-> dest = selectedOption(destinationCombo)
-> validate source != null và dest != null
-> validate source.id != dest.id
-> validate lines không rỗng
-> tạo CreateTransferReceiptRequest
   -> maKhoNguon = source.id
   -> maKhoDich = dest.id
   -> chonLoThuCong = manualLotCheckBox.isSelected()
   -> ghiChu = noteArea.getText().trim()
   -> items = buildRequestItems()
-> saveButton.setEnabled(false)
-> statusLabel = "Đang lưu phiếu điều chuyển..."
-> SwingWorker<TransferReceiptDto, Void>
   -> doInBackground()
      -> inventoryApiClient.createTransferReceipt(request)
      -> POST /api/inventory/transfers
   -> done()
      -> saveButton.setEnabled(true)
      -> nếu thành công:
         -> show message mã phiếu điều chuyển
         -> resetForm()
         -> loadStockForSource()
      -> nếu lỗi:
         -> showError(ex)
```

`buildRequestItems()`:

```text
buildRequestItems()
-> duyệt lines
-> mỗi TransferLine -> CreateTransferReceiptItemRequest
   -> maNguyenLieu
   -> soLuongDieuChuyen
   -> lotSelections nếu có chọn lô thủ công
-> trả List<CreateTransferReceiptItemRequest>
```

Backend call chain:

```text
POST /api/inventory/transfers
-> InventoryController.createTransferReceipt(CreateTransferReceiptRequest request)
-> InventoryService.createTransferReceipt(request)
-> validate kho nguồn, kho đích, items
-> nếu không chọn lô thủ công:
   -> backend tự phân bổ lô nguồn theo FEFO
-> nếu chọn thủ công:
   -> dùng lotSelections frontend gửi lên
-> INSERT PHIEUDIEUCHUYEN
-> INSERT CHITIETPHIEUDIEUCHUYEN
-> trừ lô nguồn trong LOHANG_NGUYENLIEU
-> tạo hoặc cộng lô đích trong LOHANG_NGUYENLIEU
-> cập nhật ma_lo_hang_nguon, ma_lo_hang_dich nếu schema đang dùng
-> trừ TONKHO kho nguồn
-> cộng TONKHO kho đích
-> INSERT NHATKY_KHO TRANSFER_OUT
-> INSERT NHATKY_KHO TRANSFER_IN
-> trả BaseResponse<TransferReceiptDto>
```

### 25.3.11 Bấm Hủy

```text
cancelButton ActionListener
-> resetForm()
```

`resetForm()`:

```text
resetForm()
-> lines.clear()
-> populateTransferTable()
-> noteArea.setText("")
-> quantityField.setText("")
-> lotModel.setRowCount(0)
```

Không gọi backend.

## 25.4 Xem tồn kho - `XemTonKhoFrame`

### 25.4.1 Mở màn hình

```text
KhoMenuFrame.handleMenuClick(...)
-> mở XemTonKhoFrame
-> XemTonKhoFrame.XemTonKhoFrame()
```

Constructor:

```text
XemTonKhoFrame()
-> setTitle(...)
-> tạo root panel
-> buildHeader()
-> buildSummaryCards()
-> buildFilters()
-> buildTables()
-> buildFooter()
-> loadData(true)
```

Build UI:

```text
buildHeader()
-> dựng tiêu đề + nút Quay lại

buildSummaryCards()
-> dựng các card: tổng số lượng, ổn định, tồn thấp, hết hàng

buildFilters()
-> searchField
-> warehouseCombo
-> ingredientCombo
-> statusCombo
-> filterButton.addActionListener(...)
   -> currentPage = 0
   -> loadData(false)
-> resetButton.addActionListener(...)
   -> resetFilters()

buildTables()
-> stockTable: danh sách tồn kho
-> lotTable: lô hàng còn tồn
-> stockTable selection listener
   -> loadLotsForSelectedStock()

buildFooter()
-> prevButton.addActionListener(...)
   -> currentPage--
   -> loadData(false)
-> nextButton.addActionListener(...)
   -> currentPage++
   -> loadData(false)
```

### 25.4.2 Load data chính

```text
loadData(refreshFilters)
-> nếu loading thì return
-> loading = true
-> đọc filter keyword/kho/nguyên liệu/trạng thái/page
-> SwingWorker<ScreenData, Void>
   -> doInBackground()
      -> apiClient.getStock(...)
         -> GET /api/inventory/stock
      -> apiClient.getStockSummary(...)
         -> GET /api/inventory/stock/summary
      -> nếu refreshFilters hoặc !filtersLoaded:
         -> dùng rows hoặc API lookup hiện có để populate filter
   -> done()
      -> populateStockTable(page.content)
      -> updateSummary(summary)
      -> populateFilters(...) nếu cần
      -> loadLotsForSelectedStock()
      -> loading = false
```

Backend call chain:

```text
GET /api/inventory/stock
-> InventoryController.getInventoryStock(...)
-> InventoryService.getInventoryStock(...)
-> query TONKHO JOIN KHO JOIN NGUYENLIEU JOIN DONVITINH
-> trả BaseResponse<PageResponse<InventoryDto>>

GET /api/inventory/stock/summary
-> InventoryController.getInventoryStockSummary(...)
-> InventoryService.getInventoryStockSummary(...)
-> aggregate TONKHO theo filter
-> trả BaseResponse<StockSummaryDto>
```

### 25.4.3 Chọn một dòng tồn kho

```text
stockTable selection changed
-> loadLotsForSelectedStock()
```

Chi tiết:

```text
loadLotsForSelectedStock()
-> đọc maKho và maNguyenLieu từ dòng selected
-> nếu chưa chọn dòng: lotModel.setRowCount(0), return
-> apiClient.getStockLots(maKho, maNguyenLieu)
   -> GET /api/inventory/stock/lots?maKho=&maNguyenLieu=
-> done()
   -> populateLotTable(rows)
```

Backend:

```text
GET /api/inventory/stock/lots
-> InventoryController.getInventoryStockLots(...)
-> InventoryService.getInventoryStockLots(...)
-> query LOHANG_NGUYENLIEU còn tồn theo kho/nguyên liệu
-> trả BaseResponse<List<BatchInventoryDto>>
```

### 25.4.4 Reset bộ lọc

```text
resetButton -> resetFilters()
-> searchField.setText("")
-> warehouseCombo về Tất cả kho
-> ingredientCombo về Tất cả nguyên liệu
-> statusCombo về Tất cả trạng thái
-> currentPage = 0
-> loadData(false)
```

## 25.5 Theo dõi hạn sử dụng - `TheoDoiHanSuDungFrame`

### 25.5.1 Mở màn hình

```text
KhoMenuFrame.handleMenuClick(...)
-> mở TheoDoiHanSuDungFrame
-> TheoDoiHanSuDungFrame.TheoDoiHanSuDungFrame()
```

Constructor:

```text
TheoDoiHanSuDungFrame()
-> buildHeader()
-> buildSummaryCards()
-> buildFilters()
-> buildTable()
-> buildFooter()
-> loadData(true)
```

Build UI:

```text
buildFilters()
-> keywordField
-> warehouseCombo
-> ingredientCombo
-> warningDaysField
-> onlyAvailableCheckBox
-> lotStatusCombo
-> warningLevelCombo
-> remainingDaysField
-> filterButton -> loadData(false)
-> resetButton -> resetFilters()
-> refreshButton/Rà soát HSD -> refreshExpiredLots()
```

### 25.5.2 Load dữ liệu

```text
loadData(loadLookups)
-> nếu loading return
-> đọc filter hiện tại
-> loading = true
-> SwingWorker<ScreenData, Void>
   -> doInBackground()
      -> nếu loadLookups:
         -> apiClient.getLookups()
            -> GET /api/inventory/expiry/lookups
      -> apiClient.getExpiryLots(...)
         -> GET /api/inventory/expiry/lots
      -> apiClient.getStatistics(...)
         -> GET /api/inventory/expiry/statistics
   -> done()
      -> nếu có lookups: populateLookups(lookups)
      -> loadedLots = data.lots
      -> populateLotTable(filterByKeyword(loadedLots))
      -> updateSummary(data.statistics)
      -> loading = false
```

Backend:

```text
GET /api/inventory/expiry/lookups
-> ExpiryController.getLookups()
-> ExpiryService.getLookups()

GET /api/inventory/expiry/lots
-> ExpiryController.getExpiryLots(...)
-> ExpiryService.getExpiryLots(...)
-> query LOHANG_NGUYENLIEU JOIN KHO JOIN NGUYENLIEU
-> tính còn bao nhiêu ngày
-> phân loại cảnh báo

GET /api/inventory/expiry/statistics
-> ExpiryController.getStatistics(...)
-> ExpiryService.getStatistics(...)
-> aggregate tổng số lô, sắp hết hạn, đã hết hạn, không có HSD
```

### 25.5.3 Bấm Rà soát HSD

```text
refreshButton ActionListener
-> refreshExpiredLots()
```

Chi tiết:

```text
refreshExpiredLots()
-> nếu loading return
-> loading = true
-> SwingWorker<ExpiryRefreshDto, Void>
   -> doInBackground()
      -> apiClient.refreshExpiredLots()
      -> POST /api/inventory/expiry/refresh
   -> done()
      -> showMessage số lô đã rà soát/cập nhật
      -> loading = false
      -> loadData(false)
```

Backend:

```text
POST /api/inventory/expiry/refresh
-> ExpiryController.refreshExpiredLots()
-> ExpiryService.refreshExpiredLots()
-> rà các lô hết hạn
-> cập nhật trạng thái nếu backend đang hỗ trợ
-> trả BaseResponse<ExpiryRefreshDto>
```

## 25.6 Tra cứu lịch sử kho - `TraCuuLichSuKhoFrame`

### 25.6.1 Mở màn hình

```text
KhoMenuFrame.handleMenuClick(...)
-> mở TraCuuLichSuKhoFrame
-> TraCuuLichSuKhoFrame.TraCuuLichSuKhoFrame()
```

Constructor:

```text
TraCuuLichSuKhoFrame()
-> buildHeader()
-> buildFilterCard()
-> buildSummaryCards()
-> buildHistoryTable()
-> buildFooter()
-> loadData(true)
```

Build UI:

```text
buildFilterCard()
-> keywordField
-> warehouseCombo
-> ingredientCombo
-> transactionTypeCombo
-> fromDateField
-> toDateField
-> filterButton -> loadData(false)
-> resetButton -> resetFilters()
```

### 25.6.2 Load data

```text
loadData(loadLookups)
-> nếu loading return
-> đọc filter
-> loading = true
-> SwingWorker<ScreenData, Void>
   -> doInBackground()
      -> nếu loadLookups hoặc !lookupsLoaded:
         -> apiClient.getLookups()
            -> GET /api/inventory/history/lookups
      -> apiClient.getHistory(...)
         -> GET /api/inventory/history
      -> apiClient.getSummary(...)
         -> GET /api/inventory/history/summary
   -> done()
      -> populateLookups(lookups) nếu có
      -> populateHistoryTable(historyRows)
      -> updateSummary(summaryRows, historyRows)
      -> loading = false
```

Backend:

```text
GET /api/inventory/history/lookups
-> InventoryHistoryController.getLookups()
-> InventoryHistoryService.getLookups()

GET /api/inventory/history
-> InventoryHistoryController.getHistory(...)
-> InventoryHistoryService.getHistory(...)
-> query NHATKY_KHO JOIN KHO JOIN NGUYENLIEU LEFT JOIN NGUOIDUNG

GET /api/inventory/history/summary
-> InventoryHistoryController.getSummary(...)
-> InventoryHistoryService.getSummary(...)
-> aggregate lịch sử theo loại giao dịch hoặc theo filter
```

### 25.6.3 Reset bộ lọc

```text
resetButton -> resetFilters()
-> clear keyword/fromDate/toDate
-> combo về tất cả
-> currentPage = 0 nếu có phân trang
-> loadData(false)
```

## 25.7 Báo cáo hao hụt - `BaoCaoHaoHutFrame`

### 25.7.1 Mở màn hình

```text
KhoMenuFrame.handleMenuClick(...)
-> mở BaoCaoHaoHutFrame
-> BaoCaoHaoHutFrame.BaoCaoHaoHutFrame()
```

Constructor:

```text
BaoCaoHaoHutFrame()
-> buildHeader()
-> buildReportInfo()
-> buildTables()
-> buildDetailCard()
-> buildNoteAndFooter()
-> bindEvents()
-> loadLookups()
```

Build UI:

```text
buildReportInfo()
-> warehouseCombo
-> ingredientCombo
-> wastageTypeCombo phía trên để lọc lịch sử
-> reportDateField
-> reporterField

buildTables()
-> lotTable: lô hàng còn tồn
-> historyTable: phiếu hao hụt gần đây
-> lotTable selection -> fillSelectedLotToForm()

buildDetailCard()
-> lotCombo
-> quantityField
-> remainingField
-> expiryField
-> detailWastageTypeCombo nếu frame hiện tại có
-> reloadButton -> loadLotsAndHistory()
-> saveButton -> saveWastage()

buildNoteAndFooter()
-> noteArea
-> cancelButton -> resetForm()
```

### 25.7.2 Bind events

```text
bindEvents()
-> warehouseCombo ActionListener
   -> nếu !loading: loadLotsAndHistory()
-> ingredientCombo ActionListener
   -> nếu !loading: loadLotsAndHistory()
-> wastageTypeCombo ActionListener
   -> nếu !loading: loadHistoryOnly()
-> lotCombo ActionListener
   -> updateLotFieldsFromCombo()
```

Ý nghĩa:

- Đổi kho/nguyên liệu phải load lại lô và lịch sử.
- Đổi loại hao hụt phía trên chỉ lọc lịch sử, không đổi lô.
- Đổi lô trong chi tiết thì tự fill số lượng còn lại/hạn sử dụng.

### 25.7.3 Load lookup

```text
loadLookups()
-> loading = true
-> setFormEnabled(false)
-> SwingWorker<WastageLookupDto, Void>
   -> doInBackground()
      -> apiClient.getLookups()
      -> GET /api/inventory/wastages/lookups
   -> done()
      -> warehouseCombo.setModel(...)
      -> ingredientCombo.setModel(...)
      -> wastageTypeCombo.setModel(...)
      -> detailWastageTypeCombo.setModel(...) nếu có
      -> loading = false
      -> setFormEnabled(true)
      -> loadLotsAndHistory()
```

Backend:

```text
GET /api/inventory/wastages/lookups
-> WastageController.getLookups()
-> WastageService.getLookups()
-> trả kho, nguyên liệu, loại hao hụt
```

### 25.7.4 Load lô và lịch sử

```text
loadLotsAndHistory()
-> nếu loading return
-> đọc kho/nguyên liệu/loại hao hụt filter
-> loading = true
-> SwingWorker<ScreenData, Void>
   -> doInBackground()
      -> apiClient.getAvailableLots(maKho, maNguyenLieu)
         -> GET /api/inventory/wastages/lots
      -> apiClient.getWastages(maKho, maNguyenLieu, loaiHaoHut, ...)
         -> GET /api/inventory/wastages
   -> done()
      -> populateLotTable(lots)
      -> lotCombo.setModel(lots)
      -> populateHistoryTable(history)
      -> updateLotFieldsFromCombo()
      -> loading = false
```

Backend:

```text
GET /api/inventory/wastages/lots
-> WastageController.getAvailableLots(...)
-> WastageService.getAvailableLots(...)

GET /api/inventory/wastages
-> WastageController.getWastages(...)
-> WastageService.getWastages(...)
```

### 25.7.5 Chọn lô

```text
Click lotTable
-> fillSelectedLotToForm()
-> lotCombo.setSelectedItem(lot)
-> updateLotFieldsFromCombo()

Chọn lotCombo
-> updateLotFieldsFromCombo()
-> remainingField = số lượng còn lại
-> expiryField = hạn sử dụng
```

### 25.7.6 Bấm Lưu báo cáo

```text
saveButton -> saveWastage()
```

Chi tiết:

```text
saveWastage()
-> đọc selected lot
-> đọc quantityField
-> đọc loại hao hụt ở phần chi tiết
-> đọc noteArea
-> validate có lô, số lượng > 0, số lượng <= còn lại
-> tạo CreateWastageRequest
   -> maKho
   -> maNguyenLieu
   -> maLoHang
   -> soLuongHaoHut
   -> loaiHaoHut
   -> ghiChu
-> saveButton.setEnabled(false)
-> SwingWorker<WastageDto, Void>
   -> doInBackground()
      -> apiClient.createWastage(request)
      -> POST /api/inventory/wastages
   -> done()
      -> saveButton.setEnabled(true)
      -> show message
      -> resetForm()
      -> loadLotsAndHistory()
```

Backend:

```text
POST /api/inventory/wastages
-> WastageController.createWastage(CreateWastageRequest request)
-> WastageService.createWastage(request)
-> validate lô còn tồn
-> INSERT PHIEUHAOHUT
-> trừ LOHANG_NGUYENLIEU
-> trừ TONKHO
-> INSERT NHATKY_KHO WASTAGE
-> trả BaseResponse<WastageDto>
```

## 25.8 Kiểm kho - `KiemKhoFrame`

### 25.8.1 Mở màn hình

```text
KhoMenuFrame.handleMenuClick(...)
-> mở KiemKhoFrame
-> KiemKhoFrame.KiemKhoFrame()
```

Constructor:

```text
KiemKhoFrame()
-> buildHeader()
-> buildFilterCard()
-> buildTables()
-> buildDetailCard()
-> buildItemTable()
-> buildFooter()
-> bindEvents()
-> loadLookups()
```

Build UI:

```text
buildFilterCard()
-> warehouseCombo
-> ingredientCombo
-> dateField
-> inspectorField
-> reloadButton -> loadSystemStockAndHistory()

buildTables()
-> stockTable: tồn hệ thống theo lô
-> historyTable: phiếu kiểm kho gần đây
-> openDraftButton -> openSelectedStocktake()
-> cancelDraftButton -> cancelSelectedStocktake()

buildDetailCard()
-> lotCombo
-> systemQtyField
-> actualQtyField
-> diffField
-> expiryField
-> handlingCombo
-> reasonField
-> addButton -> addOrUpdateLine()

buildItemTable()
-> itemTable: dòng kiểm kho trong phiếu hiện tại

buildFooter()
-> noteArea
-> clearButton -> removeSelectedLine()
-> resetButton -> resetDraft()
-> saveDraftButton -> saveStocktake(false)
-> completeButton -> saveStocktake(true)
```

### 25.8.2 Bind events

```text
bindEvents()
-> warehouseCombo ActionListener
   -> nếu !suppressEvents: loadSystemStockAndHistory()
-> ingredientCombo ActionListener
   -> nếu !suppressEvents: loadSystemStockAndHistory()
-> statusCombo ActionListener
   -> nếu !suppressEvents: loadHistoryOnly()
-> lotCombo ActionListener
   -> updateStockFieldsFromCombo()
-> actualQtyField DocumentListener
   -> updateDiffField()
```

### 25.8.3 Load lookup

```text
loadLookups()
-> loading = true
-> setFormEnabled(false)
-> SwingWorker<StocktakeLookupDto, Void>
   -> doInBackground()
      -> apiClient.getLookups()
      -> GET /api/inventory/stocktakes/lookups
   -> done()
      -> populate warehouseCombo, ingredientCombo, statusCombo, handlingCombo
      -> loading = false
      -> setFormEnabled(true)
      -> loadSystemStockAndHistory()
```

Backend:

```text
GET /api/inventory/stocktakes/lookups
-> StocktakeController.getLookups()
-> StocktakeService.getLookups()
-> trả kho, nguyên liệu, trạng thái phiếu, hướng xử lý
```

### 25.8.4 Load tồn hệ thống và phiếu gần đây

```text
loadSystemStockAndHistory()
-> nếu loading return
-> đọc kho/nguyên liệu/trạng thái
-> loading = true
-> SwingWorker<ScreenData, Void>
   -> doInBackground()
      -> apiClient.getSystemStock(maKho, maNguyenLieu)
         -> GET /api/inventory/stocktakes/system-stock
      -> apiClient.getStocktakes(maKho, maNguyenLieu, trangThai, ...)
         -> GET /api/inventory/stocktakes
   -> done()
      -> populateStockTable(systemStock)
      -> populateHistoryTable(stocktakes)
      -> lotCombo.setModel(systemStock)
      -> updateStockFieldsFromCombo()
      -> updateActionButtons()
      -> loading = false
```

Backend:

```text
GET /api/inventory/stocktakes/system-stock
-> StocktakeController.getSystemStock(...)
-> StocktakeService.getSystemStock(...)
-> query LOHANG_NGUYENLIEU còn tồn

GET /api/inventory/stocktakes
-> StocktakeController.getStocktakes(...)
-> StocktakeService.getStocktakes(...)
-> query PHIEUKIEMKHO + tổng số dòng chi tiết
```

### 25.8.5 Chọn lô và nhập thực tế

```text
Click stockTable
-> fillSelectedStockToForm()
-> selectLot(maLoHang)
-> updateStockFieldsFromCombo()

Chọn lotCombo
-> updateStockFieldsFromCombo()
-> systemQtyField = tồn hệ thống
-> expiryField = hạn sử dụng
-> updateDiffField()
```

Khi nhập số lượng thực tế:

```text
actualQtyField DocumentListener
-> updateDiffField()
-> diff = actualQty - systemQty
-> diffField.setText(...)
-> suggestHandlingForDiff(diff)
```

`suggestHandlingForDiff(diff)`:

```text
nếu diff = 0 -> NO_ACTION
nếu diff < 0 -> có thể gợi ý CREATE_WASTAGE hoặc ADJUST_INVENTORY tùy code hiện tại
nếu diff > 0 -> gợi ý ADJUST_INVENTORY
```

### 25.8.6 Bấm Thêm dòng kiểm kho

```text
addButton -> addOrUpdateLine()
```

Chi tiết:

```text
addOrUpdateLine()
-> đọc lotCombo selected
-> đọc systemQtyField
-> đọc actualQtyField
-> tính diff
-> đọc handlingCombo
-> đọc reasonField
-> validate lô và số thực tế hợp lệ
-> nếu dòng lô đã tồn tại trong draftLines:
   -> update dòng cũ
-> nếu chưa có:
   -> draftLines.add(new DraftLine(...))
-> populateItemTable()
-> updateActionButtons()
```

Không gọi backend.

### 25.8.7 Bấm Xóa dòng

```text
clearButton -> removeSelectedLine()
-> row = itemTable.getSelectedRow()
-> draftLines.remove(row)
-> populateItemTable()
-> updateActionButtons()
```

Không gọi backend.

### 25.8.8 Bấm Lưu nháp

```text
saveDraftButton -> saveStocktake(false)
```

Chi tiết:

```text
saveStocktake(false)
-> validate có kho và draftLines không rỗng
-> tạo StocktakeRequest
   -> maKho
   -> ghiChu
   -> items = draftLines -> StocktakeItemRequest
-> setFormEnabled(false)
-> SwingWorker<StocktakeDto, Void>
   -> doInBackground()
      -> nếu editingStocktakeId == null:
         -> apiClient.createStocktake(request)
      -> nếu editingStocktakeId != null:
         -> apiClient.updateStocktake(editingStocktakeId, request)
      -> vì completeAfterCreate = false: return saved
   -> done()
      -> show message
      -> resetDraft()
      -> loadSystemStockAndHistory()
```

Backend:

```text
POST /api/inventory/stocktakes hoặc PUT /api/inventory/stocktakes/{id}
-> StocktakeController.create/update
-> StocktakeService.create/update
-> lưu PHIEUKIEMKHO trạng thái DRAFT
-> lưu CHITIETPHIEUKIEMKHO
-> chưa điều chỉnh tồn kho nếu chỉ lưu nháp
```

### 25.8.9 Bấm Hoàn tất

```text
completeButton -> saveStocktake(true)
```

Chi tiết:

```text
saveStocktake(true)
-> tạo/lưu StocktakeRequest như lưu nháp
-> SwingWorker
   -> doInBackground()
      -> saved = create hoặc update phiếu
      -> apiClient.completeStocktake(saved.getMaPhieuKiemKho())
      -> POST /api/inventory/stocktakes/{id}/complete
   -> done()
      -> resetDraft()
      -> loadSystemStockAndHistory()
```

Backend:

```text
POST /api/inventory/stocktakes/{id}/complete
-> StocktakeController.completeStocktake(id)
-> StocktakeService.completeStocktake(id)
-> chuyển trạng thái COMPLETED
-> xử lý chênh lệch theo huongXuLy
-> nếu điều chỉnh tồn: cập nhật LOHANG_NGUYENLIEU, TONKHO
-> nếu tạo hao hụt: tạo chứng từ hao hụt hoặc xử lý theo rule backend
-> ghi NHATKY_KHO nếu tồn thay đổi
```

### 25.8.10 Bấm Mở phiếu

```text
openDraftButton -> openSelectedStocktake()
```

Chi tiết:

```text
openSelectedStocktake()
-> đọc id phiếu từ historyTable selected row
-> nếu chưa chọn: warning
-> SwingWorker<StocktakeDto, Void>
   -> doInBackground()
      -> apiClient.getStocktake(id)
      -> GET /api/inventory/stocktakes/{id}
   -> done()
      -> loadStocktakeIntoDraft(stocktake)
```

`loadStocktakeIntoDraft(stocktake)`:

```text
-> editingStocktakeId = stocktake.maPhieuKiemKho
-> set warehouse/date/note theo phiếu
-> draftLines.clear()
-> chuyển items trong response thành draftLines
-> populateItemTable()
-> updateActionButtons()
```

### 25.8.11 Bấm Hủy phiếu

```text
cancelDraftButton -> cancelSelectedStocktake()
```

Chi tiết:

```text
cancelSelectedStocktake()
-> đọc id phiếu đang mở hoặc selected trong historyTable
-> confirm người dùng
-> SwingWorker<StocktakeDto, Void>
   -> doInBackground()
      -> apiClient.cancelStocktake(id)
      -> DELETE /api/inventory/stocktakes/{id}
   -> done()
      -> resetDraft()
      -> loadSystemStockAndHistory()
```

Backend:

```text
DELETE /api/inventory/stocktakes/{id}
-> StocktakeController.cancelStocktake(id)
-> StocktakeService.cancelStocktake(id)
-> kiểm tra quyền STOCKTAKE:MANAGE
-> chuyển trạng thái CANCELLED hoặc hủy mềm
-> trả BaseResponse<StocktakeDto>
```

## 25.9 Cách đọc call chain khi debug lỗi

Khi một nút bị lỗi, đọc theo thứ tự này:

```text
1. Tìm nút trong build...()
   -> xem addActionListener gọi hàm nào

2. Mở hàm xử lý sự kiện
   -> ví dụ saveReceipt(), addLine(), loadData(false)

3. Nếu có SwingWorker
   -> doInBackground() là nơi gọi API
   -> done() là nơi cập nhật UI hoặc hiện lỗi

4. Mở API client tương ứng
   -> xem endpoint, method, request body

5. Mở backend controller
   -> xem endpoint map vào method nào

6. Mở service
   -> xem validate, transaction, repository, update bảng nào

7. Xem response DTO
   -> frontend đang đọc field nào để render bảng/combobox
```

Ví dụ xuất kho:

```text
Bấm Lưu phiếu
-> XuatKhoFrame.saveReceipt()
-> InventoryApiClient.createExportReceipt(request)
-> POST /api/inventory/exports
-> InventoryController.createExportReceipt(request)
-> InventoryService.createExportReceipt(request)
-> cập nhật PHIEUXUAT, CHITIETPHIEUXUAT, LOHANG_NGUYENLIEU, TONKHO, NHATKY_KHO
-> BaseResponse<ExportReceiptDto>
-> XuatKhoFrame.done()
-> resetReceipt()
-> loadStockForSelectedWarehouse()
```
