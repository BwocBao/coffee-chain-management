# LỰA CHỌN CHI TIẾT CÁC CHỨC NĂNG - COFFEE CHAIN MANAGEMENT

## Tổng quan kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (Java Swing)                         │
│              - JFrame: LoginFrame, NhapKhoFrame...              │
│              - Service: AuthApiClient, InventoryApiClient...    │
│              - DTO: LoginResponse, CreateUserRequest...         │
│              - Session Manager (lưu token + user info)          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                HTTP (REST API) - Bearer Token
                             │
┌─────────────────────────────┴────────────────────────────────────┐
│                    BACKEND (Spring Boot)                          │
│              - Controller: AuthController, UserController...      │
│              - Service: AuthService, UserService...              │
│              - Security: AuthGuard, SessionUser, TokenStore      │
│              - Database: Oracle/MySQL                            │
└──────────────────────────────────────────────────────────────────┘
```

---

## 1. CHỨC NĂNG ĐĂNG NHẬP (LOGIN)

### 1.1 Quy trình tổng quan
```
User nhập tài khoản + mật khẩu 
  ↓
Bấm nút "Sign in"
  ↓
Frontend gửi POST request tới API /api/auth/login
  ↓
Backend xác thực tài khoản, tạo token
  ↓
Frontend lưu token + user info vào session
  ↓
Mở màn hình MenuTongFrame (menu chính)
```

### 1.2 Chi tiết Frontend (LoginFrame.java)

**Bước 1: Giao diện**
- File: `coffee-chain-management-frontend/src/main/java/com/coffeechain/ui/LoginFrame.java`
- UI Components:
  - `PromptTextField usernameField` - Ô nhập tên đăng nhập
  - `PromptPasswordField passwordField` - Ô nhập mật khẩu
  - `JCheckBox rememberMe` - Checkbox "Remember Me"
  - `RoundedButton signInButton` - Nút "Sign in"

**Bước 2: Người dùng nhập dữ liệu**
- User nhập username vào field
- User nhập password vào field
- Có thể bấm nút mắt để hiển thị/ẩn password

**Bước 3: Bấm nút "Sign in"**
```java
private void doLogin() {
    String username = usernameField.getText().trim();        // Lấy username
    String password = new String(passwordField.getPassword()); // Lấy password
    
    if (username.isBlank() || password.isBlank()) {
        showMessage("Vui lòng nhập tên đăng nhập và mật khẩu", false);
        return;
    }
    
    signInButton.setEnabled(false);
    signInButton.setText("Signing in...");
    showMessage("Đang đăng nhập...", true);
    
    // Tạo thread mới để không block UI
    new Thread(() -> {
        try {
            // Bước 4: Gọi API login
            LoginResponse response = authApiClient.login(username, password);
            
            // Bước 5: Lưu session
            SessionManager.saveSession(response.getToken(), response.getUser());
            
            // Bước 6: Mở màn hình chính
            SwingUtilities.invokeLater(() -> {
                showMessage("Đăng nhập thành công", true);
                new MenuTongFrame().setVisible(true);
                dispose();
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> 
                showMessage("Đăng nhập thất bại: " + ex.getMessage(), false)
            );
        } finally {
            SwingUtilities.invokeLater(() -> {
                signInButton.setEnabled(true);
                signInButton.setText("Sign in");
            });
        }
    }).start();
}
```

### 1.3 Chi tiết Frontend - API Client (AuthApiClient.java)

**Bước 4: Gọi API từ Frontend**
```java
public LoginResponse login(String tenDangNhap, String matKhau) 
        throws IOException, InterruptedException {
    
    // Tạo LoginRequest
    LoginRequest loginRequest = new LoginRequest(tenDangNhap, matKhau);
    String json = toJson(loginRequest);
    
    // Tạo HTTP POST request
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.LOGIN_URL))  // POST /api/auth/login
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .build();
    
    // Gửi request
    HttpResponse<String> response = send(request);
    
    // Parse response
    BaseResponse<LoginResponse> baseResponse = readBaseResponse(
            response,
            new TypeReference<BaseResponse<LoginResponse>>() {}
    );
    
    return extractData(baseResponse);
}
```

**Dữ liệu gửi (Request Body)**
```json
{
  "tenDangNhap": "admin",
  "matKhau": "admin123"
}
```

### 1.4 Chi tiết Backend - Controller (AuthController.java)

**Bước 5: Backend nhận request**
```java
@PostMapping("/login")
public ResponseEntity<BaseResponse<LoginResponse>> login(
        @RequestBody LoginRequest request) {
    
    // Gọi AuthService để xác thực
    LoginResponse data = authService.login(request);
    
    return ResponseEntity.ok(
            BaseResponse.ok("Đăng nhập thành công", data)
    );
}
```

### 1.5 Chi tiết Backend - Service (AuthService.java)

**Bước 6: Backend xác thực tài khoản**
```java
public LoginResponse login(LoginRequest request) {
    
    // Bước 6.1: Validate request không rỗng
    if (request == null || isBlank(request.getTenDangNhap()) 
            || isBlank(request.getMatKhau())) {
        throw new AppException(
            HttpStatus.BAD_REQUEST, 
            "Vui lòng nhập tên đăng nhập và mật khẩu"
        );
    }
    
    // Bước 6.2: Tìm user trong database
    NguoiDungRecord user = nguoiDungRepository
            .findByTenDangNhap(request.getTenDangNhap().trim())
            .orElseThrow(() -> new AppException(
                HttpStatus.UNAUTHORIZED, 
                "Sai tên đăng nhập hoặc mật khẩu"
            ));
    
    // Bước 6.3: Kiểm tra tài khoản có hoạt động không
    if (!"ACTIVE".equalsIgnoreCase(user.getTrangThai())) {
        throw new AppException(
            HttpStatus.FORBIDDEN, 
            "Tài khoản không hoạt động hoặc đã bị khóa"
        );
    }
    
    // Bước 6.4: Xác thực mật khẩu (so sánh hash)
    if (!PasswordUtil.verifyPassword(
            request.getMatKhau(), 
            user.getMatKhau())) {
        throw new AppException(
            HttpStatus.UNAUTHORIZED, 
            "Sai tên đăng nhập hoặc mật khẩu"
        );
    }
    
    // Bước 6.5: Lấy danh sách quyền của user
    Set<String> permissions = nguoiDungRepository
            .findPermissionsByUserId(user.getMaNguoiDung());
    // Ví dụ: {USER:CREATE, INVENTORY:IMPORT, INVENTORY:EXPORT}
    
    // Bước 6.6: Tạo SessionUser object
    SessionUser sessionUser = new SessionUser();
    sessionUser.setMaNguoiDung(user.getMaNguoiDung());
    sessionUser.setTenDangNhap(user.getTenDangNhap());
    sessionUser.setMaVaiTro(user.getMaVaiTro());
    sessionUser.setTenVaiTro(user.getTenVaiTro());
    sessionUser.setMaChiNhanh(user.getMaChiNhanh());
    sessionUser.setTenChiNhanh(user.getTenChiNhanh());
    sessionUser.setPermissions(permissions);
    sessionUser.setExpiredAt(LocalDateTime.now().plusHours(2)); // Session 2 giờ
    
    // Bước 6.7: Tạo token (lưu SessionUser vào TokenStore)
    String token = tokenStore.createSession(sessionUser);
    // Token format: JWT hoặc UUID
    
    // Bước 6.8: Trả response
    return new LoginResponse(token, toUserInfo(sessionUser));
}
```

**Database Query**
```sql
-- Tìm user
SELECT * FROM NGUOIDUNG 
WHERE TENDANGNHAP = 'admin' AND TRANGTHAI = 'ACTIVE'

-- Lấy quyền của user
SELECT PHANQUYEN.MAPHANQUYEN 
FROM PHANQUYEN
JOIN GIAM_VAI_TRO ON PHANQUYEN.MAPHANQUYEN = GIAM_VAI_TRO.MAPHANQUYEN
WHERE GIAM_VAI_TRO.MAVAITRO = user.MAVAITRO
```

### 1.6 Response Backend trả về

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "maNguoiDung": 1,
      "tenDangNhap": "admin",
      "maVaiTro": 1,
      "tenVaiTro": "ADMIN",
      "maChiNhanh": null,
      "tenChiNhanh": null,
      "permissions": ["USER:CREATE", "INVENTORY:IMPORT", "INVENTORY:EXPORT"],
      "expiredAt": "2024-05-20T14:30:00"
    }
  }
}
```

### 1.7 Frontend lưu session (SessionManager.java)

```java
public static void saveSession(String token, UserInfoResponse user) {
    // Lưu token vào file config hoặc memory
    // Lưu user info vào session object
    // Sử dụng khi gọi các API khác bằng header:
    // Authorization: Bearer <token>
}
```

### 1.8 Mở màn hình chính (MenuTongFrame.java)

- Khóa LoginFrame
- Mở MenuTongFrame (menu chính với các chức năng khác)

---

## 2. CHỨC NĂNG TẠO TÀI KHOẢN (CREATE USER)

### 2.1 Quy trình tổng quan
```
User bấm nút "Tạo tài khoản" / "Thêm người dùng"
  ↓
Mở màn hình TaoTaiKhoanFrame
  ↓
Frontend gọi GET /api/users/lookups để lấy danh sách role và chi nhánh
  ↓
User nhập thông tin: username, email, password, chọn role, chọn chi nhánh
  ↓
Bấm nút "Signup"
  ↓
Frontend ghi POST request tới API /api/users
  ↓
Backend kiểm tra quyền, validate dữ liệu, tạo user trong database
  ↓
Frontend nhận response, hiển thị thông báo thành công
```

### 2.2 Chi tiết Frontend - Load dữ liệu (TaoTaiKhoanFrame.java)

**Bước 1: Mở màn hình tạo tài khoản**
```java
public class TaoTaiKhoanFrame extends JFrame {
    private final UserApiClient userApiClient = new UserApiClient();
    
    public TaoTaiKhoanFrame() {
        setTitle("Phụng Lộc - Tạo tài khoản");
        // ... UI setup ...
        
        // Bước 2: Load dữ liệu
        loadLookups();
    }
    
    private void loadLookups() {
        new SwingWorker<UserLookupResponse, Void>() {
            @Override
            protected UserLookupResponse doInBackground() throws Exception {
                return userApiClient.getCreateUserLookups();
            }
            
            @Override
            protected void done() {
                try {
                    UserLookupResponse data = get();
                    
                    // Populate role combobox
                    roleCombo.removeAllItems();
                    for (OptionDto role : data.getRoles()) {
                        roleCombo.addItem(role);
                    }
                    
                    // Populate branch combobox
                    branchCombo.removeAllItems();
                    for (OptionDto branch : data.getBranches()) {
                        branchCombo.addItem(branch);
                    }
                    
                    statusLabel.setText("Tải dữ liệu thành công");
                } catch (Exception e) {
                    statusLabel.setText("Lỗi: " + e.getMessage());
                }
            }
        }.execute();
    }
}
```

**API Client gọi backend**
```java
public UserLookupResponse getCreateUserLookups() 
        throws IOException, InterruptedException {
    
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ApiConfig.GET_CREATE_USER_LOOKUPS_URL))
            // GET /api/users/lookups
            .header("Authorization", bearerToken()) // Gửi token
            .GET()
            .build();
    
    HttpResponse<String> response = send(request);
    BaseResponse<UserLookupResponse> baseResponse = readBaseResponse(
            response,
            new TypeReference<BaseResponse<UserLookupResponse>>() {}
    );
    
    return extractData(baseResponse);
}
```

### 2.3 Chi tiết Backend - Get Lookups (UserController.java)

**Bước 3: Backend nhận request get lookups**
```java
@GetMapping("/lookups")
public ResponseEntity<BaseResponse<CreateUserLookupResponse>> getCreateUserLookups(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    
    // Kiểm tra user đã đăng nhập và có quyền USER:CREATE
    SessionUser currentUser = authGuard.requirePermission(authHeader, "USER:CREATE");
    
    // Gọi service để lấy dữ liệu
    CreateUserLookupResponse data = userService.getCreateUserLookups(currentUser);
    
    return ResponseEntity.ok(
            BaseResponse.ok("Lấy dữ liệu tạo tài khoản thành công", data)
    );
}
```

### 2.4 Chi tiết Backend - Service Logic

**Bước 4: Backend tạo danh sách role và chi nhánh dựa trên quyền**
```java
public CreateUserLookupResponse getCreateUserLookups(SessionUser currentUser) {
    
    if (currentUser == null) {
        throw new AppException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
    }
    
    String currentRole = normalizeRoleName(currentUser.getTenVaiTro());
    CreateUserLookupResponse response = new CreateUserLookupResponse();
    
    // Trường hợp 1: ADMIN có thể tạo tất cả role
    if (ROLE_ADMIN.equals(currentRole)) {
        // Lấy tất cả role
        response.setRoles(nguoiDungRepository.findRoleOptionsForCreateUser());
        // Lấy tất cả chi nhánh ACTIVE
        response.setBranches(nguoiDungRepository.findActiveBranchOptions());
        return response;
    }
    
    // Trường hợp 2: QUAN_LY_CHI_NHANH chỉ được tạo THU_NGAN
    if (ROLE_QUAN_LY_CHI_NHANH.equals(currentRole)) {
        Long maChiNhanh = currentUser.getMaChiNhanh();
        
        if (maChiNhanh == null) {
            throw new AppException(
                HttpStatus.FORBIDDEN, 
                "Tài khoản quản lý chi nhánh chưa được gán chi nhánh"
            );
        }
        
        // Chỉ trả THU_NGAN
        response.setRoles(
            nguoiDungRepository.findRoleOptionByName(ROLE_THU_NGAN)
                .map(List::of)
                .orElseThrow(() -> new AppException(
                    HttpStatus.BAD_REQUEST, 
                    "Không tìm thấy vai trò THU_NGAN"
                ))
        );
        
        // Chỉ trả chi nhánh của quản lý hiện tại
        response.setBranches(
            nguoiDungRepository.findBranchOptionById(maChiNhanh)
                .map(List::of)
                .orElseThrow(() -> new AppException(
                    HttpStatus.BAD_REQUEST, 
                    "Chi nhánh không tồn tại hoặc không hoạt động"
                ))
        );
        
        return response;
    }
    
    // Role khác không được phép
    throw new AppException(
        HttpStatus.FORBIDDEN, 
        "Bạn không có quyền tạo tài khoản"
    );
}
```

**Response từ Backend**
```json
{
  "success": true,
  "message": "Lấy dữ liệu tạo tài khoản thành công",
  "data": {
    "roles": [
      { "value": 1, "label": "ADMIN" },
      { "value": 3, "label": "QUAN_LY_KHO" },
      { "value": 4, "label": "THU_NGAN" }
    ],
    "branches": [
      { "value": 1, "label": "Chi nhánh Phụng Lộc - Q.1" },
      { "value": 2, "label": "Chi nhánh Quận 3" }
    ]
  }
}
```

### 2.5 Frontend - User nhập dữ liệu và bấm "Signup"

```java
private void bindEvents() {
    createButton.addActionListener(e -> doCreateUser());
}

private void doCreateUser() {
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String password = new String(passwordField.getPassword());
    
    OptionDto selectedRole = (OptionDto) roleCombo.getSelectedItem();
    OptionDto selectedBranch = (OptionDto) branchCombo.getSelectedItem();
    
    // Validate dữ liệu
    if (username.isBlank() || email.isBlank() || password.isBlank()) {
        showMessage("Vui lòng nhập đủ thông tin", false);
        return;
    }
    
    if (selectedRole == null || selectedBranch == null) {
        showMessage("Vui lòng chọn role và chi nhánh", false);
        return;
    }
    
    if (!agreeCheckBox.isSelected()) {
        showMessage("Vui lòng đồng ý với điều khoản", false);
        return;
    }
    
    createButton.setEnabled(false);
    createButton.setText("Creating...");
    
    new Thread(() -> {
        try {
            // Tạo request
            CreateUserRequest request = new CreateUserRequest();
            request.setTenDangNhap(username);
            request.setEmail(email);
            request.setMatKhau(password);
            request.setTenVaiTro(selectedRole.getLabel());
            request.setMaChiNhanh(selectedBranch.getValue());
            
            // Bước 5: Gọi API tạo user
            CreateUserResponse response = userApiClient.createUser(request);
            
            SwingUtilities.invokeLater(() -> {
                showMessage("Tạo tài khoản thành công: " + response.getMaNguoiDung(), true);
                // Sau 2 giây, quay lại menu
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        SwingUtilities.invokeLater(this::goBack);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> 
                showMessage("Lỗi: " + ex.getMessage(), false)
            );
        } finally {
            SwingUtilities.invokeLater(() -> {
                createButton.setEnabled(true);
                createButton.setText("Signup");
            });
        }
    }).start();
}
```

**Request Body gửi tới backend**
```json
{
  "tenDangNhap": "thungan01",
  "email": "thungan01@coffee.vn",
  "matKhau": "123456",
  "tenVaiTro": "THU_NGAN",
  "maChiNhanh": 1
}
```

### 2.6 Chi tiết Backend - Create User (UserController.java)

```java
@PostMapping
public ResponseEntity<BaseResponse<CreateUserResponse>> createUser(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateUserRequest request) {
    
    // Kiểm tra user có quyền USER:CREATE
    SessionUser currentUser = authGuard.requirePermission(authHeader, "USER:CREATE");
    
    // Gọi service để tạo user
    CreateUserResponse data = userService.createUser(currentUser, request);
    
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BaseResponse.created("Tạo tài khoản thành công", data));
}
```

### 2.7 Chi tiết Backend - Service (UserService.java)

```java
@Transactional(rollbackFor = Exception.class)
public CreateUserResponse createUser(SessionUser currentUser, CreateUserRequest request) {
    
    // Bước 6.1: Validate request không rỗng
    if (request == null || isBlank(request.getTenDangNhap()) 
            || isBlank(request.getMatKhau()) || isBlank(request.getTenVaiTro())) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu yêu cầu");
    }
    
    // Bước 6.2: Chuẩn hóa dữ liệu
    String normalizedUsername = request.getTenDangNhap().trim().toLowerCase();
    String normalizedEmail = request.getEmail().trim().toLowerCase();
    String normalizedRole = request.getTenVaiTro().toUpperCase();
    
    // Bước 6.3: Kiểm tra username, email đã tồn tại
    if (nguoiDungRepository.findByTenDangNhap(normalizedUsername).isPresent()) {
        throw new AppException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
    }
    
    if (nguoiDungRepository.findByEmail(normalizedEmail).isPresent()) {
        throw new AppException(HttpStatus.CONFLICT, "Email đã tồn tại");
    }
    
    // Bước 6.4: Kiểm tra vai trò tồn tại
    VaiTroRecord role = vaiTroRepository.findByTenVaiTro(normalizedRole)
            .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Vai trò không tồn tại"));
    
    // Bước 6.5: Xác định chi nhánh được gán
    Long assignedBranch = null;
    
    String currentRole = normalizeRoleName(currentUser.getTenVaiTro());
    
    if (ROLE_ADMIN.equals(currentRole)) {
        // ADMIN tạo ADMIN thì maChiNhanh = null
        if (!ROLE_ADMIN.equals(normalizedRole)) {
            // ADMIN tạo role khác phải có maChiNhanh hợp lệ
            if (request.getMaChiNhanh() == null) {
                throw new AppException(
                    HttpStatus.BAD_REQUEST, 
                    "Phải chỉ định chi nhánh cho role không phải ADMIN"
                );
            }
            assignedBranch = request.getMaChiNhanh();
        }
    } else if (ROLE_QUAN_LY_CHI_NHANH.equals(currentRole)) {
        // QUAN_LY_CHI_NHANH chỉ được tạo THU_NGAN
        if (!ROLE_THU_NGAN.equals(normalizedRole)) {
            throw new AppException(
                HttpStatus.FORBIDDEN, 
                "Bạn chỉ được tạo tài khoản THU_NGAN"
            );
        }
        // Chi nhánh tự động từ quản lý hiện tại
        assignedBranch = currentUser.getMaChiNhanh();
    } else {
        throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền tạo tài khoản");
    }
    
    // Bước 6.6: Hash mật khẩu
    String hashedPassword = PasswordUtil.hashPassword(request.getMatKhau());
    
    // Bước 6.7: Tạo record mới
    NguoiDungRecord newUser = new NguoiDungRecord();
    newUser.setTenDangNhap(normalizedUsername);
    newUser.setEmail(normalizedEmail);
    newUser.setMatKhau(hashedPassword);
    newUser.setMaVaiTro(role.getMaVaiTro());
    newUser.setTenVaiTro(role.getTenVaiTro());
    newUser.setMaChiNhanh(assignedBranch);
    newUser.setTrangThai("ACTIVE");
    newUser.setNgayTao(LocalDateTime.now());
    
    // Bước 6.8: Lưu vào database
    newUser = nguoiDungRepository.save(newUser);
    
    // Bước 6.9: Trả response
    return new CreateUserResponse(
            newUser.getMaNguoiDung(),
            newUser.getTenDangNhap(),
            newUser.getEmail()
    );
}
```

**Database SQL được thực thi**
```sql
-- INSERT người dùng
INSERT INTO NGUOIDUNG 
(TENDANGNHAP, EMAIL, MATKHAUHASHED, MAVAITRO, MACHINHANG, TRANGTHAI, NGAYTAO) 
VALUES ('thungan01', 'thungan01@coffee.vn', 'hash_password', 4, 1, 'ACTIVE', NOW())

-- Kết quả: ID mới = 15 (auto increment)
```

**Response từ Backend**
```json
{
  "success": true,
  "message": "Tạo tài khoản thành công",
  "data": {
    "maNguoiDung": 15,
    "tenDangNhap": "thungan01",
    "email": "thungan01@coffee.vn"
  }
}
```

---

## 3. CHỨC NĂNG NHẬP KHO (IMPORT WAREHOUSE)

### 3.1 Quy trình tổng quan
```
User bấm nút "Nhập kho"
  ↓
Mở màn hình NhapKhoFrame
  ↓
Frontend gọi GET /api/inventory/imports/lookups để lấy danh sách kho, 
nhà cung cấp, nguyên liệu
  ↓
User chọn kho, nhà cung cấp
  ↓
User tìm kiếm và chọn nguyên liệu từ dropdown
  ↓
User nhập số lượng, đơn giá, số lô, hạn sử dụng
  ↓
Bấm nút "Thêm dòng" - dòng được thêm vào bảng
  ↓
Lặp lại cho các nguyên liệu khác
  ↓
Bấm nút "Lưu phiếu" (Save)
  ↓
Frontend gọi POST /api/inventory/imports
  ↓
Backend tạo phiếu nhập, chi tiết phiếu, lô hàng, cập nhật tồn kho
  ↓
Frontend nhận response, hiển thị thông báo thành công
```

### 3.2 Chi tiết Frontend - Load dữ liệu (NhapKhoFrame.java)

**Bước 1: Mở màn hình nhập kho**
```java
public class NhapKhoFrame extends JFrame {
    private final InventoryApiClient apiClient = new InventoryApiClient();
    
    public NhapKhoFrame() {
        setTitle("Phụng Lộc - Nhập kho");
        // ... UI setup ...
        
        // Bước 2: Load dữ liệu
        loadLookups();
    }
    
    private void loadLookups() {
        new SwingWorker<InventoryLookupDto, Void>() {
            @Override
            protected InventoryLookupDto doInBackground() throws Exception {
                return apiClient.getImportLookups();
            }
            
            @Override
            protected void done() {
                try {
                    InventoryLookupDto data = get();
                    
                    // Populate warehouse combobox
                    for (OptionDto warehouse : data.getWarehouses()) {
                        warehouseCombo.addItem(warehouse);
                    }
                    
                    // Populate supplier combobox
                    for (OptionDto supplier : data.getSuppliers()) {
                        supplierCombo.addItem(supplier);
                    }
                    
                    // Lưu danh sách nguyên liệu
                    allIngredients = data.getIngredients();
                    
                    statusLabel.setText("Tải dữ liệu thành công");
                } catch (Exception e) {
                    statusLabel.setText("Lỗi: " + e.getMessage());
                }
            }
        }.execute();
    }
}
```

**Backend GET /api/inventory/imports/lookups**
```java
@GetMapping("/imports/lookups")
public ResponseEntity<BaseResponse<InventoryLookupResponse>> importLookups(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    
    // Kiểm tra quyền INVENTORY:IMPORT
    authGuard.requirePermission(authHeader, "INVENTORY:IMPORT");
    
    return ResponseEntity.ok(BaseResponse.ok(
            "Lay du lieu nhap kho thanh cong", 
            inventoryService.getImportLookup()
    ));
}

// Service trả về
public InventoryLookupResponse getImportLookup() {
    return new InventoryLookupResponse(
        khoDuLieuRepository.findAllActive(),  // Danh sách kho
        nhaCungCapRepository.findAllActive(), // Danh sách nhà cung cấp
        nguyenLieuRepository.findAllActive()  // Danh sách nguyên liệu
    );
}
```

**Response từ Backend**
```json
{
  "success": true,
  "data": {
    "warehouses": [
      { "value": 1, "label": "Kho tổng" },
      { "value": 2, "label": "Kho chi nhánh Q.1" }
    ],
    "suppliers": [
      { "value": 1, "label": "Cà phê Việt Nam" },
      { "value": 2, "label": "Nguồn cung cấp B" }
    ],
    "ingredients": [
      { "value": 1, "label": "Cà phê Robusta", "unit": "kg" },
      { "value": 2, "label": "Cà phê Arabica", "unit": "kg" }
    ]
  }
}
```

### 3.3 Frontend - User nhập dữ liệu từng dòng

```java
private List<ImportLine> lines = new ArrayList<>();

private void addImportLine() {
    String quantityStr = quantityField.getText().trim();
    String priceStr = priceField.getText().trim();
    String lot = lotField.getText().trim();
    String expiry = expiryField.getText().trim();
    
    OptionDto ingredient = (OptionDto) ingredientCombo.getSelectedItem();
    
    // Validate
    if (ingredient == null || quantityStr.isBlank() 
            || priceStr.isBlank() || lot.isBlank() || expiry.isBlank()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin");
        return;
    }
    
    try {
        BigDecimal quantity = new BigDecimal(quantityStr);
        BigDecimal price = new BigDecimal(priceStr);
        
        // Validate số lượng và giá
        if (quantity.signum() <= 0 || price.signum() <= 0) {
            throw new NumberFormatException();
        }
        
        // Validate ngày hết hạn (format: yyyy-MM-dd)
        LocalDate expiryDate = LocalDate.parse(expiry);
        
        // Tạo dòng nhập
        ImportLine line = new ImportLine(
            ingredient.getValue(),
            ingredient.getLabel(),
            expiryDate,
            quantity.longValue(),
            price.longValue()
        );
        
        // Thêm vào danh sách
        lines.add(line);
        
        // Cập nhật bảng
        updateImportTable();
        
        // Xóa dữ liệu input
        clearQuickInputFields();
        
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Số lượng/giá không hợp lệ");
    } catch (DateTimeParseException ex) {
        JOptionPane.showMessageDialog(this, "Ngày hết hạn phải là yyyy-MM-dd");
    }
}

private void updateImportTable() {
    importTableModel.setRowCount(0);
    BigDecimal total = BigDecimal.ZERO;
    
    for (ImportLine line : lines) {
        BigDecimal lineTotal = BigDecimal.valueOf(line.quantity)
                .multiply(BigDecimal.valueOf(line.price));
        
        importTableModel.addRow(new Object[]{
            line.ingredientId,
            line.ingredientName,
            line.expiryDate,
            numberFormat.format(line.quantity),
            numberFormat.format(line.price),
            numberFormat.format(lineTotal)
        });
        
        total = total.add(lineTotal);
    }
    
    totalLabel.setText(numberFormat.format(total));
}
```

### 3.4 Frontend - Bấm "Lưu phiếu"

```java
private void saveImportReceipt() {
    if (lines.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất một dòng nhập kho");
        return;
    }
    
    OptionDto selectedWarehouse = (OptionDto) warehouseCombo.getSelectedItem();
    OptionDto selectedSupplier = (OptionDto) supplierCombo.getSelectedItem();
    String note = noteArea.getText();
    
    // Tạo request
    CreateImportReceiptRequest request = new CreateImportReceiptRequest();
    request.setMaKho(selectedWarehouse.getValue());
    request.setMaNhaCungCap(selectedSupplier.getValue());
    request.setGhiChu(note);
    
    // Thêm items
    List<CreateImportReceiptItemRequest> items = new ArrayList<>();
    for (ImportLine line : lines) {
        CreateImportReceiptItemRequest item = new CreateImportReceiptItemRequest();
        item.setMaNguyenLieu(line.ingredientId);
        item.setSoLuongNhap(line.quantity);
        item.setDonGiaNhap(line.price);
        item.setSoLo(line.lot);
        item.setHanSuDung(line.expiryDate);
        items.add(item);
    }
    request.setItems(items);
    
    saveButton.setEnabled(false);
    saveButton.setText("Đang lưu...");
    
    new Thread(() -> {
        try {
            // Bước 5: Gọi API
            ImportReceiptDto response = apiClient.createImportReceipt(request);
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    this, 
                    "Lưu phiếu thành công: " + response.getMaPhieuNhap(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    this, 
                    "Lỗi: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
            });
        } finally {
            SwingUtilities.invokeLater(() -> {
                saveButton.setEnabled(true);
                saveButton.setText("Lưu phiếu");
            });
        }
    }).start();
}
```

**Request Body gửi tới backend**
```json
{
  "maKho": 1,
  "maNhaCungCap": 1,
  "ghiChu": "Nhập bổ sung hằng ngày",
  "items": [
    {
      "maNguyenLieu": 1,
      "soLuongNhap": 1000,
      "donGiaNhap": 120000,
      "soLo": "LOT-2024-001",
      "hanSuDung": "2027-12-31"
    },
    {
      "maNguyenLieu": 2,
      "soLuongNhap": 500,
      "donGiaNhap": 150000,
      "soLo": "LOT-2024-002",
      "hanSuDung": "2028-01-15"
    }
  ]
}
```

### 3.5 Chi tiết Backend - Create Import Receipt (InventoryController.java)

```java
@PostMapping("/imports")
public ResponseEntity<BaseResponse<ImportReceiptResponse>> createImportReceipt(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestBody CreateImportReceiptRequest request) {
    
    // Kiểm tra quyền INVENTORY:IMPORT
    SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:IMPORT");
    
    // Gọi service
    ImportReceiptResponse data = inventoryService.createImportReceipt(user, request);
    
    return ResponseEntity.ok(
            BaseResponse.ok("Tao phieu nhap thanh cong", data)
    );
}
```

### 3.6 Chi tiết Backend - Service (InventoryService.java)

```java
@Transactional(rollbackFor = Exception.class)
public ImportReceiptResponse createImportReceipt(
        SessionUser user, 
        CreateImportReceiptRequest request) {
    
    // Bước 6.1: Validate request
    if (request == null || request.getMaKho() == null 
            || request.getMaNhaCungCap() == null 
            || request.getItems().isEmpty()) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu yêu cầu");
    }
    
    // Bước 6.2: Kiểm tra kho tồn tại
    KhoDuLieuRecord warehouse = khoDuLieuRepository.findById(request.getMaKho())
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Kho không tồn tại"));
    
    // Bước 6.3: Kiểm tra nhà cung cấp tồn tại
    NhaCungCapRecord supplier = nhaCungCapRepository.findById(request.getMaNhaCungCap())
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Nhà cung cấp không tồn tại"));
    
    // Bước 6.4: Tạo phiếu nhập (PHIEUNHAP)
    PhieuNhapRecord phieuNhap = new PhieuNhapRecord();
    phieuNhap.setMaKho(warehouse.getMaKho());
    phieuNhap.setMaNhaCungCap(supplier.getMaNhaCungCap());
    phieuNhap.setNgayNhap(LocalDateTime.now());
    phieuNhap.setNguoiTao(user.getMaNguoiDung());
    phieuNhap.setGhiChu(request.getGhiChu());
    phieuNhap.setTrangThai("CONFIRMED");
    phieuNhap = phieuNhapRepository.save(phieuNhap);
    
    long maPhieuNhap = phieuNhap.getMaPhieuNhap();
    
    // Bước 6.5: Duyệt từng item và thêm chi tiết phiếu
    for (CreateImportReceiptItemRequest item : request.getItems()) {
        
        // Kiểm tra nguyên liệu tồn tại
        NguyenLieuRecord ingredient = nguyenLieuRepository.findById(item.getMaNguyenLieu())
                .orElseThrow(() -> new AppException(
                    HttpStatus.NOT_FOUND, 
                    "Nguyên liệu ID " + item.getMaNguyenLieu() + " không tồn tại"
                ));
        
        // Bước 6.5a: Tạo chi tiết phiếu (CHITIETPHIEUNHAP)
        ChiTietPhieuNhapRecord detail = new ChiTietPhieuNhapRecord();
        detail.setMaPhieuNhap(maPhieuNhap);
        detail.setMaNguyenLieu(ingredient.getMaNguyenLieu());
        detail.setSoLuongNhap(item.getSoLuongNhap());
        detail.setDonGiaNhap(item.getDonGiaNhap());
        chiTietPhieuNhapRepository.save(detail);
        
        // Bước 6.5b: Tạo lô hàng (LOHANG_NGUYENLIEU)
        LoHangRecord loHang = new LoHangRecord();
        loHang.setMaKho(warehouse.getMaKho());
        loHang.setMaNguyenLieu(ingredient.getMaNguyenLieu());
        loHang.setSoLo(item.getSoLo());
        loHang.setHanSuDung(item.getHanSuDung());
        loHang.setSoLuongCon(item.getSoLuongNhap()); // Lần đầu = số lượng nhập
        loHang.setNgayTao(LocalDateTime.now());
        loHangRepository.save(loHang);
        
        // Bước 6.5c: Cập nhật tồn kho (TONKHO)
        TonKhoRecord tonKho = tonKhoRepository
                .findByMaKhoAndMaNguyenLieu(
                    warehouse.getMaKho(), 
                    ingredient.getMaNguyenLieu()
                )
                .orElse(new TonKhoRecord());
        
        // Nếu chưa có bản ghi, tạo mới
        if (tonKho.getMaTonKho() == null) {
            tonKho.setMaKho(warehouse.getMaKho());
            tonKho.setMaNguyenLieu(ingredient.getMaNguyenLieu());
            tonKho.setSoLuongTon(0L);
        }
        
        // Cộng thêm số lượng nhập
        tonKho.setSoLuongTon(
            tonKho.getSoLuongTon() + item.getSoLuongNhap()
        );
        tonKho.setNgayCapNhat(LocalDateTime.now());
        tonKhoRepository.save(tonKho);
        
        // Bước 6.5d: Ghi lịch sử kho (NHATKY_KHO)
        NhatKyKhoRecord nhatKy = new NhatKyKhoRecord();
        nhatKy.setMaKho(warehouse.getMaKho());
        nhatKy.setMaNguyenLieu(ingredient.getMaNguyenLieu());
        nhatKy.setLoaiThaoTac("NHAP");
        nhatKy.setSoLuong(item.getSoLuongNhap());
        nhatKy.setGhiChu("Phiếu nhập #" + maPhieuNhap);
        nhatKy.setNgayTao(LocalDateTime.now());
        nhatKy.setNguoiTao(user.getMaNguoiDung());
        nhatKyKhoRepository.save(nhatKy);
    }
    
    // Bước 6.6: Trả response
    return new ImportReceiptResponse(
        maPhieuNhap,
        phieuNhap.getNgayNhap(),
        warehouse.getTenKho(),
        supplier.getTenNhaCungCap(),
        request.getItems().size(),
        "CONFIRMED"
    );
}
```

**Các câu lệnh SQL được thực thi**
```sql
-- 1. INSERT phiếu nhập
INSERT INTO PHIEUNHAP (MAKHO, MANHAACUNGCAP, NGAYNHAP, NGUOITAO, TRANGTHAI) 
VALUES (1, 1, NOW(), 1, 'CONFIRMED')

-- 2. INSERT chi tiết phiếu nhập
INSERT INTO CHITIETPHIEUNHAP (MAPHIEUNHAP, MANGUYENLIEU, SOLUONGNHAP, DONGIANHAP) 
VALUES (100, 1, 1000, 120000)
INSERT INTO CHITIETPHIEUNHAP (MAPHIEUNHAP, MANGUYENLIEU, SOLUONGNHAP, DONGIANHAP) 
VALUES (100, 2, 500, 150000)

-- 3. INSERT lô hàng
INSERT INTO LOHANG_NGUYENLIEU (MAKHO, MANGUYENLIEU, SOLO, HANSUDUNG, SOLUONGCON) 
VALUES (1, 1, 'LOT-2024-001', '2027-12-31', 1000)
INSERT INTO LOHANG_NGUYENLIEU (MAKHO, MANGUYENLIEU, SOLO, HANSUDUNG, SOLUONGCON) 
VALUES (1, 2, 'LOT-2024-002', '2028-01-15', 500)

-- 4. UPDATE tồn kho
UPDATE TONKHO SET SOLUONGTON = SOLUONGTON + 1000 
WHERE MAKHO = 1 AND MANGUYENLIEU = 1

UPDATE TONKHO SET SOLUONGTON = SOLUONGTON + 500 
WHERE MAKHO = 1 AND MANGUYENLIEU = 2

-- 5. INSERT lịch sử kho
INSERT INTO NHATKY_KHO (MAKHO, MANGUYENLIEU, LOAIHAODTAC, SOLUONG, GHICHU, NGAYTAO, NGUOITAO) 
VALUES (1, 1, 'NHAP', 1000, 'Phiếu nhập #100', NOW(), 1)
```

**Response từ Backend**
```json
{
  "success": true,
  "message": "Tao phieu nhap thanh cong",
  "data": {
    "maPhieuNhap": 100,
    "ngayNhap": "2024-05-20T10:30:00",
    "tenKho": "Kho tổng",
    "tenNhaCungCap": "Cà phê Việt Nam",
    "soChiTiet": 2,
    "trangThai": "CONFIRMED"
  }
}
```

---

## 4. CHỨC NĂNG XUẤT KHO (EXPORT WAREHOUSE)

### 4.1 Quy trình tổng quan
```
User bấm nút "Xuất kho"
  ↓
Mở màn hình XuatKhoFrame
  ↓
Frontend gọi GET /api/inventory/exports/lookups để lấy danh sách kho, 
loại xuất, nguyên liệu
  ↓
User chọn kho, loại xuất kho
  ↓
User tìm kiếm và chọn nguyên liệu
  ↓
Frontend gọi GET /api/inventory/exports/lots?maKho=1&maNguyenLieu=1 
để lấy danh sách lô hàng còn tồn (sắp xếp FEFO - hạn sớm nhất trước)
  ↓
User chọn lô hoặc chọn "tự động" (lấy lô sớm nhất)
  ↓
User nhập số lượng xuất
  ↓
Bấm nút "Thêm dòng"
  ↓
Lặp lại cho các nguyên liệu khác
  ↓
Bấm nút "Lưu phiếu"
  ↓
Frontend gọi POST /api/inventory/exports
  ↓
Backend tạo phiếu xuất, cập nhật tồn kho và lô hàng
  ↓
Frontend hiển thị thông báo thành công
```

### 4.2 Chi tiết Frontend - Load dữ liệu (XuatKhoFrame.java)

**Bước 1: Mở màn hình xuất kho**
```java
public class XuatKhoFrame extends JFrame {
    private final InventoryApiClient apiClient = new InventoryApiClient();
    
    public XuatKhoFrame() {
        setTitle("Phụng Lộc - Xuất kho");
        // ... UI setup ...
        
        loadLookups();
    }
    
    private void loadLookups() {
        new SwingWorker<InventoryExportLookupDto, Void>() {
            @Override
            protected InventoryExportLookupDto doInBackground() throws Exception {
                return apiClient.getExportLookups();
            }
            
            @Override
            protected void done() {
                try {
                    InventoryExportLookupDto data = get();
                    
                    // Populate warehouse, export type, ingredients
                    for (OptionDto w : data.getWarehouses()) {
                        warehouseCombo.addItem(w);
                    }
                    
                    for (OptionDto t : data.getExportTypes()) {
                        exportTypeCombo.addItem(t);
                    }
                    
                    allIngredients = data.getIngredients();
                    
                    statusLabel.setText("Tải dữ liệu thành công");
                } catch (Exception e) {
                    statusLabel.setText("Lỗi: " + e.getMessage());
                }
            }
        }.execute();
    }
}
```

### 4.3 Frontend - Chọn nguyên liệu, lấy danh sách lô

```java
private void loadLotsForExport() {
    OptionDto selectedWarehouse = (OptionDto) warehouseCombo.getSelectedItem();
    OptionDto selectedIngredient = (OptionDto) ingredientCombo.getSelectedItem();
    
    if (selectedWarehouse == null || selectedIngredient == null) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn kho và nguyên liệu");
        return;
    }
    
    new SwingWorker<List<InventoryLotResponse>, Void>() {
        @Override
        protected List<InventoryLotResponse> doInBackground() throws Exception {
            // Bước 4: Gọi API lấy danh sách lô
            return apiClient.getLotsForExport(
                selectedWarehouse.getValue(), 
                selectedIngredient.getValue()
            );
        }
        
        @Override
        protected void done() {
            try {
                List<InventoryLotResponse> lots = get();
                
                if (lots.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        XuatKhoFrame.this, 
                        "Không có lô hàng nào còn tồn"
                    );
                    return;
                }
                
                // Populate lot combobox
                // Các lô được sắp xếp FEFO (hạn sớm nhất trước)
                lotCombo.removeAllItems();
                for (InventoryLotResponse lot : lots) {
                    String label = lot.getSoLo() + " (Hạn: " 
                        + lot.getHanSuDung() + ", Tồn: " 
                        + lot.getSoLuongCon() + ")";
                    lotCombo.addItem(new OptionDto(lot.getMaLoHang(), label));
                }
                
                statusLabel.setText("Tải dữ liệu lô thành công");
            } catch (Exception e) {
                statusLabel.setText("Lỗi: " + e.getMessage());
            }
        }
    }.execute();
}
```

**Backend GET /api/inventory/exports/lots?maKho=1&maNguyenLieu=2**
```java
@GetMapping("/exports/lots")
public ResponseEntity<BaseResponse<List<InventoryLotResponse>>> exportLots(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam Long maKho,
        @RequestParam Long maNguyenLieu) {
    
    SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:EXPORT");
    
    return ResponseEntity.ok(BaseResponse.ok(
            "Lay danh sach lo xuat kho thanh cong",
            inventoryService.getLotsForExport(maKho, maNguyenLieu, user)
    ));
}

// Service method - FEFO (First Expired, First Out)
public List<InventoryLotResponse> getLotsForExport(
        Long maKho, Long maNguyenLieu, SessionUser user) {
    
    // Truy vấn các lô có số lượng còn > 0
    // Sắp xếp theo FEFO: hạn sủ dụng sớm nhất trước
    return loHangRepository.findByMaKhoAndMaNguyenLieuOrderByHanSuDungAsc(
        maKho, 
        maNguyenLieu
    )
    .stream()
    .filter(lo -> lo.getSoLuongCon() > 0)
    .map(lo -> new InventoryLotResponse(
        lo.getMaLoHang(),
        lo.getSoLo(),
        lo.getHanSuDung(),
        lo.getSoLuongCon()
    ))
    .collect(Collectors.toList());
}
```

**Response - Danh sách lô sắp xếp theo FEFO**
```json
{
  "success": true,
  "data": [
    {
      "maLoHang": 50,
      "soLo": "LOT-2024-001",
      "hanSuDung": "2025-06-15",
      "soLuongCon": 500
    },
    {
      "maLoHang": 51,
      "soLo": "LOT-2024-002",
      "hanSuDung": "2025-08-20",
      "soLuongCon": 800
    }
  ]
}
```

### 4.4 Frontend - User nhập số lượng xuất

```java
private void addExportLine() {
    OptionDto selectedWarehouse = (OptionDto) warehouseCombo.getSelectedItem();
    OptionDto selectedIngredient = (OptionDto) ingredientCombo.getSelectedItem();
    OptionDto selectedLot = (OptionDto) lotCombo.getSelectedItem();
    OptionDto selectedExportType = (OptionDto) exportTypeCombo.getSelectedItem();
    String quantityStr = quantityField.getText().trim();
    
    // Validate
    if (selectedWarehouse == null || selectedIngredient == null 
            || selectedLot == null || selectedExportType == null 
            || quantityStr.isBlank()) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn đủ thông tin");
        return;
    }
    
    try {
        long quantity = Long.parseLong(quantityStr);
        
        if (quantity <= 0) {
            throw new NumberFormatException();
        }
        
        // Tạo dòng xuất
        ExportLine line = new ExportLine(
            selectedWarehouse.getValue(),
            selectedIngredient.getValue(),
            selectedLot.getValue(),
            selectedExportType.getValue(),
            quantity
        );
        
        lines.add(line);
        updateExportTable();
        clearQuickInputFields();
        
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên dương");
    }
}

private void updateExportTable() {
    exportTableModel.setRowCount(0);
    
    for (ExportLine line : lines) {
        exportTableModel.addRow(new Object[]{
            line.ingredientId,
            line.ingredientName,
            line.lotNumber,
            line.exportType,
            line.quantity
        });
    }
}
```

### 4.5 Frontend - Bấm "Lưu phiếu"

```java
private void saveExportReceipt() {
    if (lines.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất một dòng");
        return;
    }
    
    OptionDto selectedWarehouse = (OptionDto) warehouseCombo.getSelectedItem();
    String note = noteArea.getText();
    
    // Tạo request
    CreateExportReceiptRequest request = new CreateExportReceiptRequest();
    request.setMaKho(selectedWarehouse.getValue());
    request.setGhiChu(note);
    
    List<CreateExportReceiptItemRequest> items = new ArrayList<>();
    for (ExportLine line : lines) {
        CreateExportReceiptItemRequest item = new CreateExportReceiptItemRequest();
        item.setMaLoHang(line.lotId);
        item.setMaNguyenLieu(line.ingredientId);
        item.setSoLuongXuat(line.quantity);
        item.setLoaiXuat(line.exportType);
        items.add(item);
    }
    request.setItems(items);
    
    saveButton.setEnabled(false);
    saveButton.setText("Đang lưu...");
    
    new Thread(() -> {
        try {
            // Bước 5: Gọi API
            ExportReceiptDto response = apiClient.createExportReceipt(request);
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    this, 
                    "Lưu phiếu xuất thành công: " + response.getMaPhieuXuat(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            });
        } finally {
            SwingUtilities.invokeLater(() -> {
                saveButton.setEnabled(true);
                saveButton.setText("Lưu phiếu");
            });
        }
    }).start();
}
```

**Request Body gửi tới backend**
```json
{
  "maKho": 1,
  "ghiChu": "Xuất cho chi nhánh Q.3",
  "items": [
    {
      "maLoHang": 50,
      "maNguyenLieu": 1,
      "soLuongXuat": 300,
      "loaiXuat": "SALE"
    },
    {
      "maLoHang": 51,
      "maNguyenLieu": 2,
      "soLuongXuat": 200,
      "loaiXuat": "TRANSFER"
    }
  ]
}
```

### 4.6 Chi tiết Backend - Create Export Receipt

```java
@PostMapping("/exports")
public ResponseEntity<BaseResponse<ExportReceiptResponse>> createExportReceipt(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestBody CreateExportReceiptRequest request) {
    
    SessionUser user = authGuard.requirePermission(authHeader, "INVENTORY:EXPORT");
    
    ExportReceiptResponse data = inventoryService.createExportReceipt(user, request);
    
    return ResponseEntity.ok(
            BaseResponse.ok("Tao phieu xuat thanh cong", data)
    );
}

// Service method
@Transactional(rollbackFor = Exception.class)
public ExportReceiptResponse createExportReceipt(
        SessionUser user, 
        CreateExportReceiptRequest request) {
    
    // Validate request
    if (request == null || request.getMaKho() == null || request.getItems().isEmpty()) {
        throw new AppException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu yêu cầu");
    }
    
    // Kiểm tra kho tồn tại
    KhoDuLieuRecord warehouse = khoDuLieuRepository.findById(request.getMaKho())
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Kho không tồn tại"));
    
    // Tạo phiếu xuất
    PhieuXuatRecord phieuXuat = new PhieuXuatRecord();
    phieuXuat.setMaKho(warehouse.getMaKho());
    phieuXuat.setNgayXuat(LocalDateTime.now());
    phieuXuat.setNguoiTao(user.getMaNguoiDung());
    phieuXuat.setGhiChu(request.getGhiChu());
    phieuXuat.setTrangThai("CONFIRMED");
    phieuXuat = phieuXuatRepository.save(phieuXuat);
    
    long maPhieuXuat = phieuXuat.getMaPhieuXuat();
    
    // Duyệt từng item
    for (CreateExportReceiptItemRequest item : request.getItems()) {
        
        // Lấy lô hàng
        LoHangRecord loHang = loHangRepository.findById(item.getMaLoHang())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Lô hàng không tồn tại"));
        
        // Kiểm tra số lượng có đủ không
        if (loHang.getSoLuongCon() < item.getSoLuongXuat()) {
            throw new AppException(
                HttpStatus.BAD_REQUEST, 
                "Lô " + loHang.getSoLo() + " không đủ số lượng"
            );
        }
        
        // Tạo chi tiết phiếu xuất
        ChiTietPhieuXuatRecord detail = new ChiTietPhieuXuatRecord();
        detail.setMaPhieuXuat(maPhieuXuat);
        detail.setMaLoHang(loHang.getMaLoHang());
        detail.setMaNguyenLieu(item.getMaNguyenLieu());
        detail.setSoLuongXuat(item.getSoLuongXuat());
        detail.setLoaiXuat(item.getLoaiXuat());
        chiTietPhieuXuatRepository.save(detail);
        
        // Cập nhật số lượng lô hàng
        loHang.setSoLuongCon(
            loHang.getSoLuongCon() - item.getSoLuongXuat()
        );
        loHangRepository.save(loHang);
        
        // Cập nhật tồn kho
        TonKhoRecord tonKho = tonKhoRepository
                .findByMaKhoAndMaNguyenLieu(warehouse.getMaKho(), item.getMaNguyenLieu())
                .orElseThrow(() -> new AppException(
                    HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Tồn kho không tồn tại"
                ));
        
        tonKho.setSoLuongTon(tonKho.getSoLuongTon() - item.getSoLuongXuat());
        tonKho.setNgayCapNhat(LocalDateTime.now());
        tonKhoRepository.save(tonKho);
        
        // Ghi lịch sử kho
        NhatKyKhoRecord nhatKy = new NhatKyKhoRecord();
        nhatKy.setMaKho(warehouse.getMaKho());
        nhatKy.setMaNguyenLieu(item.getMaNguyenLieu());
        nhatKy.setLoaiThaoTac("XUAT_" + item.getLoaiXuat());
        nhatKy.setSoLuong(item.getSoLuongXuat());
        nhatKy.setGhiChu("Phiếu xuất #" + maPhieuXuat);
        nhatKy.setNgayTao(LocalDateTime.now());
        nhatKy.setNguoiTao(user.getMaNguoiDung());
        nhatKyKhoRepository.save(nhatKy);
    }
    
    return new ExportReceiptResponse(maPhieuXuat, "CONFIRMED");
}
```

---

## 5. CHỨC NĂNG QUẢN LÝ NGUYÊN LIỆU (MANAGE INGREDIENTS)

### 5.1 Quy trình tổng quan
```
User bấm vào chức năng "Quản lý nguyên liệu"
  ↓
Mở màn hình QuanLyNguyenLieuFrame
  ↓
Frontend gọi GET /api/ingredients để lấy danh sách nguyên liệu
  ↓
Frontend gọi GET /api/ingredients/lookups để lấy danh sách đơn vị tính
  ↓
Hiển thị bảng danh sách nguyên liệu
  ↓
User có thể:
  - Tìm kiếm: nhập từ khóa trong search box, bấm tìm
    Frontend gọi GET /api/ingredients?keyword=...
  
  - Lọc theo trạng thái: Active/Inactive
    Frontend gọi GET /api/ingredients?status=...
  
  - Tạo nguyên liệu mới: bấm nút "Thêm"
    Mở dialog/frame nhập dữ liệu
    Bấm "Save" -> Frontend gọi POST /api/ingredients
  
  - Cập nhật nguyên liệu: click vào dòng, bấm "Sửa"
    Mở dialog/frame với dữ liệu hiện tại
    Nhập lại dữ liệu
    Bấm "Save" -> Frontend gọi PUT /api/ingredients/{id}
  
  - Xóa/Deactivate: click vào dòng, bấm "Xóa"
    Frontend gọi PUT /api/ingredients/{id}/status
```

---

## 6. CHỨC NĂNG QUẢN LÝ CHI NHÁNH (MANAGE BRANCHES)

### 6.1 Quy trình tổng quan
```
User bấm vào "Quản lý chi nhánh"
  ↓
Mở màn hình QuanLyChiNhanhFrame
  ↓
Frontend gọi GET /api/branches để lấy danh sách chi nhánh
  ↓
Hiển thị bảng danh sách
  ↓
User có thể:
  - Tạo chi nhánh: bấm "Thêm"
    Frontend gọi POST /api/branches
  
  - Cập nhật chi nhánh: click dòng, bấm "Sửa"
    Frontend gọi PUT /api/branches/{id}
  
  - Deactivate: bấm "Xóa"
    Frontend gọi PUT /api/branches/{id}/status
```

---

## 7. CHỨC NĂNG QUẢN LÝ QUY TẮC & PHÂN QUYỀN (RBAC - ROLE BASED ACCESS CONTROL)

### 7.1 Quy trình tổng quan
```
User bấm vào "Phân quyền bảo mật"
  ↓
Mở màn hình PhanQuyenBaoMatFrame
  ↓
Frontend gọi GET /api/rbac/roles để lấy danh sách role
  ↓
Frontend gọi GET /api/rbac/permissions để lấy danh sách permission
  ↓
Hiển thị bảng role, và checkboxes permission
  ↓
User chọn một role
  ↓
Frontend gọi GET /api/rbac/roles/{roleId}/permissions để lấy quyền hiện tại
  ↓
Hiển thị các checkbox đã ticked/unticked
  ↓
User tick/untick checkboxes để thay đổi quyền
  ↓
Bấm "Lưu"
  ↓
Frontend gọi PUT /api/rbac/roles/{roleId}/permissions
  ↓
Backend cập nhật bảng GIAM_VAI_TRO_PHANQUYEN
```

---

## 8. CHỨC NĂNG THEO DÕI HẠN SỬ DỤNG (TRACK EXPIRY)

### 8.1 Quy trình tổng quan
```
User bấm vào "Theo dõi hạn sử dụng"
  ↓
Mở màn hình TheoDoiHanSuDungFrame
  ↓
Frontend gọi GET /api/expiry/lookups để lấy danh sách kho
  ↓
User chọn kho (hoặc mặc định là tất cả)
  ↓
Frontend gọi GET /api/expiry?maKho=1&daysUntilExpiry=30
    Để lấy những lô hàng hết hạn trong vòng 30 ngày tới
  ↓
Hiển thị bảng:
  - Nguyên liệu
  - Số lô
  - Hạn sử dụng
  - Số lượng còn
  - Số ngày còn lại (tính từ hôm nay)
  ↓
Bấn các lô hàng hết hạn sớm nhất ở trên cùng (red color)
```

---

## 9. CHỨC NĂNG XEM TỒN KHO (VIEW STOCK)

### 9.1 Quy trình tổng quan
```
User bấm vào "Xem tồn kho"
  ↓
Mở màn hình XemTonKhoFrame
  ↓
Frontend gọi GET /api/inventory/stock?maKho=all
  ↓
Hiển thị tổng quan tồn kho:
  Bảng với các cột:
  - Mã nguyên liệu
  - Tên nguyên liệu
  - Đơn vị tính
  - Tồn kho
  - Giá trị tồn kho (tồn * giá)
  ↓
User có thể:
  - Lọc theo kho: chọn combobox kho
  - Tìm kiếm nguyên liệu
  ↓
Click vào dòng để xem chi tiết các lô
  ↓
Frontend gọi GET /api/inventory/lots?maKho=1&maNguyenLieu=1
  ↓
Hiển thị popup/dialog với danh sách lô:
  - Số lô
  - Hạn sử dụng
  - Số lượng còn
```

---

## 10. CHỨC NĂNG KIỂM KHO (STOCKTAKE)

### 10.1 Quy trình tổng quan
```
User bấm vào "Kiểm kho"
  ↓
Mở màn hình KiemKhoFrame
  ↓
Frontend gọi GET /api/stocktake/lookups để lấy danh sách kho
  ↓
User chọn kho cần kiểm
  ↓
Bấm "Bắt đầu kiểm"
  ↓
Frontend gọi POST /api/stocktake với trạng thái DRAFT
  ↓
Backend tạo record KIEMKHO với trạng thái DRAFT
  ↓
Frontend hiển thị bảng danh sách nguyên liệu trong kho
  ↓
User nhập số lượng thực tế (physical count) cho từng nguyên liệu
  ↓
Hệ thống tự tính:
  - Số lượng theo hệ thống (từ database)
  - Chênh lệch = thực tế - theo hệ thống
  - Nếu chênh lệch > 0: hụt
  - Nếu chênh lệch < 0: thừa
  ↓
Bấm "Lưu phiếu kiểm"
  ↓
Frontend gọi PUT /api/stocktake/{id} với trạng thái CONFIRMED
  ↓
Backend:
  1. Tạo chi tiết kiểm kho (CHITIET_KIEMKHO)
  2. Nếu có chênh lệch, tạo record WASTAGE (hụt)
  3. Cập nhật tồn kho nếu có chênh lệch
```

---

## 11. CHỨC NĂNG BÁO CÁO HÀO HỤT (WASTAGE REPORT)

### 11.1 Quy trình tổng quan
```
User bấm vào "Báo cáo hào hụt"
  ↓
Mở màn hình BaoCaoHaoHutFrame
  ↓
Frontend gọi GET /api/wastage để lấy danh sách hào hụt
  ↓
Frontend gọi GET /api/wastage/lookups để lấy danh sách kho
  ↓
User có thể lọc:
  - Theo kho: chọn combobox
  - Theo ngày: từ ngày - đến ngày
  - Theo nguyên liệu
  ↓
Frontend gọi GET /api/wastage?maKho=1&fromDate=2024-05-01&toDate=2024-05-20
  ↓
Hiển thị bảng hào hụt:
  - Nguyên liệu
  - Kho
  - Số lượng hụt
  - Lý do
  - Ngày phát hiện
  ↓
Hiển thị tổng cộng hao hụt
```

---

## 12. CHỨC NĂNG LỊCH SỬ KHO (WAREHOUSE HISTORY)

### 12.1 Quy trình tổng quan
```
User bấm vào "Tra cứu lịch sử kho"
  ↓
Mở màn hình TraCuuLichSuKhoFrame
  ↓
Frontend gọi GET /api/inventory-history/lookups để lấy danh sách kho
  ↓
User chọn kho (hoặc tất cả), chọn ngày từ - đến
  ↓
Frontend gọi GET /api/inventory-history?maKho=1&fromDate=2024-05-01&toDate=2024-05-20
  ↓
Hiển thị bảng lịch sử:
  - Ngày tạo
  - Loại thao tác (NHAP, XUAT_SALE, XUAT_TRANSFER...)
  - Nguyên liệu
  - Số lượng
  - Người thao tác
  - Ghi chú
  ↓
User có thể click vào dòng để xem chi tiết
```

---

## 13. CHỨC NĂNG ĐIỀU CHUYỂN KHO (WAREHOUSE TRANSFER)

### 13.1 Quy trình tổng quan
```
User bấm vào "Điều chuyển kho"
  ↓
Mở màn hình DieuChuyenKhoFrame
  ↓
Frontend gọi GET /api/inventory/transfers/lookups 
để lấy danh sách kho nguồn và kho đích
  ↓
User chọn kho nguồn
  ↓
Frontend gọi GET /api/inventory/transfers/lots?maKhoNguon=1
để lấy danh sách lô hàng có tồn
  ↓
User chọn kho đích
  ↓
User chọn lô hàng và nhập số lượng điều chuyển
  ↓
Lặp lại cho các lô khác
  ↓
Bấm "Lưu phiếu"
  ↓
Frontend gọi POST /api/inventory/transfers
  ↓
Backend:
  1. Tạo phiếu điều chuyển
  2. Giảm tồn kho ở kho nguồn
  3. Tăng tồn kho ở kho đích
  4. Cập nhật lô hàng (thêm lô vào kho đích hoặc tạo lô mới)
  5. Ghi lịch sử
```

---

## 14. CHỨC NĂNG QUẢN LÝ NHÀ CUNG CẤP (MANAGE SUPPLIERS)

### 14.1 Quy trình tổng quan
```
User bấm vào "Quản lý nhà cung cấp"
  ↓
Frontend gọi GET /api/suppliers để lấy danh sách
  ↓
Hiển thị bảng:
  - Tên nhà cung cấp
  - Địa chỉ
  - Điện thoại
  - Email
  - Trạng thái
  ↓
User có thể:
  - Tạo: POST /api/suppliers
  - Cập nhật: PUT /api/suppliers/{id}
  - Xóa: PUT /api/suppliers/{id}/status
```

---

## 15. CHỨC NĂNG QUẢN LÝ ĐƠN VỊ TÍNH (MANAGE UNITS)

### 15.1 Quy trình tổng quan
```
User bấm vào "Quản lý đơn vị tính"
  ↓
Frontend gọi GET /api/units để lấy danh sách
  ↓
Hiển thị bảng:
  - Mã đơn vị
  - Tên đơn vị (kg, lít, cái, hộp...)
  - Trạng thái
  ↓
User có thể:
  - Tạo: POST /api/units
  - Cập nhật: PUT /api/units/{id}
  - Xóa: PUT /api/units/{id}/status
```

---

## 16. CHỨC NĂNG QUẢN LÝ KHO (MANAGE WAREHOUSES)

### 16.1 Quy trình tổng quan
```
User bấm vào "Quản lý kho"
  ↓
Frontend gọi GET /api/warehouses để lấy danh sách
  ↓
Hiển thị bảng:
  - Mã kho
  - Tên kho
  - Địa chỉ
  - Người quản lý
  - Trạng thái
  ↓
User có thể:
  - Tạo: POST /api/warehouses
  - Cập nhật: PUT /api/warehouses/{id}
  - Xóa: PUT /api/warehouses/{id}/status
```

---

## 17. CHỨC NĂNG QUẢN LÝ POS (MANAGE POS)

### 17.1 Quy trình tổng quan
```
User bấm vào "Quản lý POS"
  ↓
Frontend gọi GET /api/pos để lấy danh sách POS
  ↓
Hiển thị bảng:
  - Mã POS
  - Tên POS
  - Chi nhánh
  - Địa chỉ
  - Trạng thái
  ↓
User có thể:
  - Tạo: POST /api/pos
  - Cập nhật: PUT /api/pos/{id}
  - Xóa: PUT /api/pos/{id}/status
```

---

## 18. CHỨC NĂNG QUẢN TRỊ HỆ THỐNG (SYSTEM ADMIN)

### 18.1 Quy trình tổng quan
```
User bấm vào "Quản trị hệ thống"
  ↓
Mở màn hình QuanTriHeThongFrame
  ↓
Hiển thị các chức năng admin:
  - Khôi phục database
  - Xuất/nhập dữ liệu
  - Xem log hệ thống
  - Cấu hình hệ thống
  - Quản lý phiên đăng nhập
  ↓
Phụ thuộc vào quyền của user
```

---

## 19. CHỨC NĂNG QUÊN MẬT KHẨU (FORGOT PASSWORD)

### 19.1 Quy trình tổng quan
```
Trên màn hình login, user bấm "Quên mật khẩu"
  ↓
Mở màn hình ForgotPasswordFrame
  ↓
User nhập email
  ↓
Bấm "Gửi mã"
  ↓
Frontend gọi POST /api/password-reset/forgot-password
  ↓
Backend:
  1. Tìm user theo email
  2. Tạo reset code (6 chữ số hoặc 32 ký tự)
  3. Gửi email chứa reset code
  4. Lưu reset code vào database (với expiration time)
  ↓
Frontend hiển thị input để user nhập reset code
  ↓
User nhập mã nhận được từ email
  ↓
Frontend gọi POST /api/password-reset/verify-code
  ↓
Backend xác thực mã, nếu đúng -> cho user nhập mật khẩu mới
  ↓
User nhập mật khẩu mới
  ↓
Frontend gọi POST /api/password-reset/reset-password
  ↓
Backend:
  1. Xác thực reset code một lần nữa
  2. Hash mật khẩu mới
  3. Cập nhật mật khẩu trong database
  4. Xóa reset code
  ↓
Frontend hiển thị "Đặt lại mật khẩu thành công"
  ↓
Quay lại màn hình login
```

---

## 20. KIẾN TRÚC BẢO MẬT & XÁC THỰC

### 20.1 Token-based Authentication
```
1. User login -> Backend tạo token (JWT hoặc UUID)
2. Frontend lưu token
3. Mỗi request gửi header:
   Authorization: Bearer <token>
4. Backend xác thực token, lấy SessionUser
5. Kiểm tra quyền (authorization guard)
6. Thực thi business logic
```

### 20.2 Permission Checking
```
1. Mỗi API endpoint được ghi chú với quyền cần thiết
   Ví dụ: @AuthGuard(required = "INVENTORY:IMPORT")
   
2. Khi user gọi API, backend kiểm tra:
   - Token có hợp lệ không
   - User có quyền này không
   
3. Quyền được lấy từ database dựa trên vai trò (role) của user
   ADMIN -> tất cả quyền
   THU_NGAN -> chỉ có quyền bán hàng
   QUAN_LY_CHI_NHANH -> quyền quản lý chi nhánh của mình
```

### 20.3 Session Management
```
1. Token được lưu trong TokenStore (in-memory hoặc Redis)
2. Mỗi token có expiration time (mặc định 2 giờ)
3. Khi token hết hạn:
   - Backend trả HTTP 401 Unauthorized
   - Frontend clear session và quay về màn hình login
4. Frontend có thể gọi /api/auth/me để kiểm tra session còn hợp lệ
```

---

## 21. FLOW CỦA MỐI LẦN GỌI API

```
┌─────────────────────────────────────────────────────────────────┐
│                          FRONTEND                                │
│  1. Tạo HttpRequest với header Authorization: Bearer <token>    │
│  2. Gửi request tới backend                                     │
│  3. Nhận response, parse JSON                                   │
│  4. Cập nhật UI (table, message label...)                       │
└────────────────┬────────────────────────────────────────────────┘
                 │ HTTP/JSON
                 ↓
┌─────────────────────────────────────────────────────────────────┐
│                           BACKEND                                │
│                                                                  │
│  1. Spring DispatcherServlet nhận request                       │
│  2. Router tìm đúng Controller method                           │
│  3. Extract token từ header                                     │
│  4. AuthGuard xác thực token -> lấy SessionUser                 │
│  5. AuthGuard kiểm tra permission                               │
│  6. Gọi Service để xử lý business logic                         │
│  7. Service tương tác với Database (Repository)                 │
│  8. Return response object                                      │
│  9. Controller wrap response vào BaseResponse                   │
│  10. Return HTTP response với JSON body                         │
│                                                                  │
│  Exception handling:                                            │
│  - Nếu token invalid -> HTTP 401                                │
│  - Nếu không có quyền -> HTTP 403                               │
│  - Nếu dữ liệu invalid -> HTTP 400                              │
│  - Nếu không tìm thấy -> HTTP 404                               │
│  - Nếu lỗi server -> HTTP 500                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 22. RESPONSE FORMAT

### Success Response
```json
{
  "success": true,
  "message": "Thực hiện thành công",
  "data": {
    // Dữ liệu trả về tuỳ từng API
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Lỗi xác thực: Tài khoản không tồn tại",
  "data": null
}
```

### Error Response với chi tiết lỗi validation
```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "errors": [
    {
      "field": "tenDangNhap",
      "message": "Tên đăng nhập không được để trống"
    },
    {
      "field": "matKhau",
      "message": "Mật khẩu phải ít nhất 6 ký tự"
    }
  ]
}
```

---

## KẾT LUẬN

Toàn bộ hệ thống tuân theo mô hình:
1. **Frontend** (Java Swing) - Giao diện người dùng
2. **API Client** - Gửi HTTP request tới backend
3. **Backend** (Spring Boot) - Xử lý business logic, kiểm tra quyền
4. **Database** - Lưu trữ dữ liệu

Mỗi chức năng đều thực hiện các bước:
- Load dữ liệu (dropdowns, lookups)
- User nhập/chọn dữ liệu
- Validate dữ liệu ở frontend
- Gửi request tới backend
- Backend validate, kiểm tra quyền
- Backend xử lý business logic
- Backend trả response
- Frontend cập nhật UI

---

**Tài liệu này được tạo ngày:** 20/05/2024
