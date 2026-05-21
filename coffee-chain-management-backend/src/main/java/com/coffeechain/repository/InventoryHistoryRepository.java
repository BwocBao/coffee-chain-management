package com.coffeechain.repository;

import com.coffeechain.dto.response.InventoryHistoryLookupResponse;
import com.coffeechain.dto.response.InventoryHistoryResponse;
import com.coffeechain.dto.response.InventoryHistorySummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Repository
public class InventoryHistoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public InventoryHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<InventoryHistoryResponse> historyMapper = (rs, rowNum) -> {
        Long maLoHang = rs.getObject("ma_lo_hang") == null ? null : rs.getLong("ma_lo_hang");
        Long maChungTu = rs.getObject("ma_chung_tu") == null ? null : rs.getLong("ma_chung_tu");
        Long maNguoiThaoTac = rs.getObject("nguoi_thao_tac") == null ? null : rs.getLong("nguoi_thao_tac");

        Timestamp thoiGianTs = rs.getTimestamp("thoi_gian");
        LocalDateTime thoiGian = thoiGianTs == null ? null : thoiGianTs.toLocalDateTime();

        return new InventoryHistoryResponse(
                rs.getLong("ma_nhat_ky_kho"),
                rs.getLong("ma_kho"),
                rs.getString("ten_kho"),
                rs.getLong("ma_nguyen_lieu"),
                rs.getString("ten_nguyen_lieu"),
                rs.getString("don_vi_tinh"),
                maLoHang,
                rs.getString("loai_giao_dich"),
                rs.getString("ten_chung_tu"),
                maChungTu,
                rs.getBigDecimal("so_luong_thay_doi"),
                rs.getBigDecimal("so_luong_truoc"),
                rs.getBigDecimal("so_luong_sau"),
                thoiGian,
                maNguoiThaoTac,
                rs.getString("ten_nguoi_thao_tac")
        );
    };

    public List<InventoryHistoryResponse> searchHistory(
            Long maKho,
            Long maNguyenLieu,
            Long maLoHang,
            String loaiGiaoDich,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String keyword
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    nkk.ma_nhat_ky_kho,
                    nkk.ma_kho,
                    k.ten_kho,
                    nkk.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    nkk.ma_lo_hang,
                    nkk.loai_giao_dich,
                    nkk.ten_chung_tu,
                    nkk.ma_chung_tu,
                    nkk.so_luong_thay_doi,
                    nkk.so_luong_truoc,
                    nkk.so_luong_sau,
                    nkk.thoi_gian,
                    nkk.nguoi_thao_tac,
                    nd.ten_dang_nhap AS ten_nguoi_thao_tac
                FROM NHATKY_KHO nkk
                JOIN KHO k
                    ON k.ma_kho = nkk.ma_kho
                JOIN NGUYENLIEU nl
                    ON nl.ma_nguyen_lieu = nkk.ma_nguyen_lieu
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                LEFT JOIN NGUOIDUNG nd
                    ON nd.ma_nguoi_dung = nkk.nguoi_thao_tac
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        appendCommonFilters(sql, params, maKho, maNguyenLieu, maLoHang, loaiGiaoDich, fromDate, toDate, keyword);

        sql.append(" ORDER BY nkk.thoi_gian DESC, nkk.ma_nhat_ky_kho DESC ");

        return jdbcTemplate.query(sql.toString(), historyMapper, params.toArray());
    }

    public List<InventoryHistoryResponse> findHistoryById(Long id, Long forcedMaKho) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    nkk.ma_nhat_ky_kho,
                    nkk.ma_kho,
                    k.ten_kho,
                    nkk.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    nkk.ma_lo_hang,
                    nkk.loai_giao_dich,
                    nkk.ten_chung_tu,
                    nkk.ma_chung_tu,
                    nkk.so_luong_thay_doi,
                    nkk.so_luong_truoc,
                    nkk.so_luong_sau,
                    nkk.thoi_gian,
                    nkk.nguoi_thao_tac,
                    nd.ten_dang_nhap AS ten_nguoi_thao_tac
                FROM NHATKY_KHO nkk
                JOIN KHO k
                    ON k.ma_kho = nkk.ma_kho
                JOIN NGUYENLIEU nl
                    ON nl.ma_nguyen_lieu = nkk.ma_nguyen_lieu
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                LEFT JOIN NGUOIDUNG nd
                    ON nd.ma_nguoi_dung = nkk.nguoi_thao_tac
                WHERE nkk.ma_nhat_ky_kho = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(id);

        if (forcedMaKho != null) {
            sql.append(" AND nkk.ma_kho = ? ");
            params.add(forcedMaKho);
        }

        return jdbcTemplate.query(sql.toString(), historyMapper, params.toArray());
    }

    public List<InventoryHistorySummaryResponse> getSummary(
            Long maKho,
            Long maNguyenLieu,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    nkk.ma_kho,
                    k.ten_kho,
                    nkk.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'IMPORT'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_nhap,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'EXPORT'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_xuat,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'TRANSFER_IN'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_dieu_chuyen_vao,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'TRANSFER_OUT'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_dieu_chuyen_ra,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'WASTAGE'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_hao_hut,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'SALE_DEDUCT'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_ban_hang_tru_kho,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'SALE_REVERSE'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_hoan_tru_kho,

                    SUM(CASE WHEN nkk.loai_giao_dich = 'STOCKTAKE_ADJUST'
                        THEN ABS(nkk.so_luong_thay_doi) ELSE 0 END) AS tong_dieu_chinh_kiem_kho,

                    SUM(nkk.so_luong_sau - nkk.so_luong_truoc) AS bien_dong_rong,

                    COUNT(*) AS so_giao_dich
                FROM NHATKY_KHO nkk
                JOIN KHO k
                    ON k.ma_kho = nkk.ma_kho
                JOIN NGUYENLIEU nl
                    ON nl.ma_nguyen_lieu = nkk.ma_nguyen_lieu
                JOIN DONVITINH dvt
                    ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (maKho != null) {
            sql.append(" AND nkk.ma_kho = ? ");
            params.add(maKho);
        }

        if (maNguyenLieu != null) {
            sql.append(" AND nkk.ma_nguyen_lieu = ? ");
            params.add(maNguyenLieu);
        }

        if (fromDate != null) {
            sql.append(" AND nkk.thoi_gian >= ? ");
            params.add(Timestamp.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND nkk.thoi_gian <= ? ");
            params.add(Timestamp.valueOf(toDate));
        }

        sql.append("""
                GROUP BY
                    nkk.ma_kho,
                    k.ten_kho,
                    nkk.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu
                ORDER BY k.ten_kho, nl.ten_nguyen_lieu
                """);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            InventoryHistorySummaryResponse response = new InventoryHistorySummaryResponse();

            response.setMaKho(rs.getLong("ma_kho"));
            response.setTenKho(rs.getString("ten_kho"));
            response.setMaNguyenLieu(rs.getLong("ma_nguyen_lieu"));
            response.setTenNguyenLieu(rs.getString("ten_nguyen_lieu"));
            response.setDonViTinh(rs.getString("don_vi_tinh"));

            response.setTongNhap(rs.getBigDecimal("tong_nhap"));
            response.setTongXuat(rs.getBigDecimal("tong_xuat"));
            response.setTongDieuChuyenVao(rs.getBigDecimal("tong_dieu_chuyen_vao"));
            response.setTongDieuChuyenRa(rs.getBigDecimal("tong_dieu_chuyen_ra"));
            response.setTongHaoHut(rs.getBigDecimal("tong_hao_hut"));
            response.setTongBanHangTruKho(rs.getBigDecimal("tong_ban_hang_tru_kho"));
            response.setTongHoanTruKho(rs.getBigDecimal("tong_hoan_tru_kho"));
            response.setTongDieuChinhKiemKho(rs.getBigDecimal("tong_dieu_chinh_kiem_kho"));
            response.setBienDongRong(rs.getBigDecimal("bien_dong_rong"));
            response.setSoGiaoDich(rs.getInt("so_giao_dich"));

            return response;
        }, params.toArray());
    }

    private void appendCommonFilters(
            StringBuilder sql,
            List<Object> params,
            Long maKho,
            Long maNguyenLieu,
            Long maLoHang,
            String loaiGiaoDich,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String keyword
    ) {
        if (maKho != null) {
            sql.append(" AND nkk.ma_kho = ? ");
            params.add(maKho);
        }

        if (maNguyenLieu != null) {
            sql.append(" AND nkk.ma_nguyen_lieu = ? ");
            params.add(maNguyenLieu);
        }

        if (maLoHang != null) {
            sql.append(" AND nkk.ma_lo_hang = ? ");
            params.add(maLoHang);
        }

        if (loaiGiaoDich != null && !loaiGiaoDich.isBlank()) {
            sql.append(" AND nkk.loai_giao_dich = ? ");
            params.add(loaiGiaoDich.trim().toUpperCase(Locale.ROOT));
        }

        if (fromDate != null) {
            sql.append(" AND nkk.thoi_gian >= ? ");
            params.add(Timestamp.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND nkk.thoi_gian <= ? ");
            params.add(Timestamp.valueOf(toDate));
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(k.ten_kho) LIKE ?
                        OR LOWER(nl.ten_nguyen_lieu) LIKE ?
                        OR LOWER(nkk.loai_giao_dich) LIKE ?
                        OR LOWER(nkk.ten_chung_tu) LIKE ?
                        OR LOWER(nd.ten_dang_nhap) LIKE ?
                        OR TO_CHAR(nkk.ma_chung_tu) LIKE ?
                    )
                    """);

            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add("%" + keyword.trim() + "%");
        }
    }

    public List<InventoryHistoryLookupResponse.OptionDto> findWarehouseOptions(Long forcedMaKho) {
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

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new InventoryHistoryLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description")
        ), params.toArray());
    }

    public List<InventoryHistoryLookupResponse.OptionDto> findIngredientOptions() {
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

        return jdbcTemplate.query(sql, (rs, rowNum) -> new InventoryHistoryLookupResponse.OptionDto(
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
}