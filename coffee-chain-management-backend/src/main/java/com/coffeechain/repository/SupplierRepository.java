package com.coffeechain.repository;


import com.coffeechain.dto.response.SupplierResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
public class SupplierRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert supplierInsert;

    public SupplierRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.supplierInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("NHACUNGCAP")
                .usingGeneratedKeyColumns("ma_nha_cung_cap")
                .usingColumns(
                        "ten_nha_cung_cap",
                        "so_dien_thoai",
                        "email",
                        "dia_chi"
                );
    }

    private final RowMapper<SupplierResponse> mapper = (rs, rowNum) -> new SupplierResponse(
            rs.getLong("ma_nha_cung_cap"),
            rs.getString("ten_nha_cung_cap"),
            rs.getString("so_dien_thoai"),
            rs.getString("email"),
            rs.getString("dia_chi")
    );

    public List<SupplierResponse> searchSuppliers(String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ma_nha_cung_cap,
                    ten_nha_cung_cap,
                    so_dien_thoai,
                    email,
                    dia_chi
                FROM NHACUNGCAP
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(ten_nha_cung_cap) LIKE ?
                        OR LOWER(so_dien_thoai) LIKE ?
                        OR LOWER(email) LIKE ?
                        OR LOWER(dia_chi) LIKE ?
                    )
                    """);

            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append(" ORDER BY ma_nha_cung_cap ");

        return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
    }

    public Optional<SupplierResponse> findById(Long id) {
        String sql = """
                SELECT
                    ma_nha_cung_cap,
                    ten_nha_cung_cap,
                    so_dien_thoai,
                    email,
                    dia_chi
                FROM NHACUNGCAP
                WHERE ma_nha_cung_cap = ?
                """;

        List<SupplierResponse> rows = jdbcTemplate.query(sql, mapper, id);
        return rows.stream().findFirst();
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NHACUNGCAP WHERE ma_nha_cung_cap = ?",
                Integer.class,
                id
        );

        return count != null && count > 0;
    }

    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM NHACUNGCAP
                WHERE LOWER(ten_nha_cung_cap) = LOWER(?)
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
                FROM NHACUNGCAP
                WHERE LOWER(ten_nha_cung_cap) = LOWER(?)
                  AND ma_nha_cung_cap <> ?
                """,
                Integer.class,
                name,
                id
        );

        return count != null && count > 0;
    }

    public boolean hasImportReceipt(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PHIEUNHAP
                WHERE ma_nha_cung_cap = ?
                """,
                Integer.class,
                id
        );

        return count != null && count > 0;
    }

    public Long insertSupplier(
            String name,
            String phone,
            String email,
            String address
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("ten_nha_cung_cap", name);
        params.put("so_dien_thoai", phone);
        params.put("email", email);
        params.put("dia_chi", address);

        Number key = supplierInsert.executeAndReturnKey(params);
        return key.longValue();
    }

    public int updateSupplier(
            Long id,
            String name,
            String phone,
            String email,
            String address
    ) {
        String sql = """
                UPDATE NHACUNGCAP
                SET ten_nha_cung_cap = ?,
                    so_dien_thoai = ?,
                    email = ?,
                    dia_chi = ?
                WHERE ma_nha_cung_cap = ?
                """;

        return jdbcTemplate.update(sql, name, phone, email, address, id);
    }

    public int deleteSupplier(Long id) {
        return jdbcTemplate.update(
                "DELETE FROM NHACUNGCAP WHERE ma_nha_cung_cap = ?",
                id
        );
    }
}