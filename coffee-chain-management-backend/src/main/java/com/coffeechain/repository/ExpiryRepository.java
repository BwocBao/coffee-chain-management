package com.coffeechain.repository;

import com.coffeechain.dto.response.ExpiryLookupResponse;
import com.coffeechain.dto.response.ExpiryLotResponse;
import com.coffeechain.dto.response.ExpiryStatisticsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExpiryRepository {
    private final JdbcTemplate jdbcTemplate;

    public ExpiryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ExpiryLotResponse> lotMapper = (rs, rowNum) -> {
        Timestamp ngayTaoTs = rs.getTimestamp("ngay_tao");
        Date hanSuDungDate = rs.getDate("han_su_dung");

        LocalDateTime ngayTao = ngayTaoTs == null ? null : ngayTaoTs.toLocalDateTime();
        LocalDate hanSuDung = hanSuDungDate == null ? null : hanSuDungDate.toLocalDate();

        BigDecimal daysDecimal = rs.getBigDecimal("so_ngay_con_lai");
        Integer soNgayConLai = daysDecimal == null ? null : daysDecimal.intValue();

        return new ExpiryLotResponse(
                rs.getLong("ma_lo_hang"),
                rs.getLong("ma_kho"),
                rs.getString("ten_kho"),
                rs.getLong("ma_nguyen_lieu"),
                rs.getString("ten_nguyen_lieu"),
                rs.getString("don_vi_tinh"),
                rs.getBigDecimal("so_luong_con_lai"),
                ngayTao,
                hanSuDung,
                soNgayConLai,
                rs.getString("trang_thai"),
                rs.getString("muc_canh_bao")
        );
    };

    public List<ExpiryLotResponse> searchLots(
            Long maKho,
            Long maNguyenLieu,
            String trangThai,
            String mucCanhBao,
            Integer daysToExpire,
            Boolean onlyAvailable,
            Integer warningDays
    ) {
        /*
         * Mức cảnh báo:
         * - HET_HANG: lô đã hết số lượng hoặc USED_UP.
         * - KHONG_CO_HSD: lô không có hạn sử dụng.
         * - DA_HET_HAN: lô đã quá hạn.
         * - SAP_HET_HAN: lô còn hạn nhưng <= warningDays.
         * - BINH_THUONG: còn hạn dài.
         */
        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM (
                    SELECT
                        lh.ma_lo_hang,
                        lh.ma_kho,
                        k.ten_kho,
                        lh.ma_nguyen_lieu,
                        nl.ten_nguyen_lieu,
                        dvt.ky_hieu AS don_vi_tinh,
                        lh.so_luong_con_lai,
                        lh.ngay_tao,
                        lh.han_su_dung,
                        CASE
                            WHEN lh.han_su_dung IS NULL THEN NULL
                            ELSE TRUNC(lh.han_su_dung) - TRUNC(SYSDATE)
                        END AS so_ngay_con_lai,
                        lh.trang_thai,
                        CASE
                            WHEN lh.so_luong_con_lai <= 0 OR lh.trang_thai = 'USED_UP' THEN 'HET_HANG'
                            WHEN lh.han_su_dung IS NULL THEN 'KHONG_CO_HSD'
                            WHEN TRUNC(lh.han_su_dung) < TRUNC(SYSDATE) OR lh.trang_thai = 'EXPIRED' THEN 'DA_HET_HAN'
                            WHEN TRUNC(lh.han_su_dung) <= TRUNC(SYSDATE) + ? THEN 'SAP_HET_HAN'
                            ELSE 'BINH_THUONG'
                        END AS muc_canh_bao
                    FROM LOHANG_NGUYENLIEU lh
                    JOIN KHO k
                        ON k.ma_kho = lh.ma_kho
                    JOIN NGUYENLIEU nl
                        ON nl.ma_nguyen_lieu = lh.ma_nguyen_lieu
                    JOIN DONVITINH dvt
                        ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                ) x
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();
        params.add(warningDays);

        if (maKho != null) {
            sql.append(" AND x.ma_kho = ? ");
            params.add(maKho);
        }

        if (maNguyenLieu != null) {
            sql.append(" AND x.ma_nguyen_lieu = ? ");
            params.add(maNguyenLieu);
        }

        if (trangThai != null && !trangThai.isBlank()) {
            sql.append(" AND x.trang_thai = ? ");
            params.add(trangThai);
        }

        if (mucCanhBao != null && !mucCanhBao.isBlank()) {
            sql.append(" AND x.muc_canh_bao = ? ");
            params.add(mucCanhBao);
        }

        if (daysToExpire != null) {
            sql.append("""
                    AND x.han_su_dung IS NOT NULL
                    AND TRUNC(x.han_su_dung) BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + ?
                    """);
            params.add(daysToExpire);
        }

        if (Boolean.TRUE.equals(onlyAvailable)) {
            sql.append("""
                    AND x.trang_thai = 'ACTIVE'
                    AND x.so_luong_con_lai > 0
                    AND (
                        x.han_su_dung IS NULL
                        OR TRUNC(x.han_su_dung) >= TRUNC(SYSDATE)
                    )
                    """);
        }

        sql.append("""
                ORDER BY
                    CASE x.muc_canh_bao
                        WHEN 'DA_HET_HAN' THEN 1
                        WHEN 'SAP_HET_HAN' THEN 2
                        WHEN 'BINH_THUONG' THEN 3
                        WHEN 'KHONG_CO_HSD' THEN 4
                        WHEN 'HET_HANG' THEN 5
                        ELSE 6
                    END,
                    x.han_su_dung NULLS LAST,
                    x.ma_lo_hang
                """);

        return jdbcTemplate.query(sql.toString(), lotMapper, params.toArray());
    }

    public List<ExpiryLotResponse> findLotById(Long maLoHang, Long forcedMaKho) {
        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM (
                    SELECT
                        lh.ma_lo_hang,
                        lh.ma_kho,
                        k.ten_kho,
                        lh.ma_nguyen_lieu,
                        nl.ten_nguyen_lieu,
                        dvt.ky_hieu AS don_vi_tinh,
                        lh.so_luong_con_lai,
                        lh.ngay_tao,
                        lh.han_su_dung,
                        CASE
                            WHEN lh.han_su_dung IS NULL THEN NULL
                            ELSE TRUNC(lh.han_su_dung) - TRUNC(SYSDATE)
                        END AS so_ngay_con_lai,
                        lh.trang_thai,
                        CASE
                            WHEN lh.so_luong_con_lai <= 0 OR lh.trang_thai = 'USED_UP' THEN 'HET_HANG'
                            WHEN lh.han_su_dung IS NULL THEN 'KHONG_CO_HSD'
                            WHEN TRUNC(lh.han_su_dung) < TRUNC(SYSDATE) OR lh.trang_thai = 'EXPIRED' THEN 'DA_HET_HAN'
                            WHEN TRUNC(lh.han_su_dung) <= TRUNC(SYSDATE) + 30 THEN 'SAP_HET_HAN'
                            ELSE 'BINH_THUONG'
                        END AS muc_canh_bao
                    FROM LOHANG_NGUYENLIEU lh
                    JOIN KHO k
                        ON k.ma_kho = lh.ma_kho
                    JOIN NGUYENLIEU nl
                        ON nl.ma_nguyen_lieu = lh.ma_nguyen_lieu
                    JOIN DONVITINH dvt
                        ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                ) x
                WHERE x.ma_lo_hang = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(maLoHang);

        if (forcedMaKho != null) {
            sql.append(" AND x.ma_kho = ? ");
            params.add(forcedMaKho);
        }

        return jdbcTemplate.query(sql.toString(), lotMapper, params.toArray());
    }

    public ExpiryStatisticsResponse getStatistics(Long maKho, Integer warningDays) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    COUNT(*) AS tong_so_lo,
                    SUM(CASE
                        WHEN lh.trang_thai = 'ACTIVE'
                         AND lh.so_luong_con_lai > 0
                         AND (lh.han_su_dung IS NULL OR TRUNC(lh.han_su_dung) >= TRUNC(SYSDATE))
                        THEN 1 ELSE 0
                    END) AS so_lo_dang_hoat_dong,
                    SUM(CASE
                        WHEN lh.trang_thai = 'ACTIVE'
                         AND lh.so_luong_con_lai > 0
                         AND lh.han_su_dung IS NOT NULL
                         AND TRUNC(lh.han_su_dung) BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + ?
                        THEN 1 ELSE 0
                    END) AS so_lo_sap_het_han,
                    SUM(CASE
                        WHEN lh.so_luong_con_lai > 0
                         AND (lh.trang_thai = 'EXPIRED'
                              OR (lh.han_su_dung IS NOT NULL AND TRUNC(lh.han_su_dung) < TRUNC(SYSDATE)))
                        THEN 1 ELSE 0
                    END) AS so_lo_da_het_han,
                    SUM(CASE
                        WHEN lh.trang_thai = 'USED_UP' OR lh.so_luong_con_lai <= 0
                        THEN 1 ELSE 0
                    END) AS so_lo_da_dung_het,
                    SUM(CASE
                        WHEN lh.han_su_dung IS NULL
                        THEN 1 ELSE 0
                    END) AS so_lo_khong_co_hsd
                FROM LOHANG_NGUYENLIEU lh
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();
        params.add(warningDays);

        if (maKho != null) {
            sql.append(" AND lh.ma_kho = ? ");
            params.add(maKho);
        }

        return jdbcTemplate.queryForObject(sql.toString(), (rs, rowNum) -> {
            ExpiryStatisticsResponse response = new ExpiryStatisticsResponse();
            response.setTongSoLo(rs.getInt("tong_so_lo"));
            response.setSoLoDangHoatDong(rs.getInt("so_lo_dang_hoat_dong"));
            response.setSoLoSapHetHan(rs.getInt("so_lo_sap_het_han"));
            response.setSoLoDaHetHan(rs.getInt("so_lo_da_het_han"));
            response.setSoLoDaDungHet(rs.getInt("so_lo_da_dung_het"));
            response.setSoLoKhongCoHanSuDung(rs.getInt("so_lo_khong_co_hsd"));
            return response;
        }, params.toArray());
    }

    public List<ExpiryLookupResponse.OptionDto> findWarehouseOptions(Long forcedMaKho) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    ma_kho AS id,
                    loai_kho AS code,
                    ten_kho AS name,
                    trang_thai AS description
                FROM KHO
                WHERE trang_thai = 'ACTIVE'
                """);

        List<Object> params = new ArrayList<>();

        if (forcedMaKho != null) {
            sql.append(" AND ma_kho = ? ");
            params.add(forcedMaKho);
        }

        sql.append(" ORDER BY ten_kho ");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new ExpiryLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description")
        ), params.toArray());
    }

    public List<ExpiryLookupResponse.OptionDto> findIngredientOptions() {
        String sql = """
                SELECT
                    nl.ma_nguyen_lieu AS id,
                    nl.trang_thai AS code,
                    nl.ten_nguyen_lieu AS name,
                    dvt.ky_hieu AS description
                FROM NGUYENLIEU nl
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE nl.trang_thai = 'ACTIVE'
                ORDER BY nl.ten_nguyen_lieu
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ExpiryLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description")
        ));
    }

    public Long findActiveWarehouseIdByBranchId(Long maChiNhanh) {
        String sql = """
                SELECT ma_kho
                FROM KHO
                WHERE ma_chi_nhanh = ?
                  AND trang_thai = 'ACTIVE'
                """;

        List<Long> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("ma_kho"), maChiNhanh);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void refreshExpiredLots() {
        /*
         * Procedure này đã có trong file SQL:
         * prc_cap_nhat_lo_het_han
         *
         * Nó cập nhật:
         * - Lô quá hạn -> EXPIRED
         * - Lô số lượng = 0 -> USED_UP
         */
        jdbcTemplate.execute("BEGIN prc_cap_nhat_lo_het_han; END;");
    }
}