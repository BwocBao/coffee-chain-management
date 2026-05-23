package com.coffeechain.repository;

import com.coffeechain.dto.response.WarehouseLookupResponse;
import com.coffeechain.dto.response.WarehouseResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class WarehouseRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert warehouseInsert;

  public WarehouseRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.warehouseInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("KHO")
            .usingGeneratedKeyColumns("ma_kho")
            .usingColumns("ten_kho", "loai_kho", "ma_chi_nhanh", "trang_thai");
  }

  private final RowMapper<WarehouseResponse> mapper =
      (rs, rowNum) -> {
        Long maChiNhanh = rs.getObject("ma_chi_nhanh") == null ? null : rs.getLong("ma_chi_nhanh");

        return new WarehouseResponse(
            rs.getLong("ma_kho"),
            rs.getString("ten_kho"),
            rs.getString("loai_kho"),
            maChiNhanh,
            rs.getString("ten_chi_nhanh"),
            rs.getString("trang_thai"));
      };

  public List<WarehouseResponse> searchWarehouses(
      String keyword, String loaiKho, String trangThai) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    k.ma_kho,
                    k.ten_kho,
                    k.loai_kho,
                    k.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    k.trang_thai
                FROM KHO k
                LEFT JOIN CHINHANH cn
                    ON cn.ma_chi_nhanh = k.ma_chi_nhanh
                WHERE 1 = 1
                """);

    List<Object> params = new ArrayList<>();

    if (keyword != null && !keyword.isBlank()) {
      sql.append(
          """
                    AND (
                        LOWER(k.ten_kho) LIKE ?
                        OR LOWER(k.loai_kho) LIKE ?
                        OR LOWER(cn.ten_chi_nhanh) LIKE ?
                    )
                    """);

      String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
      params.add(like);
      params.add(like);
      params.add(like);
    }

    if (loaiKho != null && !loaiKho.isBlank()) {
      sql.append(" AND k.loai_kho = ? ");
      params.add(loaiKho.trim().toUpperCase(Locale.ROOT));
    }

    if (trangThai != null && !trangThai.isBlank()) {
      sql.append(" AND k.trang_thai = ? ");
      params.add(trangThai.trim().toUpperCase(Locale.ROOT));
    }

    sql.append(" ORDER BY k.ma_kho ");

    return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
  }

  public Optional<WarehouseResponse> findById(Long id) {
    String sql =
        """
                SELECT
                    k.ma_kho,
                    k.ten_kho,
                    k.loai_kho,
                    k.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    k.trang_thai
                FROM KHO k
                LEFT JOIN CHINHANH cn
                    ON cn.ma_chi_nhanh = k.ma_chi_nhanh
                WHERE k.ma_kho = ?
                """;

    List<WarehouseResponse> rows = jdbcTemplate.query(sql, mapper, id);
    return rows.stream().findFirst();
  }

  public boolean existsById(Long id) {
    Integer count =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM KHO WHERE ma_kho = ?", Integer.class, id);

    return count != null && count > 0;
  }

  public boolean existsByName(String tenKho) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM KHO
                WHERE LOWER(ten_kho) = LOWER(?)
                """,
            Integer.class,
            tenKho);

    return count != null && count > 0;
  }

  public boolean existsByNameExceptId(String tenKho, Long maKho) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM KHO
                WHERE LOWER(ten_kho) = LOWER(?)
                  AND ma_kho <> ?
                """,
            Integer.class,
            tenKho,
            maKho);

    return count != null && count > 0;
  }

  public boolean existsActiveBranch(Long maChiNhanh) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM CHINHANH
                WHERE ma_chi_nhanh = ?
                  AND trang_thai = 'ACTIVE'
                """,
            Integer.class,
            maChiNhanh);

    return count != null && count > 0;
  }

  public boolean existsCentralWarehouse() {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM KHO
                WHERE loai_kho = 'CENTRAL'
                """,
            Integer.class);

    return count != null && count > 0;
  }

  public boolean existsCentralWarehouseExceptId(Long maKho) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM KHO
                WHERE loai_kho = 'CENTRAL'
                  AND ma_kho <> ?
                """,
            Integer.class,
            maKho);

    return count != null && count > 0;
  }

  public boolean existsWarehouseForBranch(Long maChiNhanh) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM KHO
                WHERE ma_chi_nhanh = ?
                """,
            Integer.class,
            maChiNhanh);

    return count != null && count > 0;
  }

  public boolean existsWarehouseForBranchExceptId(Long maChiNhanh, Long maKho) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM KHO
                WHERE ma_chi_nhanh = ?
                  AND ma_kho <> ?
                """,
            Integer.class,
            maChiNhanh,
            maKho);

    return count != null && count > 0;
  }

  public Long insertWarehouse(String tenKho, String loaiKho, Long maChiNhanh, String trangThai) {
    Map<String, Object> params = new HashMap<>();
    params.put("ten_kho", tenKho);
    params.put("loai_kho", loaiKho);
    params.put("ma_chi_nhanh", maChiNhanh);
    params.put("trang_thai", trangThai);

    Number key = warehouseInsert.executeAndReturnKey(params);
    return key.longValue();
  }

  public int updateWarehouse(
      Long maKho, String tenKho, String loaiKho, Long maChiNhanh, String trangThai) {
    String sql =
        """
                UPDATE KHO
                SET ten_kho = ?,
                    loai_kho = ?,
                    ma_chi_nhanh = ?,
                    trang_thai = ?
                WHERE ma_kho = ?
                """;

    return jdbcTemplate.update(sql, tenKho, loaiKho, maChiNhanh, trangThai, maKho);
  }

  public int updateStatus(Long maKho, String trangThai) {
    String sql =
        """
                UPDATE KHO
                SET trang_thai = ?
                WHERE ma_kho = ?
                """;

    return jdbcTemplate.update(sql, trangThai, maKho);
  }

  public List<WarehouseLookupResponse.OptionDto> findActiveBranchOptions() {
    String sql =
        """
                SELECT
                    ma_chi_nhanh AS id,
                    ten_chi_nhanh AS name,
                    dia_chi AS description
                FROM CHINHANH
                WHERE trang_thai = 'ACTIVE'
                ORDER BY ten_chi_nhanh
                """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new WarehouseLookupResponse.OptionDto(
                rs.getLong("id"), null, rs.getString("name"), rs.getString("description")));
  }
}
