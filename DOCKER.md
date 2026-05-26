# Chay database + backend bang Docker Compose

Yeu cau may da cai Docker Desktop hoac Docker Engine.

## Chuan bi `.env`

Copy file mau:

```bash
cp .env.example .env
```

Sau do mo `.env` va dien secret that cua ban: mat khau Oracle, Gmail app password, payOS, Cloudinary.

## Chay lan dau

```bash
docker compose up --build
```

Compose se tu:

- tai Oracle XE image;
- tao user Oracle theo `APP_DB_USERNAME` / `APP_DB_PASSWORD` trong `.env`;
- chay `quan_ly_chuoi_cafe_phung_loc.sql` de tao schema;
- chay `seed.sql` de nap du lieu mau/quyen;
- build va chay Spring Boot backend o `http://localhost:8080`.

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

Tai khoan mac dinh lay tu `.env`:

```text
DEFAULT_ADMIN_USERNAME
DEFAULT_ADMIN_PASSWORD
```

## Chay nen

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

## Reset sach database

Lenh nay xoa volume Oracle va khoi tao DB lai tu dau o lan chay tiep theo:

```bash
docker compose down -v
```

Sau do chay lai:

```bash
docker compose up --build
```

## Ghi chu

- Script init Oracle chi chay khi volume `oracle-data` chua ton tai.
- Neu sua `seed.sql` hoac schema va muon nap lai tu dau, can chay `docker compose down -v`.
- Khong commit file `.env` len GitHub.
- Java Swing cua cac may khac chi can tro `BASE_URL` ve URL backend public bang ngrok/domain.
- Power BI DirectQuery nen truy cap Oracle qua Tailscale/VPN thay vi public port `1521` ra internet.
