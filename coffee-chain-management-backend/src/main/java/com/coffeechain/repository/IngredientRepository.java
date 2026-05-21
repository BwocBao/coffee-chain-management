package com.coffeechain.repository;

import com.coffeechain.dto.response.IngredientLookupResponse;
import com.coffeechain.dto.response.IngredientResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
public class IngredientRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert ingredientInsert;

    public IngredientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.ingredientInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("NGUYENLIEU")
                .usingGeneratedKeyColumns("ma_nguyen_lieu")
                .usingColumns(
                        "ten_nguyen_lieu",
                        "ma_don_vi_tinh",
                        "muc_ton_toi_thieu",
                        "trang_thai"
                );
    }

    private final RowMapper<IngredientResponse> mapper = (rs, rowNum) -> new IngredientResponse(
            rs.getLong("ma_nguyen_lieu"),
            rs.getString("ten_nguyen_lieu"),
            rs.getLong("ma_don_vi_tinh"),
            rs.getString("ten_don_vi_tinh"),
            rs.getString("ky_hieu"),
            rs.getBigDecimal("muc_ton_toi_thieu"),
            rs.getString("trang_thai")
    );

    public List<IngredientResponse> searchIngredients(
            String keyword,
            String status,
            Long unitId
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    nl.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    nl.ma_don_vi_tinh,
                    dvt.ten_don_vi_tinh,
                    dvt.ky_hieu,
                    nl.muc_ton_toi_thieu,
                    nl.trang_thai
                FROM NGUYENLIEU nl
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(nl.ten_nguyen_lieu) LIKE ?
                        OR LOWER(dvt.ten_don_vi_tinh) LIKE ?
                        OR LOWER(dvt.ky_hieu) LIKE ?
                    )
                    """);

            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND nl.trang_thai = ? ");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }

        if (unitId != null) {
            sql.append(" AND nl.ma_don_vi_tinh = ? ");
            params.add(unitId);
        }

        sql.append(" ORDER BY nl.ma_nguyen_lieu ");

        return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
    }

    public Optional<IngredientResponse> findById(Long id) {
        String sql = """
                SELECT
                    nl.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    nl.ma_don_vi_tinh,
                    dvt.ten_don_vi_tinh,
                    dvt.ky_hieu,
                    nl.muc_ton_toi_thieu,
                    nl.trang_thai
                FROM NGUYENLIEU nl
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE nl.ma_nguyen_lieu = ?
                """;

        List<IngredientResponse> rows = jdbcTemplate.query(sql, mapper, id);
        return rows.stream().findFirst();
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NGUYENLIEU WHERE ma_nguyen_lieu = ?",
                Integer.class,
                id
        );

        return count != null && count > 0;
    }

    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM NGUYENLIEU
                WHERE LOWER(ten_nguyen_lieu) = LOWER(?)
                """,
                Integer.class,
                name
        );

        return count != null && count > 0;
    }

    public boolean existsByNameExceptId(String name, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM NGUYENLIEU
                WHERE LOWER(ten_nguyen_lieu) = LOWER(?)
                  AND ma_nguyen_lieu <> ?
                """,
                Integer.class,
                name,
                id
        );

        return count != null && count > 0;
    }

    public boolean existsUnit(Long unitId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM DONVITINH
                WHERE ma_don_vi_tinh = ?
                """,
                Integer.class,
                unitId
        );

        return count != null && count > 0;
    }

    public Long insertIngredient(
            String name,
            Long unitId,
            BigDecimal minimumStock,
            String status
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("ten_nguyen_lieu", name);
        params.put("ma_don_vi_tinh", unitId);
        params.put("muc_ton_toi_thieu", minimumStock);
        params.put("trang_thai", status);

        Number key = ingredientInsert.executeAndReturnKey(params);
        return key.longValue();
    }

    public int updateIngredient(
            Long id,
            String name,
            Long unitId,
            BigDecimal minimumStock,
            String status
    ) {
        String sql = """
                UPDATE NGUYENLIEU
                SET ten_nguyen_lieu = ?,
                    ma_don_vi_tinh = ?,
                    muc_ton_toi_thieu = ?,
                    trang_thai = ?
                WHERE ma_nguyen_lieu = ?
                """;

        return jdbcTemplate.update(sql, name, unitId, minimumStock, status, id);
    }

    public int updateStatus(Long id, String status) {
        String sql = """
                UPDATE NGUYENLIEU
                SET trang_thai = ?
                WHERE ma_nguyen_lieu = ?
                """;

        return jdbcTemplate.update(sql, status, id);
    }

    public List<IngredientLookupResponse.OptionDto> findUnitOptions() {
        String sql = """
                SELECT
                    ma_don_vi_tinh AS id,
                    ten_don_vi_tinh AS name,
                    ky_hieu AS description
                FROM DONVITINH
                ORDER BY ma_don_vi_tinh
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new IngredientLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description")
        ));
    }
}