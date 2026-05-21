package com.coffeechain.repository;

import com.coffeechain.dto.request.CreateExportReceiptItemRequest;
import com.coffeechain.dto.request.CreateImportReceiptItemRequest;
import com.coffeechain.dto.response.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert importReceiptInsert;
    private final SimpleJdbcInsert importDetailInsert;
    private final SimpleJdbcInsert exportReceiptInsert;
    private final SimpleJdbcInsert exportDetailInsert;
    private final SimpleJdbcInsert transferReceiptInsert;
    private final SimpleJdbcInsert transferDetailInsert;
    private final SimpleJdbcInsert lotInsert;

    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.importReceiptInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("PHIEUNHAP")
                .usingColumns("MA_KHO", "MA_NHA_CUNG_CAP", "TONG_TIEN", "NGUOI_TAO", "GHI_CHU")
                .usingGeneratedKeyColumns("MA_PHIEU_NHAP");
        this.importDetailInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("CHITIETPHIEUNHAP")
                .usingColumns("MA_PHIEU_NHAP", "MA_NGUYEN_LIEU", "SO_LUONG_NHAP", "DON_GIA_NHAP", "SO_LO", "HAN_SU_DUNG")
                .usingGeneratedKeyColumns("MA_CT_PHIEU_NHAP");
        this.exportReceiptInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("PHIEUXUAT")
                .usingColumns("MA_KHO", "LOAI_XUAT", "TONG_GIA_TRI_XUAT", "NGUOI_TAO", "TRANG_THAI", "GHI_CHU")
                .usingGeneratedKeyColumns("MA_PHIEU_XUAT");
        this.exportDetailInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("CHITIETPHIEUXUAT")
                .usingColumns("MA_PHIEU_XUAT", "MA_NGUYEN_LIEU", "MA_LO_HANG", "SO_LUONG_XUAT", "DON_GIA_XUAT")
                .usingGeneratedKeyColumns("MA_CT_PHIEU_XUAT");
        this.transferReceiptInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("PHIEUDIEUCHUYEN")
                .usingColumns("MA_KHO_NGUON", "MA_KHO_DICH", "NGUOI_TAO", "TRANG_THAI", "GHI_CHU")
                .usingGeneratedKeyColumns("MA_PHIEU_DIEU_CHUYEN");

        this.transferDetailInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("CHITIETPHIEUDIEUCHUYEN")
                .usingColumns(
                        "MA_PHIEU_DIEU_CHUYEN",
                        "MA_NGUYEN_LIEU",
                        "MA_LO_HANG_NGUON",
                        "MA_LO_HANG_DICH",
                        "SO_LUONG_DIEU_CHUYEN"
                )
                .usingGeneratedKeyColumns("MA_CT_PHIEU_DIEU_CHUYEN");
        this.lotInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("LOHANG_NGUYENLIEU")
                .usingColumns("MA_KHO", "MA_NGUYEN_LIEU", "MA_CT_PHIEU_NHAP", "SO_LUONG_CON_LAI", "TRANG_THAI", "HAN_SU_DUNG")
                .usingGeneratedKeyColumns("MA_LO_HANG");
    }

    public List<InventoryOptionResponse> findWarehouses() {
        return jdbcTemplate.query("""
                SELECT k.ma_kho, k.ten_kho, k.loai_kho, cn.ten_chi_nhanh
                FROM KHO k
                LEFT JOIN CHINHANH cn ON cn.ma_chi_nhanh = k.ma_chi_nhanh
                WHERE k.trang_thai = 'ACTIVE'
                ORDER BY CASE WHEN k.loai_kho = 'CENTRAL' THEN 0 ELSE 1 END, k.ten_kho
                """, (rs, rowNum) -> {
            String description = rs.getString("loai_kho");
            String branchName = rs.getString("ten_chi_nhanh");
            if (branchName != null && !branchName.isBlank()) {
                description += " - " + branchName;
            }
            return new InventoryOptionResponse(
                    rs.getLong("ma_kho"),
                    rs.getString("ten_kho"),
                    description
            );
        });
    }

    public List<InventoryOptionResponse> findSuppliers() {
        return jdbcTemplate.query("""
                SELECT ma_nha_cung_cap, ten_nha_cung_cap, so_dien_thoai
                FROM NHACUNGCAP
                ORDER BY ten_nha_cung_cap
                """, (rs, rowNum) -> new InventoryOptionResponse(
                rs.getLong("ma_nha_cung_cap"),
                rs.getString("ten_nha_cung_cap"),
                rs.getString("so_dien_thoai")
        ));
    }

    public List<InventoryOptionResponse> findIngredients() {
        return jdbcTemplate.query("""
                SELECT nl.ma_nguyen_lieu,
                       nl.ten_nguyen_lieu,
                       COALESCE(dvt.ky_hieu, '-') AS don_vi_tinh
                FROM NGUYENLIEU nl
                LEFT JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE nl.trang_thai = 'ACTIVE'
                ORDER BY nl.ten_nguyen_lieu
                """, (rs, rowNum) -> new InventoryOptionResponse(
                rs.getLong("ma_nguyen_lieu"),
                rs.getString("ten_nguyen_lieu"),
                rs.getString("don_vi_tinh")
        ));
    }

    public List<InventoryLotResponse> findLotResponsesForExport(Long maKho, Long maNguyenLieu) {
        return jdbcTemplate.query("""
                SELECT lh.ma_lo_hang,
                       cpn.so_lo,
                       lh.ma_nguyen_lieu,
                       nl.ten_nguyen_lieu,
                       COALESCE(dvt.ky_hieu, '-') AS don_vi_tinh,
                       lh.so_luong_con_lai,
                       lh.han_su_dung,
                       lh.ngay_tao
                FROM LOHANG_NGUYENLIEU lh
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = lh.ma_nguyen_lieu
                LEFT JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                LEFT JOIN CHITIETPHIEUNHAP cpn ON cpn.ma_ct_phieu_nhap = lh.ma_ct_phieu_nhap
                WHERE lh.ma_kho = ?
                  AND lh.ma_nguyen_lieu = ?
                  AND lh.trang_thai = 'ACTIVE'
                  AND lh.so_luong_con_lai > 0
                ORDER BY
                  CASE WHEN lh.han_su_dung IS NULL THEN 1 ELSE 0 END,
                  lh.han_su_dung ASC,
                  lh.ngay_tao ASC,
                  lh.ma_lo_hang ASC
                """, (rs, rowNum) -> {
            InventoryLotResponse response = new InventoryLotResponse();
            response.setMaLoHang(rs.getLong("ma_lo_hang"));
            response.setSoLo(rs.getString("so_lo"));
            response.setMaNguyenLieu(rs.getLong("ma_nguyen_lieu"));
            response.setTenNguyenLieu(rs.getString("ten_nguyen_lieu"));
            response.setDonViTinh(rs.getString("don_vi_tinh"));
            response.setSoLuongConLai(rs.getBigDecimal("so_luong_con_lai"));
            Date hanSuDung = rs.getDate("han_su_dung");
            response.setHanSuDung(hanSuDung == null ? null : hanSuDung.toLocalDate());
            response.setNgayTao(toLocalDateTime(rs.getTimestamp("ngay_tao")));
            return response;
        }, maKho, maNguyenLieu);
    }

    public List<LotRecord> lockLotsForExport(Long maKho, Long maNguyenLieu) {
        return jdbcTemplate.query("""
                SELECT ma_lo_hang, ma_kho, ma_nguyen_lieu, so_luong_con_lai, han_su_dung, ngay_tao
                FROM LOHANG_NGUYENLIEU
                WHERE ma_kho = ?
                  AND ma_nguyen_lieu = ?
                  AND trang_thai = 'ACTIVE'
                  AND so_luong_con_lai > 0
                ORDER BY
                  CASE WHEN han_su_dung IS NULL THEN 1 ELSE 0 END,
                  han_su_dung ASC,
                  ngay_tao ASC,
                  ma_lo_hang ASC
                FOR UPDATE
                """, (rs, rowNum) -> mapLotRecord(rs), maKho, maNguyenLieu);
    }

    public LotRecord lockLotForExport(Long maLoHang, Long maKho, Long maNguyenLieu) {
        List<LotRecord> rows = jdbcTemplate.query("""
                SELECT ma_lo_hang, ma_kho, ma_nguyen_lieu, so_luong_con_lai, han_su_dung, ngay_tao
                FROM LOHANG_NGUYENLIEU
                WHERE ma_lo_hang = ?
                  AND ma_kho = ?
                  AND ma_nguyen_lieu = ?
                  AND trang_thai = 'ACTIVE'
                  AND so_luong_con_lai > 0
                FOR UPDATE
                """, (rs, rowNum) -> mapLotRecord(rs), maLoHang, maKho, maNguyenLieu);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public WarehouseRecord findWarehouse(Long maKho) {
        List<WarehouseRecord> rows = jdbcTemplate.query("""
                SELECT ma_kho, ten_kho, loai_kho, ma_chi_nhanh
                FROM KHO
                WHERE ma_kho = ? AND trang_thai = 'ACTIVE'
                """, (rs, rowNum) -> new WarehouseRecord(
                rs.getLong("ma_kho"),
                rs.getString("ten_kho"),
                rs.getString("loai_kho"),
                readNullableLong(rs.getObject("ma_chi_nhanh"))
        ), maKho);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public String findSupplierName(Long maNhaCungCap) {
        List<String> rows = jdbcTemplate.query(
                "SELECT ten_nha_cung_cap FROM NHACUNGCAP WHERE ma_nha_cung_cap = ?",
                (rs, rowNum) -> rs.getString("ten_nha_cung_cap"),
                maNhaCungCap
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean ingredientExists(Long maNguyenLieu) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NGUYENLIEU WHERE ma_nguyen_lieu = ? AND trang_thai = 'ACTIVE'",
                Integer.class,
                maNguyenLieu
        );
        return count != null && count > 0;
    }

    public Long createImportReceipt(Long maKho, Long maNhaCungCap, BigDecimal tongTien, Long nguoiTao, String ghiChu) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MA_KHO", maKho);
        params.put("MA_NHA_CUNG_CAP", maNhaCungCap);
        params.put("TONG_TIEN", tongTien);
        params.put("NGUOI_TAO", nguoiTao);
        params.put("GHI_CHU", normalizeText(ghiChu));
        return importReceiptInsert.executeAndReturnKey(params).longValue();
    }

    public Long createExportReceipt(Long maKho, String loaiXuat, BigDecimal tongGiaTriXuat, Long nguoiTao, String ghiChu) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MA_KHO", maKho);
        params.put("LOAI_XUAT", normalizeText(loaiXuat));
        params.put("TONG_GIA_TRI_XUAT", tongGiaTriXuat);
        params.put("NGUOI_TAO", nguoiTao);
        params.put("TRANG_THAI", "COMPLETED");
        params.put("GHI_CHU", normalizeText(ghiChu));
        return exportReceiptInsert.executeAndReturnKey(params).longValue();
    }

    public Long createImportDetail(Long maPhieuNhap, CreateImportReceiptItemRequest item) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MA_PHIEU_NHAP", maPhieuNhap);
        params.put("MA_NGUYEN_LIEU", item.getMaNguyenLieu());
        params.put("SO_LUONG_NHAP", item.getSoLuongNhap());
        params.put("DON_GIA_NHAP", item.getDonGiaNhap());
        params.put("SO_LO", normalizeText(item.getSoLo()));
        params.put("HAN_SU_DUNG", toSqlDate(item.getHanSuDung()));
        return importDetailInsert.executeAndReturnKey(params).longValue();
    }

    public Long createExportDetail(Long maPhieuXuat, CreateExportReceiptItemRequest item, Long maLoHang, BigDecimal soLuongXuat, BigDecimal donGiaXuat) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MA_PHIEU_XUAT", maPhieuXuat);
        params.put("MA_NGUYEN_LIEU", item.getMaNguyenLieu());
        params.put("MA_LO_HANG", maLoHang);
        params.put("SO_LUONG_XUAT", soLuongXuat);
        params.put("DON_GIA_XUAT", donGiaXuat);
        return exportDetailInsert.executeAndReturnKey(params).longValue();
    }

    public Long createTransferReceipt(
            Long maKhoNguon,
            Long maKhoDich,
            Long nguoiTao,
            String ghiChu
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MA_KHO_NGUON", maKhoNguon);
        params.put("MA_KHO_DICH", maKhoDich);
        params.put("NGUOI_TAO", nguoiTao);
        params.put("TRANG_THAI", "COMPLETED");
        params.put("GHI_CHU", normalizeText(ghiChu));

        return transferReceiptInsert.executeAndReturnKey(params).longValue();
    }

    public Long createTransferDetail(
            Long maPhieuDieuChuyen,
            Long maNguyenLieu,
            Long maLoHangNguon,
            Long maLoHangDich,
            BigDecimal soLuongDieuChuyen
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MA_PHIEU_DIEU_CHUYEN", maPhieuDieuChuyen);
        params.put("MA_NGUYEN_LIEU", maNguyenLieu);
        params.put("MA_LO_HANG_NGUON", maLoHangNguon);
        params.put("MA_LO_HANG_DICH", maLoHangDich);
        params.put("SO_LUONG_DIEU_CHUYEN", soLuongDieuChuyen);

        return transferDetailInsert.executeAndReturnKey(params).longValue();
    }

    public TransferReceiptResponse findTransferReceipt(Long maPhieuDieuChuyen, int detailCount) {
        return jdbcTemplate.queryForObject("""
            SELECT
                pdc.ma_phieu_dieu_chuyen,
                pdc.ngay_tao AS ngay_dieu_chuyen,
                pdc.trang_thai,
                kn.ten_kho AS ten_kho_nguon,
                kd.ten_kho AS ten_kho_dich
            FROM PHIEUDIEUCHUYEN pdc
            JOIN KHO kn ON kn.ma_kho = pdc.ma_kho_nguon
            JOIN KHO kd ON kd.ma_kho = pdc.ma_kho_dich
            WHERE pdc.ma_phieu_dieu_chuyen = ?
            """, (rs, rowNum) -> {
            TransferReceiptResponse response = new TransferReceiptResponse();
            response.setMaPhieuDieuChuyen(rs.getLong("ma_phieu_dieu_chuyen"));
            response.setNgayDieuChuyen(toLocalDateTime(rs.getTimestamp("ngay_dieu_chuyen")));
            response.setTrangThai(rs.getString("trang_thai"));
            response.setTenKhoNguon(rs.getString("ten_kho_nguon"));
            response.setTenKhoDich(rs.getString("ten_kho_dich"));
            response.setSoDongChiTiet(detailCount);
            return response;
        }, maPhieuDieuChuyen);
    }

    public Long createLot(Long maKho, Long maNguyenLieu, Long maCtPhieuNhap, BigDecimal soLuong, LocalDate hanSuDung) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MA_KHO", maKho);
        params.put("MA_NGUYEN_LIEU", maNguyenLieu);
        params.put("MA_CT_PHIEU_NHAP", maCtPhieuNhap);
        params.put("SO_LUONG_CON_LAI", soLuong);
        params.put("TRANG_THAI", "ACTIVE");
        params.put("HAN_SU_DUNG", toSqlDate(hanSuDung));
        return lotInsert.executeAndReturnKey(params).longValue();
    }

    public BigDecimal findCurrentStock(Long maKho, Long maNguyenLieu) {
        List<BigDecimal> rows = jdbcTemplate.query("""
                SELECT so_luong_ton
                FROM TONKHO
                WHERE ma_kho = ? AND ma_nguyen_lieu = ?
                """, (rs, rowNum) -> rs.getBigDecimal("so_luong_ton"), maKho, maNguyenLieu);
        return rows.isEmpty() ? BigDecimal.ZERO : rows.get(0);
    }

    public void increaseStock(Long maKho, Long maNguyenLieu, BigDecimal quantity) {
        jdbcTemplate.update("""
                MERGE INTO TONKHO t
                USING (SELECT ? ma_kho, ? ma_nguyen_lieu, ? so_luong FROM dual) s
                ON (t.ma_kho = s.ma_kho AND t.ma_nguyen_lieu = s.ma_nguyen_lieu)
                WHEN MATCHED THEN UPDATE SET
                    t.so_luong_ton = t.so_luong_ton + s.so_luong,
                    t.lan_cap_nhat_cuoi = CURRENT_TIMESTAMP
                WHEN NOT MATCHED THEN INSERT
                    (ma_kho, ma_nguyen_lieu, so_luong_ton, lan_cap_nhat_cuoi)
                    VALUES (s.ma_kho, s.ma_nguyen_lieu, s.so_luong, CURRENT_TIMESTAMP)
                """, maKho, maNguyenLieu, quantity);
    }

    public boolean decreaseStock(Long maKho, Long maNguyenLieu, BigDecimal quantity) {
        int updated = jdbcTemplate.update("""
                UPDATE TONKHO
                SET so_luong_ton = so_luong_ton - ?,
                    lan_cap_nhat_cuoi = CURRENT_TIMESTAMP
                WHERE ma_kho = ?
                  AND ma_nguyen_lieu = ?
                  AND so_luong_ton >= ?
                """, quantity, maKho, maNguyenLieu, quantity);
        return updated == 1;
    }

    public boolean decreaseLot(Long maLoHang, BigDecimal quantity) {
        int updated = jdbcTemplate.update("""
                UPDATE LOHANG_NGUYENLIEU
                SET so_luong_con_lai = so_luong_con_lai - ?,
                    trang_thai = CASE WHEN so_luong_con_lai - ? <= 0 THEN 'USED_UP' ELSE 'ACTIVE' END
                WHERE ma_lo_hang = ?
                  AND trang_thai = 'ACTIVE'
                  AND so_luong_con_lai >= ?
                """, quantity, quantity, maLoHang, quantity);
        return updated == 1;
    }

    public void createInventoryJournal(
            Long maKho,
            Long maNguyenLieu,
            Long maLoHang,
            Long maPhieuNhap,
            BigDecimal soLuongThayDoi,
            BigDecimal soLuongTruoc,
            BigDecimal soLuongSau,
            Long nguoiThaoTac
    ) {
        createInventoryJournal(
                maKho,
                maNguyenLieu,
                maLoHang,
                "IMPORT",
                "PHIEUNHAP",
                maPhieuNhap,
                soLuongThayDoi,
                soLuongTruoc,
                soLuongSau,
                nguoiThaoTac
        );
    }

    public void createInventoryJournal(
            Long maKho,
            Long maNguyenLieu,
            Long maLoHang,
            String loaiGiaoDich,
            String tenChungTu,
            Long maChungTu,
            BigDecimal soLuongThayDoi,
            BigDecimal soLuongTruoc,
            BigDecimal soLuongSau,
            Long nguoiThaoTac
    ) {
        jdbcTemplate.update("""
                INSERT INTO NHATKY_KHO
                    (ma_kho, ma_nguyen_lieu, ma_lo_hang, loai_giao_dich, ten_chung_tu,
                     ma_chung_tu, so_luong_thay_doi, so_luong_truoc, so_luong_sau, nguoi_thao_tac)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, maKho, maNguyenLieu, maLoHang, loaiGiaoDich, tenChungTu, maChungTu, soLuongThayDoi, soLuongTruoc, soLuongSau, nguoiThaoTac);
    }

    public ImportReceiptResponse findImportReceipt(Long maPhieuNhap, int detailCount) {
        return jdbcTemplate.queryForObject("""
                SELECT pn.ma_phieu_nhap, pn.ngay_nhap, pn.tong_tien, k.ten_kho, ncc.ten_nha_cung_cap
                FROM PHIEUNHAP pn
                JOIN KHO k ON k.ma_kho = pn.ma_kho
                JOIN NHACUNGCAP ncc ON ncc.ma_nha_cung_cap = pn.ma_nha_cung_cap
                WHERE pn.ma_phieu_nhap = ?
                """, (rs, rowNum) -> {
            ImportReceiptResponse response = new ImportReceiptResponse();
            response.setMaPhieuNhap(rs.getLong("ma_phieu_nhap"));
            response.setNgayNhap(toLocalDateTime(rs.getTimestamp("ngay_nhap")));
            response.setTongTien(rs.getBigDecimal("tong_tien"));
            response.setTenKho(rs.getString("ten_kho"));
            response.setTenNhaCungCap(rs.getString("ten_nha_cung_cap"));
            response.setSoDongChiTiet(detailCount);
            return response;
        }, maPhieuNhap);
    }

    public ExportReceiptResponse findExportReceipt(Long maPhieuXuat, int detailCount) {
        return jdbcTemplate.queryForObject("""
                SELECT px.ma_phieu_xuat, px.ngay_xuat, px.loai_xuat, px.trang_thai,
                       px.tong_gia_tri_xuat, k.ten_kho
                FROM PHIEUXUAT px
                JOIN KHO k ON k.ma_kho = px.ma_kho
                WHERE px.ma_phieu_xuat = ?
                """, (rs, rowNum) -> {
            ExportReceiptResponse response = new ExportReceiptResponse();
            response.setMaPhieuXuat(rs.getLong("ma_phieu_xuat"));
            response.setNgayXuat(toLocalDateTime(rs.getTimestamp("ngay_xuat")));
            response.setLoaiXuat(rs.getString("loai_xuat"));
            response.setTrangThai(rs.getString("trang_thai"));
            response.setTongGiaTriXuat(rs.getBigDecimal("tong_gia_tri_xuat"));
            response.setTenKho(rs.getString("ten_kho"));
            response.setSoDongChiTiet(detailCount);
            return response;
        }, maPhieuXuat);
    }

    public List<InventoryStockOptionResponse> findIngredientsWithStockForExport(Long maKho) {
        String sql = """
            SELECT
                nl.ma_nguyen_lieu AS id,
                nl.ten_nguyen_lieu AS name,
                dvt.ky_hieu AS description,
                SUM(lh.so_luong_con_lai) AS so_luong_ton
            FROM LOHANG_NGUYENLIEU lh
            JOIN NGUYENLIEU nl
                ON nl.ma_nguyen_lieu = lh.ma_nguyen_lieu
            JOIN DONVITINH dvt
                ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
            WHERE lh.ma_kho = ?
              AND lh.trang_thai = 'ACTIVE'
              AND lh.so_luong_con_lai > 0
              AND (
                    lh.han_su_dung IS NULL
                    OR lh.han_su_dung >= TRUNC(SYSDATE)
                  )
            GROUP BY
                nl.ma_nguyen_lieu,
                nl.ten_nguyen_lieu,
                dvt.ky_hieu
            ORDER BY nl.ten_nguyen_lieu
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new InventoryStockOptionResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("so_luong_ton")
        ), maKho);
    }

    public List<InventoryResponse> findStock(
            Long maKho,
            Long maNguyenLieu,
            String tuKhoa,
            String trangThaiTonKho,
            Long maChiNhanh,
            int page,
            int size
    ) {
        StockQuery stockQuery = buildStockQuery(maKho, maNguyenLieu, tuKhoa, trangThaiTonKho, maChiNhanh);
        List<Object> params = new ArrayList<>(stockQuery.params());
        params.add(page * size);
        params.add(size);

        String sql = """
                SELECT
                    tk.ma_ton_kho,
                    k.ma_kho,
                    k.ten_kho,
                    k.loai_kho,
                    k.ma_chi_nhanh,
                    nl.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ten_don_vi_tinh,
                    dvt.ky_hieu,
                    tk.so_luong_ton,
                    nl.muc_ton_toi_thieu,
                    CASE
                        WHEN tk.so_luong_ton = 0 THEN 'HET_HANG'
                        WHEN tk.so_luong_ton <= nl.muc_ton_toi_thieu THEN 'TON_THAP'
                        ELSE 'ON_DINH'
                    END AS trang_thai_ton_kho,
                    tk.lan_cap_nhat_cuoi
                FROM TONKHO tk
                JOIN KHO k ON k.ma_kho = tk.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = tk.ma_nguyen_lieu
                LEFT JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                """ + stockQuery.whereSql() + """
                ORDER BY
                    CASE WHEN k.loai_kho = 'CENTRAL' THEN 0 ELSE 1 END,
                    k.ten_kho,
                    nl.ten_nguyen_lieu
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            InventoryResponse response = new InventoryResponse();
            response.setMaTonKho(rs.getLong("ma_ton_kho"));
            response.setMaKho(rs.getLong("ma_kho"));
            response.setTenKho(rs.getString("ten_kho"));
            response.setLoaiKho(rs.getString("loai_kho"));
            response.setMaChiNhanh(readNullableLong(rs.getObject("ma_chi_nhanh")));
            response.setMaNguyenLieu(rs.getLong("ma_nguyen_lieu"));
            response.setTenNguyenLieu(rs.getString("ten_nguyen_lieu"));
            response.setTenDonViTinh(rs.getString("ten_don_vi_tinh"));
            response.setKyHieu(rs.getString("ky_hieu"));
            response.setSoLuongTon(rs.getBigDecimal("so_luong_ton"));
            response.setMucTonToiThieu(rs.getBigDecimal("muc_ton_toi_thieu"));
            response.setTrangThaiTonKho(rs.getString("trang_thai_ton_kho"));
            response.setLanCapNhatCuoi(toLocalDateTime(rs.getTimestamp("lan_cap_nhat_cuoi")));
            return response;
        }, params.toArray());
    }

    public long countStock(
            Long maKho,
            Long maNguyenLieu,
            String tuKhoa,
            String trangThaiTonKho,
            Long maChiNhanh
    ) {
        StockQuery stockQuery = buildStockQuery(maKho, maNguyenLieu, tuKhoa, trangThaiTonKho, maChiNhanh);
        String sql = """
                SELECT COUNT(*)
                FROM TONKHO tk
                JOIN KHO k ON k.ma_kho = tk.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = tk.ma_nguyen_lieu
                LEFT JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                """ + stockQuery.whereSql();

        Long total = jdbcTemplate.queryForObject(sql, Long.class, stockQuery.params().toArray());
        return total == null ? 0 : total;
    }

    public List<BatchInventoryResponse> findBatchStock(Long maKho, Long maNguyenLieu, Long maChiNhanh) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("""
                WHERE lh.so_luong_con_lai > 0
                """);

        if (maKho != null) {
            where.append(" AND k.ma_kho = ?\n");
            params.add(maKho);
        }
        if (maNguyenLieu != null) {
            where.append(" AND nl.ma_nguyen_lieu = ?\n");
            params.add(maNguyenLieu);
        }
        if (maChiNhanh != null) {
            where.append(" AND k.ma_chi_nhanh = ?\n");
            params.add(maChiNhanh);
        }

        String sql = """
                SELECT
                    lh.ma_lo_hang,
                    k.ma_kho,
                    k.ten_kho,
                    nl.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu,
                    lh.so_luong_con_lai,
                    lh.trang_thai,
                    lh.han_su_dung,
                    lh.ngay_tao
                FROM LOHANG_NGUYENLIEU lh
                JOIN KHO k ON k.ma_kho = lh.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = lh.ma_nguyen_lieu
                LEFT JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                """ + where + """
                ORDER BY
                    CASE WHEN k.loai_kho = 'CENTRAL' THEN 0 ELSE 1 END,
                    k.ten_kho,
                    nl.ten_nguyen_lieu,
                    CASE WHEN lh.han_su_dung IS NULL THEN 1 ELSE 0 END,
                    lh.han_su_dung,
                    lh.ngay_tao,
                    lh.ma_lo_hang
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BatchInventoryResponse response = new BatchInventoryResponse();
            response.setMaLoHang(rs.getLong("ma_lo_hang"));
            response.setMaKho(rs.getLong("ma_kho"));
            response.setTenKho(rs.getString("ten_kho"));
            response.setMaNguyenLieu(rs.getLong("ma_nguyen_lieu"));
            response.setTenNguyenLieu(rs.getString("ten_nguyen_lieu"));
            response.setKyHieu(rs.getString("ky_hieu"));
            response.setSoLuongConLai(rs.getBigDecimal("so_luong_con_lai"));
            response.setTrangThai(rs.getString("trang_thai"));
            Date hanSuDung = rs.getDate("han_su_dung");
            response.setHanSuDung(hanSuDung == null ? null : hanSuDung.toLocalDate());
            response.setNgayTao(toLocalDateTime(rs.getTimestamp("ngay_tao")));
            return response;
        }, params.toArray());
    }

    public TongQuanTonKhoResponse findStockSummary(Long maKho, Long maChiNhanh) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE 1 = 1\n");

        if (maKho != null) {
            where.append(" AND k.ma_kho = ?\n");
            params.add(maKho);
        }
        if (maChiNhanh != null) {
            where.append(" AND k.ma_chi_nhanh = ?\n");
            params.add(maChiNhanh);
        }

        String sql = """
                SELECT
                    COUNT(*) AS tong_so_nguyen_lieu,
                    NVL(SUM(tk.so_luong_ton), 0) AS tong_so_luong_ton,
                    SUM(CASE WHEN tk.so_luong_ton = 0 THEN 1 ELSE 0 END) AS so_nguyen_lieu_het_hang,
                    SUM(CASE WHEN tk.so_luong_ton > 0 AND tk.so_luong_ton <= nl.muc_ton_toi_thieu THEN 1 ELSE 0 END) AS so_nguyen_lieu_ton_thap,
                    SUM(CASE WHEN tk.so_luong_ton > nl.muc_ton_toi_thieu THEN 1 ELSE 0 END) AS so_nguyen_lieu_on_dinh
                FROM TONKHO tk
                JOIN KHO k ON k.ma_kho = tk.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = tk.ma_nguyen_lieu
                """ + where;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            TongQuanTonKhoResponse response = new TongQuanTonKhoResponse();
            response.setTongSoNguyenLieu(rs.getLong("tong_so_nguyen_lieu"));
            response.setTongSoLuongTon(rs.getBigDecimal("tong_so_luong_ton"));
            response.setSoNguyenLieuHetHang(rs.getLong("so_nguyen_lieu_het_hang"));
            response.setSoNguyenLieuTonThap(rs.getLong("so_nguyen_lieu_ton_thap"));
            response.setSoNguyenLieuOnDinh(rs.getLong("so_nguyen_lieu_on_dinh"));
            return response;
        }, params.toArray());
    }

    private StockQuery buildStockQuery(
            Long maKho,
            Long maNguyenLieu,
            String tuKhoa,
            String trangThaiTonKho,
            Long maChiNhanh
    ) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE 1 = 1\n");

        if (maKho != null) {
            where.append(" AND k.ma_kho = ?\n");
            params.add(maKho);
        }
        if (maNguyenLieu != null) {
            where.append(" AND nl.ma_nguyen_lieu = ?\n");
            params.add(maNguyenLieu);
        }
        if (tuKhoa != null) {
            where.append("""
                     AND (
                        LOWER(nl.ten_nguyen_lieu) LIKE '%' || LOWER(?) || '%'
                        OR LOWER(k.ten_kho) LIKE '%' || LOWER(?) || '%'
                     )
                    """);
            params.add(tuKhoa);
            params.add(tuKhoa);
        }
        if (trangThaiTonKho != null) {
            where.append("""
                     AND CASE
                            WHEN tk.so_luong_ton = 0 THEN 'HET_HANG'
                            WHEN tk.so_luong_ton <= nl.muc_ton_toi_thieu THEN 'TON_THAP'
                            ELSE 'ON_DINH'
                         END = ?
                    """);
            params.add(trangThaiTonKho);
        }
        if (maChiNhanh != null) {
            where.append(" AND k.ma_chi_nhanh = ?\n");
            params.add(maChiNhanh);
        }

        return new StockQuery(where.toString(), params);
    }

    private LotRecord mapLotRecord(ResultSet rs) throws SQLException {
        Date hanSuDung = rs.getDate("han_su_dung");
        Timestamp ngayTao = rs.getTimestamp("ngay_tao");
        return new LotRecord(
                rs.getLong("ma_lo_hang"),
                rs.getLong("ma_kho"),
                rs.getLong("ma_nguyen_lieu"),
                rs.getBigDecimal("so_luong_con_lai"),
                hanSuDung == null ? null : hanSuDung.toLocalDate(),
                ngayTao == null ? null : ngayTao.toLocalDateTime()
        );
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private Long readNullableLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }

    public record WarehouseRecord(Long maKho, String tenKho, String loaiKho, Long maChiNhanh) {
    }

    public record LotRecord(
            Long maLoHang,
            Long maKho,
            Long maNguyenLieu,
            BigDecimal soLuongConLai,
            LocalDate hanSuDung,
            LocalDateTime ngayTao
    ) {
    }

    private record StockQuery(String whereSql, List<Object> params) {
    }
}
