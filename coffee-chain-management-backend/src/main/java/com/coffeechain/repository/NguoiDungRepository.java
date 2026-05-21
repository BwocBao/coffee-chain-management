package com.coffeechain.repository;

import com.coffeechain.dto.response.CreateUserLookupResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class NguoiDungRepository {
    private final JdbcTemplate jdbcTemplate;

    public NguoiDungRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<NguoiDungRecord> findByTenDangNhap(String tenDangNhap) {
        String sql = """
                SELECT nd.ma_nguoi_dung, nd.ten_dang_nhap, nd.mat_khau, nd.ma_vai_tro,
                       vt.ten_vai_tro, nd.ma_chi_nhanh, cn.ten_chi_nhanh, nd.trang_thai
                FROM NGUOIDUNG nd
                JOIN VAITRO vt ON nd.ma_vai_tro = vt.ma_vai_tro
                LEFT JOIN CHINHANH cn ON nd.ma_chi_nhanh = cn.ma_chi_nhanh
                WHERE nd.ten_dang_nhap = ?
                """;
        List<NguoiDungRecord> rows = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), tenDangNhap);
        return rows.stream().findFirst();
    }

    public Optional<NguoiDungRecord> findByEmail(String email) {
        if (!hasColumn("NGUOIDUNG", "EMAIL")) {
            return Optional.empty();
        }

        String sql = """
                SELECT nd.ma_nguoi_dung, nd.ten_dang_nhap, nd.mat_khau, nd.ma_vai_tro,
                       vt.ten_vai_tro, nd.ma_chi_nhanh, cn.ten_chi_nhanh, nd.trang_thai
                FROM NGUOIDUNG nd
                JOIN VAITRO vt ON nd.ma_vai_tro = vt.ma_vai_tro
                LEFT JOIN CHINHANH cn ON nd.ma_chi_nhanh = cn.ma_chi_nhanh
                WHERE LOWER(nd.email) = LOWER(?)
                """;
        List<NguoiDungRecord> rows = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), email);
        return rows.stream().findFirst();
    }

    public int updatePasswordByEmail(String email, String matKhauHash) {
        if (!hasColumn("NGUOIDUNG", "EMAIL")) {
            return 0;
        }
        return jdbcTemplate.update(
                "UPDATE NGUOIDUNG SET mat_khau = ? WHERE LOWER(email) = LOWER(?)",
                matKhauHash,
                email
        );
    }

    public Set<String> findPermissionsByUserId(Long maNguoiDung) {
        String sql = """
                SELECT cn.ten_chuc_nang, q.hanh_dong
                FROM NGUOIDUNG nd
                JOIN VAITRO vt ON nd.ma_vai_tro = vt.ma_vai_tro
                JOIN VAITRO_QUYEN vtq ON vt.ma_vai_tro = vtq.ma_vai_tro
                JOIN QUYEN q ON vtq.ma_quyen = q.ma_quyen
                JOIN CHUCNANG cn ON q.ma_chuc_nang = cn.ma_chuc_nang
                WHERE nd.ma_nguoi_dung = ?
                ORDER BY cn.ten_chuc_nang, q.hanh_dong
                """;
        List<String> permissions = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getString("ten_chuc_nang").toUpperCase() + ":" + rs.getString("hanh_dong").toUpperCase(),
                maNguoiDung
        );
        return new LinkedHashSet<>(permissions);
    }

    public boolean existsUser(String tenDangNhap) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NGUOIDUNG WHERE LOWER(ten_dang_nhap) = LOWER(?)",
                Integer.class,
                tenDangNhap
        );
        return count != null && count > 0;
    }

    public boolean existsEmail(String email) {
        if (!hasColumn("NGUOIDUNG", "EMAIL")) return false;

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NGUOIDUNG WHERE LOWER(email) = LOWER(?)",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public boolean existsActiveBranch(Long maChiNhanh) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM CHINHANH
                WHERE ma_chi_nhanh = ?
                  AND trang_thai = 'ACTIVE'
                """,
                Integer.class,
                maChiNhanh
        );

        return count != null && count > 0;
    }

    public Long getOrCreateRole(String tenVaiTro) {
        Long id = findRoleId(tenVaiTro);
        if (id != null) return id;
        jdbcTemplate.update("INSERT INTO VAITRO (ten_vai_tro) VALUES (?)", tenVaiTro.toUpperCase());
        return findRoleId(tenVaiTro);
    }

    public Long findRoleId(String tenVaiTro) {
        return queryLongOrNull("SELECT ma_vai_tro FROM VAITRO WHERE UPPER(ten_vai_tro) = UPPER(?)", tenVaiTro);
    }

    public Long getOrCreateChucNang(String tenChucNang) {
        Long id = queryLongOrNull("SELECT ma_chuc_nang FROM CHUCNANG WHERE UPPER(ten_chuc_nang) = UPPER(?)", tenChucNang);
        if (id != null) return id;
        jdbcTemplate.update("INSERT INTO CHUCNANG (ten_chuc_nang) VALUES (?)", tenChucNang.toUpperCase());
        return queryLongOrNull("SELECT ma_chuc_nang FROM CHUCNANG WHERE UPPER(ten_chuc_nang) = UPPER(?)", tenChucNang);
    }

    public Long getOrCreatePermission(Long maChucNang, String hanhDong, String tenQuyen) {
        Long id = queryLongOrNull(
                "SELECT ma_quyen FROM QUYEN WHERE ma_chuc_nang = ? AND UPPER(hanh_dong) = UPPER(?)",
                maChucNang, hanhDong
        );
        if (id != null) return id;

        jdbcTemplate.update(
                "INSERT INTO QUYEN (ma_chuc_nang, hanh_dong, ten_quyen) VALUES (?, ?, ?)",
                maChucNang, hanhDong.toUpperCase(), tenQuyen
        );
        return queryLongOrNull(
                "SELECT ma_quyen FROM QUYEN WHERE ma_chuc_nang = ? AND UPPER(hanh_dong) = UPPER(?)",
                maChucNang, hanhDong
        );
    }

    public Long findPermissionId(String module, String action) {
        return queryLongOrNull("""
                SELECT q.ma_quyen
                FROM QUYEN q
                JOIN CHUCNANG c ON q.ma_chuc_nang = c.ma_chuc_nang
                WHERE UPPER(c.ten_chuc_nang) = UPPER(?)
                  AND UPPER(q.hanh_dong) = UPPER(?)
                """, module, action);
    }

    public void assignPermissionToRole(Long maVaiTro, Long maQuyen) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM VAITRO_QUYEN WHERE ma_vai_tro = ? AND ma_quyen = ?",
                Integer.class,
                maVaiTro,
                maQuyen
        );
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO VAITRO_QUYEN (ma_vai_tro, ma_quyen) VALUES (?, ?)", maVaiTro, maQuyen);
        }
    }

    public List<CreateUserLookupResponse.OptionDto> findRoleOptionsForCreateUser() {
        String sql = """
            SELECT
                ma_vai_tro AS id,
                ten_vai_tro AS name,
                NULL AS description
            FROM VAITRO
            ORDER BY ten_vai_tro
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CreateUserLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description")
        ));
    }

    public Optional<CreateUserLookupResponse.OptionDto> findRoleOptionByName(String tenVaiTro) {
        String sql = """
            SELECT
                ma_vai_tro AS id,
                ten_vai_tro AS name,
                NULL AS description
            FROM VAITRO
            WHERE UPPER(ten_vai_tro) = UPPER(?)
            """;

        List<CreateUserLookupResponse.OptionDto> rows = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CreateUserLookupResponse.OptionDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ),
                tenVaiTro
        );

        return rows.stream().findFirst();
    }

    public List<CreateUserLookupResponse.OptionDto> findActiveBranchOptions() {
        String sql = """
            SELECT
                ma_chi_nhanh AS id,
                ten_chi_nhanh AS name,
                dia_chi AS description
            FROM CHINHANH
            WHERE trang_thai = 'ACTIVE'
            ORDER BY ten_chi_nhanh
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CreateUserLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description")
        ));
    }

    public Optional<CreateUserLookupResponse.OptionDto> findBranchOptionById(Long maChiNhanh) {
        String sql = """
            SELECT
                ma_chi_nhanh AS id,
                ten_chi_nhanh AS name,
                dia_chi AS description
            FROM CHINHANH
            WHERE ma_chi_nhanh = ?
              AND trang_thai = 'ACTIVE'
            """;

        List<CreateUserLookupResponse.OptionDto> rows = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CreateUserLookupResponse.OptionDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ),
                maChiNhanh
        );

        return rows.stream().findFirst();
    }

    public void createUser(String tenDangNhap, String matKhauHash, Long maVaiTro, Long maChiNhanh) {
        createUser(tenDangNhap, matKhauHash, maVaiTro, maChiNhanh, null);
    }

    public void createUser(String tenDangNhap, String matKhauHash, Long maVaiTro, Long maChiNhanh, String email) {
        if (hasColumn("NGUOIDUNG", "EMAIL")) {
            String finalEmail = (email == null || email.isBlank()) ? tenDangNhap + "@phungloc.local" : email.trim();
            jdbcTemplate.update("""
                    INSERT INTO NGUOIDUNG
                        (ten_dang_nhap, mat_khau, email, ma_vai_tro, ma_chi_nhanh, trang_thai, ngay_tao)
                    VALUES
                        (?, ?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP)
                    """, tenDangNhap, matKhauHash, finalEmail, maVaiTro, maChiNhanh);
        } else {
            jdbcTemplate.update("""
                    INSERT INTO NGUOIDUNG
                        (ten_dang_nhap, mat_khau, ma_vai_tro, ma_chi_nhanh, trang_thai, ngay_tao)
                    VALUES
                        (?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP)
                    """, tenDangNhap, matKhauHash, maVaiTro, maChiNhanh);
        }
    }

    public List<String> findAllRoleNames() {
        return jdbcTemplate.query(
                "SELECT ten_vai_tro FROM VAITRO ORDER BY ten_vai_tro",
                (rs, rowNum) -> rs.getString("ten_vai_tro")
        );
    }

    public List<String> findAllPermissionNames() {
        return jdbcTemplate.query("""
                SELECT c.ten_chuc_nang, q.hanh_dong
                FROM QUYEN q
                JOIN CHUCNANG c ON q.ma_chuc_nang = c.ma_chuc_nang
                ORDER BY c.ten_chuc_nang, q.hanh_dong
                """, (rs, rowNum) -> rs.getString("ten_chuc_nang").toUpperCase() + ":" + rs.getString("hanh_dong").toUpperCase());
    }

    public Map<String, Set<String>> findPermissionsGroupedByRole() {
        String sql = """
                SELECT vt.ten_vai_tro, c.ten_chuc_nang, q.hanh_dong
                FROM VAITRO vt
                LEFT JOIN VAITRO_QUYEN vtq ON vt.ma_vai_tro = vtq.ma_vai_tro
                LEFT JOIN QUYEN q ON vtq.ma_quyen = q.ma_quyen
                LEFT JOIN CHUCNANG c ON q.ma_chuc_nang = c.ma_chuc_nang
                ORDER BY vt.ten_vai_tro, c.ten_chuc_nang, q.hanh_dong
                """;
        Map<String, Set<String>> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String role = rs.getString("ten_vai_tro");
            result.putIfAbsent(role, new LinkedHashSet<>());
            String module = rs.getString("ten_chuc_nang");
            String action = rs.getString("hanh_dong");
            if (module != null && action != null) {
                result.get(role).add(module.toUpperCase() + ":" + action.toUpperCase());
            }
        });
        return result;
    }

    private boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = UPPER(?) AND COLUMN_NAME = UPPER(?)",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private NguoiDungRecord mapUser(ResultSet rs) throws SQLException {
        NguoiDungRecord user = new NguoiDungRecord();
        user.setMaNguoiDung(rs.getLong("ma_nguoi_dung"));
        user.setTenDangNhap(rs.getString("ten_dang_nhap"));
        user.setMatKhau(rs.getString("mat_khau"));
        user.setMaVaiTro(rs.getLong("ma_vai_tro"));
        user.setTenVaiTro(rs.getString("ten_vai_tro"));

        long maChiNhanh = rs.getLong("ma_chi_nhanh");
        user.setMaChiNhanh(rs.wasNull() ? null : maChiNhanh);
        user.setTenChiNhanh(rs.getString("ten_chi_nhanh"));
        user.setTrangThai(rs.getString("trang_thai"));
        return user;
    }

    private Long queryLongOrNull(String sql, Object... args) {
        List<Long> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong(1), args);
        return ids.isEmpty() ? null : ids.get(0);
    }
}
