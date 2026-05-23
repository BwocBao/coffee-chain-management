package com.coffeechain.repository;

import com.coffeechain.dto.response.PermissionResponse;
import com.coffeechain.dto.response.RoleResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RbacRepository {
  private final JdbcTemplate jdbcTemplate;

  public RbacRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<RoleResponse> findAllRoles() {
    String sql =
        """
                SELECT ma_vai_tro, ten_vai_tro
                FROM VAITRO
                ORDER BY
                    CASE ten_vai_tro
                        WHEN 'ADMIN' THEN 1
                        WHEN 'QUAN_LY_KHO' THEN 2
                        WHEN 'QUAN_LY_CHI_NHANH' THEN 3
                        WHEN 'THU_NGAN' THEN 4
                        ELSE 99
                    END,
                    ten_vai_tro
                """;
    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> new RoleResponse(rs.getLong("ma_vai_tro"), rs.getString("ten_vai_tro")));
  }

  public Optional<RoleResponse> findRoleById(Long maVaiTro) {
    List<RoleResponse> rows =
        jdbcTemplate.query(
            """
                SELECT ma_vai_tro, ten_vai_tro
                FROM VAITRO
                WHERE ma_vai_tro = ?
                """,
            (rs, rowNum) -> new RoleResponse(rs.getLong("ma_vai_tro"), rs.getString("ten_vai_tro")),
            maVaiTro);
    return rows.stream().findFirst();
  }

  public Optional<RoleResponse> findRoleByName(String tenVaiTro) {
    List<RoleResponse> rows =
        jdbcTemplate.query(
            """
                SELECT ma_vai_tro, ten_vai_tro
                FROM VAITRO
                WHERE UPPER(ten_vai_tro) = UPPER(?)
                """,
            (rs, rowNum) -> new RoleResponse(rs.getLong("ma_vai_tro"), rs.getString("ten_vai_tro")),
            tenVaiTro);
    return rows.stream().findFirst();
  }

  public Long createRole(String tenVaiTro) {
    jdbcTemplate.update("INSERT INTO VAITRO (ten_vai_tro) VALUES (?)", tenVaiTro);
    return jdbcTemplate.queryForObject(
        "SELECT ma_vai_tro FROM VAITRO WHERE ten_vai_tro = ?", Long.class, tenVaiTro);
  }

  public List<PermissionResponse> findAllPermissions() {
    String sql =
        """
                SELECT q.ma_quyen, q.ma_chuc_nang, c.ten_chuc_nang, q.hanh_dong, q.ten_quyen
                FROM QUYEN q
                JOIN CHUCNANG c ON q.ma_chuc_nang = c.ma_chuc_nang
                ORDER BY
                    CASE c.ten_chuc_nang
                        WHEN 'USER' THEN 1
                        WHEN 'ROLE' THEN 2
                        WHEN 'BRANCH' THEN 3
                        WHEN 'PRODUCT' THEN 4
                        WHEN 'INGREDIENT' THEN 5
                        WHEN 'SUPPLIER' THEN 6
                        WHEN 'INVENTORY' THEN 7
                        WHEN 'STOCKTAKE' THEN 8
                        WHEN 'WASTAGE' THEN 9
                        WHEN 'ORDER' THEN 10
                        WHEN 'REPORT' THEN 11
                        ELSE 99
                    END,
                    CASE q.hanh_dong
                        WHEN 'VIEW' THEN 1
                        WHEN 'CREATE' THEN 2
                        WHEN 'UPDATE' THEN 3
                        WHEN 'DELETE' THEN 4
                        WHEN 'IMPORT' THEN 5
                        WHEN 'EXPORT' THEN 6
                        WHEN 'TRANSFER' THEN 7
                        WHEN 'ADJUST' THEN 8
                        WHEN 'PAY' THEN 9
                        WHEN 'CANCEL' THEN 10
                        WHEN 'REFUND' THEN 11
                        ELSE 99
                    END
                """;
    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new PermissionResponse(
                rs.getLong("ma_quyen"),
                rs.getLong("ma_chuc_nang"),
                rs.getString("ten_chuc_nang"),
                rs.getString("hanh_dong"),
                rs.getString("ten_quyen")));
  }

  public Set<Long> findPermissionIdsByRoleId(Long maVaiTro) {
    List<Long> ids =
        jdbcTemplate.query(
            """
                SELECT ma_quyen
                FROM VAITRO_QUYEN
                WHERE ma_vai_tro = ?
                ORDER BY ma_quyen
                """,
            (rs, rowNum) -> rs.getLong("ma_quyen"),
            maVaiTro);
    return new LinkedHashSet<>(ids);
  }

  public Set<String> findPermissionCodesByRoleId(Long maVaiTro) {
    List<String> codes =
        jdbcTemplate.query(
            """
                SELECT c.ten_chuc_nang, q.hanh_dong
                FROM VAITRO_QUYEN vtq
                JOIN QUYEN q ON vtq.ma_quyen = q.ma_quyen
                JOIN CHUCNANG c ON q.ma_chuc_nang = c.ma_chuc_nang
                WHERE vtq.ma_vai_tro = ?
                ORDER BY c.ten_chuc_nang, q.hanh_dong
                """,
            (rs, rowNum) ->
                rs.getString("ten_chuc_nang").toUpperCase()
                    + ":"
                    + rs.getString("hanh_dong").toUpperCase(),
            maVaiTro);
    return new LinkedHashSet<>(codes);
  }

  public int countPermissionsByIds(Set<Long> ids) {
    if (ids == null || ids.isEmpty()) return 0;
    String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM QUYEN WHERE ma_quyen IN (" + placeholders + ")",
            Integer.class,
            ids.toArray());
    return count == null ? 0 : count;
  }

  public void replaceRolePermissions(Long maVaiTro, Set<Long> permissionIds) {
    jdbcTemplate.update("DELETE FROM VAITRO_QUYEN WHERE ma_vai_tro = ?", maVaiTro);
    if (permissionIds == null || permissionIds.isEmpty()) return;
    for (Long permissionId : permissionIds) {
      jdbcTemplate.update(
          "INSERT INTO VAITRO_QUYEN (ma_vai_tro, ma_quyen) VALUES (?, ?)", maVaiTro, permissionId);
    }
  }
}
