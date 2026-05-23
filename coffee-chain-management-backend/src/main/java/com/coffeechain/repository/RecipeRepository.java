package com.coffeechain.repository;

import com.coffeechain.dto.response.RecipeIngredientLineResponse;
import com.coffeechain.dto.response.RecipeLookupResponse;
import com.coffeechain.dto.response.RecipeSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class RecipeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert productInsert;
    private final SimpleJdbcInsert formulaInsert;

    public RecipeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        this.productInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("SANPHAM")
                .usingGeneratedKeyColumns("ma_san_pham")
                .usingColumns(
                        "ten_san_pham",
                        "hinh_anh",
                        "gia_ban_hien_tai",
                        "trang_thai"
                );

        this.formulaInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("CONGTHUC_SANPHAM")
                .usingGeneratedKeyColumns("ma_cong_thuc")
                .usingColumns(
                        "ma_san_pham",
                        "ma_nguyen_lieu",
                        "so_luong_can"
                );
    }

    public List<RecipeSummaryResponse> searchRecipes(String keyword, String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    sp.gia_ban_hien_tai,
                    sp.trang_thai,
                    sp.ngay_tao,
                    COUNT(ct.ma_cong_thuc) AS so_nguyen_lieu
                FROM SANPHAM sp
                LEFT JOIN CONGTHUC_SANPHAM ct
                    ON ct.ma_san_pham = sp.ma_san_pham
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND LOWER(sp.ten_san_pham) LIKE ? ");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND sp.trang_thai = ? ");
            params.add(status);
        }

        sql.append("""
                GROUP BY
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    sp.gia_ban_hien_tai,
                    sp.trang_thai,
                    sp.ngay_tao
                ORDER BY sp.ma_san_pham
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new RecipeSummaryResponse(
                rs.getLong("ma_san_pham"),
                toRecipeCode(rs.getLong("ma_san_pham")),
                rs.getString("ten_san_pham"),
                rs.getBigDecimal("gia_ban_hien_tai"),
                rs.getString("trang_thai"),
                toLocalDateTime(rs.getTimestamp("ngay_tao")),
                rs.getInt("so_nguyen_lieu")
        ), params.toArray());
    }

    public RecipeSummaryResponse findRecipeSummaryById(Long maSanPham) {
        String sql = """
                SELECT
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    sp.gia_ban_hien_tai,
                    sp.trang_thai,
                    sp.ngay_tao,
                    COUNT(ct.ma_cong_thuc) AS so_nguyen_lieu
                FROM SANPHAM sp
                LEFT JOIN CONGTHUC_SANPHAM ct
                    ON ct.ma_san_pham = sp.ma_san_pham
                WHERE sp.ma_san_pham = ?
                GROUP BY
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    sp.gia_ban_hien_tai,
                    sp.trang_thai,
                    sp.ngay_tao
                """;

        List<RecipeSummaryResponse> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new RecipeSummaryResponse(
                rs.getLong("ma_san_pham"),
                toRecipeCode(rs.getLong("ma_san_pham")),
                rs.getString("ten_san_pham"),
                rs.getBigDecimal("gia_ban_hien_tai"),
                rs.getString("trang_thai"),
                toLocalDateTime(rs.getTimestamp("ngay_tao")),
                rs.getInt("so_nguyen_lieu")
        ), maSanPham);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public String findProductImage(Long maSanPham) {
        String sql = """
                SELECT hinh_anh
                FROM SANPHAM
                WHERE ma_san_pham = ?
                """;

        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("hinh_anh"), maSanPham);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<RecipeIngredientLineResponse> findRecipeItems(Long maSanPham) {
        String sql = """
                WITH latest_cost AS (
                    SELECT
                        ctpn.ma_nguyen_lieu,
                        ctpn.don_gia_nhap,
                        ROW_NUMBER() OVER (
                            PARTITION BY ctpn.ma_nguyen_lieu
                            ORDER BY pn.ngay_nhap DESC, ctpn.ma_ct_phieu_nhap DESC
                        ) AS rn
                    FROM CHITIETPHIEUNHAP ctpn
                    JOIN PHIEUNHAP pn
                        ON pn.ma_phieu_nhap = ctpn.ma_phieu_nhap
                )
                SELECT
                    ct.ma_cong_thuc,
                    nl.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    ct.so_luong_can,
                    NVL(lc.don_gia_nhap, 0) AS gia_von_dvt
                FROM CONGTHUC_SANPHAM ct
                JOIN NGUYENLIEU nl
                    ON nl.ma_nguyen_lieu = ct.ma_nguyen_lieu
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                LEFT JOIN latest_cost lc
                    ON lc.ma_nguyen_lieu = nl.ma_nguyen_lieu
                   AND lc.rn = 1
                WHERE ct.ma_san_pham = ?
                ORDER BY ct.ma_cong_thuc
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BigDecimal soLuongCan = rs.getBigDecimal("so_luong_can");
            BigDecimal giaVonDvt = rs.getBigDecimal("gia_von_dvt");
            BigDecimal thanhTien = soLuongCan.multiply(giaVonDvt);

            return new RecipeIngredientLineResponse(
                    rs.getLong("ma_cong_thuc"),
                    rs.getLong("ma_nguyen_lieu"),
                    rs.getString("ten_nguyen_lieu"),
                    rs.getString("don_vi_tinh"),
                    soLuongCan,
                    giaVonDvt,
                    thanhTien
            );
        }, maSanPham);
    }

    public Long insertProduct(
            String tenSanPham,
            String hinhAnh,
            BigDecimal giaBanHienTai,
            String trangThai
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ten_san_pham", tenSanPham);
        params.put("hinh_anh", hinhAnh);
        params.put("gia_ban_hien_tai", giaBanHienTai);
        params.put("trang_thai", trangThai);

        return productInsert.executeAndReturnKey(params).longValue();
    }

    public void updateProduct(
            Long maSanPham,
            String tenSanPham,
            String hinhAnh,
            BigDecimal giaBanHienTai,
            String trangThai
    ) {
        String sql = """
                UPDATE SANPHAM
                SET ten_san_pham = ?,
                    hinh_anh = ?,
                    gia_ban_hien_tai = ?,
                    trang_thai = ?
                WHERE ma_san_pham = ?
                """;

        jdbcTemplate.update(
                sql,
                tenSanPham,
                hinhAnh,
                giaBanHienTai,
                trangThai,
                maSanPham
        );
    }

    public Long insertFormulaItem(
            Long maSanPham,
            Long maNguyenLieu,
            BigDecimal soLuongCan
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ma_san_pham", maSanPham);
        params.put("ma_nguyen_lieu", maNguyenLieu);
        params.put("so_luong_can", soLuongCan);

        return formulaInsert.executeAndReturnKey(params).longValue();
    }

    public void deleteFormulaByProduct(Long maSanPham) {
        jdbcTemplate.update(
                "DELETE FROM CONGTHUC_SANPHAM WHERE ma_san_pham = ?",
                maSanPham
        );
    }

    public void updateProductStatus(Long maSanPham, String status) {
        jdbcTemplate.update(
                "UPDATE SANPHAM SET trang_thai = ? WHERE ma_san_pham = ?",
                status,
                maSanPham
        );
    }

    public boolean productExists(Long maSanPham) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SANPHAM WHERE ma_san_pham = ?",
                Integer.class,
                maSanPham
        );

        return count != null && count > 0;
    }

    public boolean productNameExists(String tenSanPham, Long ignoreProductId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM SANPHAM
                WHERE LOWER(ten_san_pham) = LOWER(?)
                """);

        List<Object> params = new ArrayList<>();
        params.add(tenSanPham);

        if (ignoreProductId != null) {
            sql.append(" AND ma_san_pham <> ? ");
            params.add(ignoreProductId);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null && count > 0;
    }

    public boolean ingredientExists(Long maNguyenLieu) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM NGUYENLIEU
                WHERE ma_nguyen_lieu = ?
                  AND trang_thai = 'ACTIVE'
                """,
                Integer.class,
                maNguyenLieu
        );

        return count != null && count > 0;
    }

    public List<RecipeLookupResponse.IngredientOption> findIngredientOptions() {
        String sql = """
                WITH latest_cost AS (
                    SELECT
                        ctpn.ma_nguyen_lieu,
                        ctpn.don_gia_nhap,
                        ROW_NUMBER() OVER (
                            PARTITION BY ctpn.ma_nguyen_lieu
                            ORDER BY pn.ngay_nhap DESC, ctpn.ma_ct_phieu_nhap DESC
                        ) AS rn
                    FROM CHITIETPHIEUNHAP ctpn
                    JOIN PHIEUNHAP pn
                        ON pn.ma_phieu_nhap = ctpn.ma_phieu_nhap
                )
                SELECT
                    nl.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu,
                    NVL(lc.don_gia_nhap, 0) AS gia_von_dvt
                FROM NGUYENLIEU nl
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                LEFT JOIN latest_cost lc
                    ON lc.ma_nguyen_lieu = nl.ma_nguyen_lieu
                   AND lc.rn = 1
                WHERE nl.trang_thai = 'ACTIVE'
                ORDER BY nl.ten_nguyen_lieu
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new RecipeLookupResponse.IngredientOption(
                rs.getLong("ma_nguyen_lieu"),
                rs.getString("ten_nguyen_lieu"),
                rs.getString("ky_hieu"),
                rs.getBigDecimal("gia_von_dvt")
        ));
    }

    private static String toRecipeCode(Long maSanPham) {
        return "CT" + String.format("%03d", maSanPham);
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}