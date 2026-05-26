# coffee-chain-management

## Chay nhanh bang Docker

Yeu cau da cai Docker Desktop hoac Docker Engine.

```bash
cp .env.example .env
# sua .env va dien secret that
docker compose up --build
```

Sau khi backend chay xong:

- Swagger: http://localhost:8080/swagger-ui/index.html
- Oracle: `localhost:1521/XEPDB1`
- User DB: gia tri `APP_DB_USERNAME / APP_DB_PASSWORD` trong `.env`
- Tai khoan app mac dinh: `DEFAULT_ADMIN_USERNAME / DEFAULT_ADMIN_PASSWORD` trong `.env`

Xem huong dan chi tiet o [DOCKER.md](DOCKER.md).
