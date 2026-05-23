package com.coffeechain.repository;

import com.coffeechain.dto.response.BranchResponse;
import com.coffeechain.dto.response.BranchStatisticsResponse;
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
public class BranchRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert branchInsert;

  public BranchRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.branchInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("CHINHANH")
            .usingGeneratedKeyColumns("ma_chi_nhanh")
            .usingColumns("ten_chi_nhanh", "dia_chi", "so_dien_thoai", "trang_thai");
  }

  private final RowMapper<BranchResponse> mapper =
      (rs, rowNum) -> {
        Long maKho = rs.getObject("ma_kho") == null ? null : rs.getLong("ma_kho");

        return new BranchResponse(
            rs.getLong("ma_chi_nhanh"),
            rs.getString("ten_chi_nhanh"),
            rs.getString("dia_chi"),
            rs.getString("so_dien_thoai"),
            maKho,
            rs.getString("ten_kho"),
            rs.getInt("so_nhan_vien"),
            rs.getString("trang_thai"));
      };

  public List<BranchResponse> searchBranches(String keyword, String status) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    cn.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    cn.dia_chi,
                    cn.so_dien_thoai,
                    cn.trang_thai,
                    k.ma_kho,
                    k.ten_kho,
                    COUNT(nd.ma_nguoi_dung) AS so_nhan_vien
                FROM CHINHANH cn
                LEFT JOIN KHO k
                    ON k.ma_chi_nhanh = cn.ma_chi_nhanh
                LEFT JOIN NGUOIDUNG nd
                    ON nd.ma_chi_nhanh = cn.ma_chi_nhanh
                WHERE 1 = 1
                """);

    List<Object> params = new ArrayList<>();

    if (keyword != null && !keyword.isBlank()) {
      sql.append(
          """
                    AND (
                        LOWER(cn.ten_chi_nhanh) LIKE ?
                        OR LOWER(cn.dia_chi) LIKE ?
                        OR LOWER(cn.so_dien_thoai) LIKE ?
                        OR LOWER(k.ten_kho) LIKE ?
                    )
                    """);

      String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
      params.add(like);
      params.add(like);
      params.add(like);
      params.add(like);
    }

    if (status != null && !status.isBlank()) {
      sql.append(" AND cn.trang_thai = ? ");
      params.add(status.trim().toUpperCase(Locale.ROOT));
    }

    sql.append(
        """
                GROUP BY
                    cn.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    cn.dia_chi,
                    cn.so_dien_thoai,
                    cn.trang_thai,
                    k.ma_kho,
                    k.ten_kho
                ORDER BY cn.ma_chi_nhanh
                """);

    return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
  }

  public Optional<BranchResponse> findById(Long id) {
    String sql =
        """
                SELECT
                    cn.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    cn.dia_chi,
                    cn.so_dien_thoai,
                    cn.trang_thai,
                    k.ma_kho,
                    k.ten_kho,
                    COUNT(nd.ma_nguoi_dung) AS so_nhan_vien
                FROM CHINHANH cn
                LEFT JOIN KHO k
                    ON k.ma_chi_nhanh = cn.ma_chi_nhanh
                LEFT JOIN NGUOIDUNG nd
                    ON nd.ma_chi_nhanh = cn.ma_chi_nhanh
                WHERE cn.ma_chi_nhanh = ?
                GROUP BY
                    cn.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    cn.dia_chi,
                    cn.so_dien_thoai,
                    cn.trang_thai,
                    k.ma_kho,
                    k.ten_kho
                """;

    List<BranchResponse> rows = jdbcTemplate.query(sql, mapper, id);
    return rows.stream().findFirst();
  }

  public boolean existsById(Long id) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM CHINHANH WHERE ma_chi_nhanh = ?", Integer.class, id);

    return count != null && count > 0;
  }

  public boolean existsByName(String name) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM CHINHANH
                WHERE LOWER(ten_chi_nhanh) = LOWER(?)
                """,
            Integer.class,
            name);

    return count != null && count > 0;
  }

  public boolean existsByNameExceptId(String name, Long id) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM CHINHANH
                WHERE LOWER(ten_chi_nhanh) = LOWER(?)
                  AND ma_chi_nhanh <> ?
                """,
            Integer.class,
            name,
            id);

    return count != null && count > 0;
  }

  public Long insertBranch(String name, String address, String phone, String status) {
    Map<String, Object> params = new HashMap<>();
    params.put("ten_chi_nhanh", name);
    params.put("dia_chi", address);
    params.put("so_dien_thoai", phone);
    params.put("trang_thai", status);

    Number key = branchInsert.executeAndReturnKey(params);
    return key.longValue();
  }

  public int updateBranch(Long id, String name, String address, String phone, String status) {
    String sql =
        """
                UPDATE CHINHANH
                SET ten_chi_nhanh = ?,
                    dia_chi = ?,
                    so_dien_thoai = ?,
                    trang_thai = ?
                WHERE ma_chi_nhanh = ?
                """;

    return jdbcTemplate.update(sql, name, address, phone, status, id);
  }

  public int updateStatus(Long id, String status) {
    String sql =
        """
                UPDATE CHINHANH
                SET trang_thai = ?
                WHERE ma_chi_nhanh = ?
                """;

    return jdbcTemplate.update(sql, status, id);
  }

  public BranchStatisticsResponse getStatistics() {
    BranchStatisticsResponse response =
        jdbcTemplate.queryForObject(
            """
                SELECT
                    COUNT(*) AS tong_so_chi_nhanh,
                    SUM(CASE WHEN trang_thai = 'ACTIVE' THEN 1 ELSE 0 END) AS dang_hoat_dong,
                    SUM(CASE WHEN trang_thai = 'CLOSED' THEN 1 ELSE 0 END) AS da_dong,
                    SUM(CASE WHEN trang_thai = 'MAINTENANCE' THEN 1 ELSE 0 END) AS bao_tri
                FROM CHINHANH
                """,
            (rs, rowNum) -> {
              BranchStatisticsResponse data = new BranchStatisticsResponse();
              data.setTongSoChiNhanh(rs.getInt("tong_so_chi_nhanh"));
              data.setSoChiNhanhDangHoatDong(rs.getInt("dang_hoat_dong"));
              data.setSoChiNhanhDaDong(rs.getInt("da_dong"));
              data.setSoChiNhanhBaoTri(rs.getInt("bao_tri"));
              return data;
            });

    String topBranchSql =
        """
                SELECT
                    cn.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    COUNT(nd.ma_nguoi_dung) AS so_nhan_vien
                FROM CHINHANH cn
                LEFT JOIN NGUOIDUNG nd
                    ON nd.ma_chi_nhanh = cn.ma_chi_nhanh
                GROUP BY
                    cn.ma_chi_nhanh,
                    cn.ten_chi_nhanh
                ORDER BY so_nhan_vien DESC, cn.ma_chi_nhanh ASC
                FETCH FIRST 1 ROWS ONLY
                """;

    List<BranchStatisticsResponse> topRows =
        jdbcTemplate.query(
            topBranchSql,
            (rs, rowNum) -> {
              BranchStatisticsResponse data = new BranchStatisticsResponse();
              data.setMaChiNhanhNhieuNhanVienNhat(rs.getLong("ma_chi_nhanh"));
              data.setTenChiNhanhNhieuNhanVienNhat(rs.getString("ten_chi_nhanh"));
              data.setSoNhanVienNhieuNhat(rs.getInt("so_nhan_vien"));
              return data;
            });

    if (response != null && !topRows.isEmpty()) {
      BranchStatisticsResponse top = topRows.get(0);
      response.setMaChiNhanhNhieuNhanVienNhat(top.getMaChiNhanhNhieuNhanVienNhat());
      response.setTenChiNhanhNhieuNhanVienNhat(top.getTenChiNhanhNhieuNhanVienNhat());
      response.setSoNhanVienNhieuNhat(top.getSoNhanVienNhieuNhat());
    }

    return response;
  }
}
