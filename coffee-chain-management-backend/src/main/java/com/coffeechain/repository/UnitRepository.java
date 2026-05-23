package com.coffeechain.repository;

import com.coffeechain.dto.response.UnitResponse;
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
public class UnitRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert unitInsert;

  public UnitRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.unitInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("DONVITINH")
            .usingGeneratedKeyColumns("ma_don_vi_tinh")
            .usingColumns("ten_don_vi_tinh", "ky_hieu");
  }

  private final RowMapper<UnitResponse> mapper =
      (rs, rowNum) ->
          new UnitResponse(
              rs.getLong("ma_don_vi_tinh"),
              rs.getString("ten_don_vi_tinh"),
              rs.getString("ky_hieu"));

  public List<UnitResponse> searchUnits(String keyword) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    ma_don_vi_tinh,
                    ten_don_vi_tinh,
                    ky_hieu
                FROM DONVITINH
                WHERE 1 = 1
                """);

    List<Object> params = new ArrayList<>();

    if (keyword != null && !keyword.isBlank()) {
      sql.append(
          """
                    AND (
                        LOWER(ten_don_vi_tinh) LIKE ?
                        OR LOWER(ky_hieu) LIKE ?
                    )
                    """);

      String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
      params.add(like);
      params.add(like);
    }

    sql.append(" ORDER BY ma_don_vi_tinh ");

    return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
  }

  public Optional<UnitResponse> findById(Long id) {
    String sql =
        """
                SELECT
                    ma_don_vi_tinh,
                    ten_don_vi_tinh,
                    ky_hieu
                FROM DONVITINH
                WHERE ma_don_vi_tinh = ?
                """;

    List<UnitResponse> rows = jdbcTemplate.query(sql, mapper, id);
    return rows.stream().findFirst();
  }

  public boolean existsById(Long id) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM DONVITINH WHERE ma_don_vi_tinh = ?", Integer.class, id);

    return count != null && count > 0;
  }

  public boolean existsBySymbol(String symbol) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM DONVITINH
                WHERE LOWER(ky_hieu) = LOWER(?)
                """,
            Integer.class,
            symbol);

    return count != null && count > 0;
  }

  public boolean existsBySymbolExceptId(String symbol, Long id) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM DONVITINH
                WHERE LOWER(ky_hieu) = LOWER(?)
                  AND ma_don_vi_tinh <> ?
                """,
            Integer.class,
            symbol,
            id);

    return count != null && count > 0;
  }

  public boolean hasIngredient(Long id) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM NGUYENLIEU
                WHERE ma_don_vi_tinh = ?
                """,
            Integer.class,
            id);

    return count != null && count > 0;
  }

  public Long insertUnit(String name, String symbol) {
    Map<String, Object> params = new HashMap<>();
    params.put("ten_don_vi_tinh", name);
    params.put("ky_hieu", symbol);

    Number key = unitInsert.executeAndReturnKey(params);
    return key.longValue();
  }

  public int updateUnit(Long id, String name, String symbol) {
    String sql =
        """
                UPDATE DONVITINH
                SET ten_don_vi_tinh = ?,
                    ky_hieu = ?
                WHERE ma_don_vi_tinh = ?
                """;

    return jdbcTemplate.update(sql, name, symbol, id);
  }

  public int deleteUnit(Long id) {
    return jdbcTemplate.update("DELETE FROM DONVITINH WHERE ma_don_vi_tinh = ?", id);
  }
}
