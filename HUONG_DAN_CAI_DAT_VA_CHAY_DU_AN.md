# Huong dan cai dat va chay du an Coffee Chain Management

File nay huong dan 2 cach chay du an:

1. Chay localhost: may tu chay Oracle Database + backend bang Docker Compose, frontend goi `http://localhost:8080`.
2. Chay online: may chi chay frontend, backend + database da chay san tren server khac, frontend goi URL server/ngrok/domain.

Du an gom:

- `coffee-chain-management-backend`: Spring Boot backend.
- `coffee-chain-management-frontend`: Java Swing frontend.
- `docker-compose.yml`: chay Oracle + backend.
- `.env.example`: file mau bien moi truong.
- `quan_ly_chuoi_cafe_phung_loc.sql`: schema database.
- `seed.sql`: du lieu mau, quyen, role.

---

## 1. Yeu cau chung

Truoc khi chay, may can co:

- Git de clone code.
- Docker Desktop neu chay localhost full backend + database.
- JDK 17 va Maven neu muon chay frontend bang source code.

Neu chi nhan file `.exe` frontend da dong goi thi khong can cai JDK/Maven, chi can mo file exe va dam bao `BASE_URL` da tro dung backend.

---

## 2. Clone code

Mo terminal/cmd/powershell va chay:

```bash
git clone <link-github-du-an>
cd coffee-chain-management
```

Thay `<link-github-du-an>` bang link GitHub that cua nhom.

---

# Cach 1: Chay localhost full backend + database

Cach nay danh cho nguoi muon chay toan bo he thong tren may minh:

```text
Frontend Java Swing
  -> http://localhost:8080
  -> Backend Spring Boot trong Docker
  -> Oracle Database trong Docker
```

## Buoc 1: Tao file `.env`

Tai thu muc goc project, copy file mau:

```bash
cp .env.example .env
```

Neu dung Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Mo file `.env` va sua cac gia tri can thiet.

Toi thieu can sua:

```env
ORACLE_PASSWORD=mat_khau_sys_oracle
APP_DB_USERNAME=CAFE_APP
APP_DB_PASSWORD=mat_khau_cafe_app

DEFAULT_ADMIN_USERNAME=admin
DEFAULT_ADMIN_PASSWORD=mat_khau_admin
DEFAULT_ADMIN_EMAIL=admin@phungloc.local
```

Neu chua test mail/payos/cloudinary thi co the de tam gia tri mau, nhung cac chuc nang lien quan mail/thanh toan/upload anh co the khong chay that.

## Buoc 2: Chay Docker Compose

Tai thu muc goc project:

```bash
docker compose up --build
```

Lan dau se mat kha lau vi Docker can tai Oracle image va build backend.

Sau khi chay thanh cong:

- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Oracle: `localhost:1521/XEPDB1`

Neu muon chay nen:

```bash
docker compose up --build -d
```

Xem log backend:

```bash
docker compose logs -f backend
```

Xem log Oracle:

```bash
docker compose logs -f oracle-db
```

## Buoc 3: Doi frontend ve localhost

Mo file:

```text
coffee-chain-management-frontend/src/main/java/com/coffeechain/config/ApiConfig.java
```

Sua:

```java
public static final String BASE_URL = "http://localhost:8080";
```

Neu file dang tro ngrok/domain thi thay ve localhost.

## Buoc 4: Chay frontend tu source

Vao thu muc frontend:

```bash
cd coffee-chain-management-frontend
mvn exec:java
```

Hoac chay bang file batch neu co:

```bash
run-frontend.bat
```

Dang nhap bang tai khoan admin trong `.env`:

```text
Username: DEFAULT_ADMIN_USERNAME
Password: DEFAULT_ADMIN_PASSWORD
```

## Buoc 5: Reset database neu can

Neu muon xoa sach database va nap lai schema + seed tu dau:

```bash
docker compose down -v
```

Sau do chay lai:

```bash
docker compose up --build
```

Luu y: lenh `down -v` se xoa volume Oracle, tuc la mat toan bo du lieu da tao trong database local.

---

# Cach 2: Chay online, chi chay frontend

Cach nay danh cho thanh vien trong nhom khong can cai Oracle/database/backend tren may. Backend va database da chay san tren server cua mot ban trong nhom.

Mo hinh:

```text
Frontend Java Swing tren may thanh vien
  -> URL backend online/ngrok/domain
  -> Backend Spring Boot tren server
  -> Oracle Database tren server
```

Vi du backend online:

```text
https://abc-xyz.ngrok-free.app
```

Hoac domain/server rieng:

```text
https://api-phungloc.example.com
```

## Buoc 1: Clone code

```bash
git clone <link-github-du-an>
cd coffee-chain-management
```

## Buoc 2: Doi BASE_URL ve backend online

Mo file:

```text
coffee-chain-management-frontend/src/main/java/com/coffeechain/config/ApiConfig.java
```

Sua:

```java
public static final String BASE_URL = "https://abc-xyz.ngrok-free.app";
```

Thay URL tren bang URL backend that ma nhom dang dung.

Khong them dau `/` o cuoi URL. Dung:

```java
"https://abc-xyz.ngrok-free.app"
```

Khong nen dung:

```java
"https://abc-xyz.ngrok-free.app/"
```

## Buoc 3: Chay frontend

Vao thu muc frontend:

```bash
cd coffee-chain-management-frontend
mvn exec:java
```

Dang nhap bang tai khoan da duoc tao tren server.

## Buoc 4: Neu dung file frontend da dong goi exe

Neu da co folder app da dong goi bang `jpackage`, can gui nguyen folder, khong gui rieng file `.exe`.

Folder dung se co dang:

```text
PhungLocCoffee/
  PhungLocCoffee.exe
  app/
  runtime/
```

Mo:

```text
PhungLocCoffee.exe
```

Khong duoc chi copy moi file `.exe`, vi app can thu muc `app` va `runtime` di kem.

---

## 3. Cach kiem tra backend dang chay

Mo trinh duyet:

```text
http://localhost:8080/swagger-ui/index.html
```

Neu chay online thi mo:

```text
<BASE_URL>/swagger-ui/index.html
```

Vi du:

```text
https://abc-xyz.ngrok-free.app/swagger-ui/index.html
```

Neu Swagger mo duoc la backend da online.

---

## 4. Cau hinh cho payOS, mail, Cloudinary

Cac dich vu nay nam o backend, cau hinh trong file `.env` cua may/server chay backend.

### Mail quen mat khau

Can cac bien:

```env
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
MAIL_FROM=your_email@gmail.com
```

Neu khong co Gmail App Password thi chuc nang gui mail that co the loi.

### payOS

Can cac bien:

```env
PAYOS_CLIENT_ID=...
PAYOS_API_KEY=...
PAYOS_CHECKSUM_KEY=...
PAYOS_RETURN_URL=...
PAYOS_CANCEL_URL=...
```

Neu test webhook local thi backend can public bang ngrok/domain.

### Cloudinary

Can cac bien:

```env
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
CLOUDINARY_FOLDER=coffee-chain/products
```

Neu thieu Cloudinary secret thi upload hinh anh san pham co the khong chay.

---

## 5. Loi thuong gap

### 5.1. Frontend bao khong ket noi duoc server

Kiem tra:

- Backend co dang chay khong.
- `ApiConfig.BASE_URL` co dung khong.
- Neu dung ngrok, link ngrok co con song khong.
- Khong them dau `/` cuoi BASE_URL.

### 5.2. Dang nhap bao het han phien hoac chua dang nhap

Thu dang xuat va dang nhap lai.

Neu chi mot so man hinh bi loi, kiem tra man hinh do co con hard-code URL cu khong. Tat ca API nen di qua `ApiConfig.BASE_URL`.

### 5.3. Docker Oracle chay rat lau lan dau

Binh thuong. Oracle XE lan dau can thoi gian de khoi tao database, tao user, chay schema va seed.

Xem log:

```bash
docker compose logs -f oracle-db
```

### 5.4. Sua `seed.sql` nhung du lieu khong doi

Do script init chi chay khi volume Oracle chua ton tai. Can reset database:

```bash
docker compose down -v
docker compose up --build
```

### 5.5. May khac mo exe bi loi giao dien

Can gui nguyen folder app da dong goi, khong gui rieng `.exe`.

Neu bi loi scale giao dien:

1. Right click `PhungLocCoffee.exe`.
2. Properties.
3. Compatibility.
4. Change high DPI settings.
5. Tick `Override high DPI scaling behavior`.
6. Chon `Application`.
7. Apply va mo lai.

---

## 6. Tom tat nhanh

### Chay localhost

```bash
git clone <link-github-du-an>
cd coffee-chain-management
cp .env.example .env
# sua .env
docker compose up --build
# sua ApiConfig.BASE_URL = "http://localhost:8080"
cd coffee-chain-management-frontend
mvn exec:java
```

### Chay online chi frontend

```bash
git clone <link-github-du-an>
cd coffee-chain-management
# sua ApiConfig.BASE_URL = "https://link-backend-online"
cd coffee-chain-management-frontend
mvn exec:java
```

Neu dung file exe thi chi can mo `PhungLocCoffee.exe`, nhung phai gui nguyen folder app da dong goi.
