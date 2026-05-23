package com.coffeechain.repository;

import com.coffeechain.dto.response.StocktakeItemResponse;
import com.coffeechain.dto.response.StocktakeLookupResponse;
import com.coffeechain.dto.response.StocktakeResponse;
import com.coffeechain.dto.response.StocktakeSystemStockResponse;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class StocktakeRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert stocktakeInsert;
  private final SimpleJdbcInsert wastageInsert;

  public StocktakeRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;

    this.stocktakeInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("PHIEUKIEMKHO")
            .usingGeneratedKeyColumns("ma_phieu_kiem_kho")
            .usingColumns("ma_kho", "nguoi_kiem", "trang_thai", "ghi_chu");

    this.wastageInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("PHIEUHAOHUT")
            .usingGeneratedKeyColumns("ma_phieu_hao_hut")
            .usingColumns(
                "ma_kho",
                "ma_nguyen_lieu",
                "ma_lo_hang",
                "so_luong_hao_hut",
                "loai_hao_hut",
                "ghi_chu",
                "nguoi_bao_cao");
  }

  private final RowMapper<StocktakeResponse> stocktakeMapper =
      (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("ngay_kiem_kho");
        LocalDateTime ngayKiemKho = ts == null ? null : ts.toLocalDateTime();

        Long nguoiKiem = rs.getObject("nguoi_kiem") == null ? null : rs.getLong("nguoi_kiem");

        return new StocktakeResponse(
            rs.getLong("ma_phieu_kiem_kho"),
            rs.getLong("ma_kho"),
            rs.getString("ten_kho"),
            ngayKiemKho,
            nguoiKiem,
            rs.getString("ten_nguoi_kiem"),
            rs.getString("trang_thai"),
            rs.getString("ghi_chu"),
            rs.getInt("so_dong_chi_tiet"));
      };

  private final RowMapper<StocktakeItemResponse> itemMapper =
      (rs, rowNum) -> {
        Long maLoHang = rs.getObject("ma_lo_hang") == null ? null : rs.getLong("ma_lo_hang");

        return new StocktakeItemResponse(
            rs.getLong("ma_ct_phieu_kiem_kho"),
            rs.getLong("ma_phieu_kiem_kho"),
            rs.getLong("ma_nguyen_lieu"),
            rs.getString("ten_nguyen_lieu"),
            rs.getString("don_vi_tinh"),
            maLoHang,
            rs.getBigDecimal("so_luong_he_thong"),
            rs.getBigDecimal("so_luong_thuc_te"),
            rs.getBigDecimal("so_luong_chenh_lech"),
            rs.getBigDecimal("ty_le_chenh_lech"),
            rs.getString("ly_do_chenh_lech"),
            rs.getString("huong_xu_ly"));
      };

  private final RowMapper<StocktakeSystemStockResponse> systemStockMapper =
      (rs, rowNum) -> {
        Date hsd = rs.getDate("han_su_dung");
        LocalDate hanSuDung = hsd == null ? null : hsd.toLocalDate();

        return new StocktakeSystemStockResponse(
            rs.getLong("ma_lo_hang"),
            rs.getLong("ma_kho"),
            rs.getString("ten_kho"),
            rs.getLong("ma_nguyen_lieu"),
            rs.getString("ten_nguyen_lieu"),
            rs.getString("don_vi_tinh"),
            rs.getBigDecimal("so_luong_he_thong"),
            hanSuDung,
            rs.getString("trang_thai_lo"));
      };

  public Long insertHeader(Long maKho, Long nguoiKiem, String ghiChu) {
    Map<String, Object> params = new HashMap<>();
    params.put("ma_kho", maKho);
    params.put("nguoi_kiem", nguoiKiem);
    params.put("trang_thai", "DRAFT");
    params.put("ghi_chu", ghiChu);

    Number key = stocktakeInsert.executeAndReturnKey(params);
    return key.longValue();
  }

  public void updateHeader(Long maPhieuKiemKho, Long maKho, String ghiChu) {
    String sql =
        """
                UPDATE PHIEUKIEMKHO
                SET ma_kho = ?,
                    ghi_chu = ?
                WHERE ma_phieu_kiem_kho = ?
                """;

    jdbcTemplate.update(sql, maKho, ghiChu, maPhieuKiemKho);
  }

  public void updateStatus(Long maPhieuKiemKho, String status) {
    String sql =
        """
                UPDATE PHIEUKIEMKHO
                SET trang_thai = ?
                WHERE ma_phieu_kiem_kho = ?
                """;

    jdbcTemplate.update(sql, status, maPhieuKiemKho);
  }

  public void insertItem(
      Long maPhieuKiemKho,
      Long maNguyenLieu,
      Long maLoHang,
      BigDecimal soLuongHeThong,
      BigDecimal soLuongThucTe,
      BigDecimal soLuongChenhLech,
      BigDecimal tyLeChenhLech,
      String lyDoChenhLech,
      String huongXuLy) {
    String sql =
        """
                INSERT INTO CHITIETPHIEUKIEMKHO (
                    ma_phieu_kiem_kho,
                    ma_nguyen_lieu,
                    ma_lo_hang,
                    so_luong_he_thong,
                    so_luong_thuc_te,
                    so_luong_chenh_lech,
                    ty_le_chenh_lech,
                    ly_do_chenh_lech,
                    huong_xu_ly
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

    jdbcTemplate.update(
        sql,
        maPhieuKiemKho,
        maNguyenLieu,
        maLoHang,
        soLuongHeThong,
        soLuongThucTe,
        soLuongChenhLech,
        tyLeChenhLech,
        lyDoChenhLech,
        huongXuLy);
  }

  public void deleteItems(Long maPhieuKiemKho) {
    jdbcTemplate.update(
        "DELETE FROM CHITIETPHIEUKIEMKHO WHERE ma_phieu_kiem_kho = ?", maPhieuKiemKho);
  }

  public List<StocktakeResponse> searchStocktakes(
      Long maKho, String trangThai, LocalDateTime fromDate, LocalDateTime toDate, String keyword) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    pkk.ma_phieu_kiem_kho,
                    pkk.ma_kho,
                    k.ten_kho,
                    pkk.ngay_kiem_kho,
                    pkk.nguoi_kiem,
                    nd.ten_dang_nhap AS ten_nguoi_kiem,
                    pkk.trang_thai,
                    pkk.ghi_chu,
                    COUNT(ct.ma_ct_phieu_kiem_kho) AS so_dong_chi_tiet
                FROM PHIEUKIEMKHO pkk
                JOIN KHO k ON k.ma_kho = pkk.ma_kho
                LEFT JOIN NGUOIDUNG nd ON nd.ma_nguoi_dung = pkk.nguoi_kiem
                LEFT JOIN CHITIETPHIEUKIEMKHO ct
                    ON ct.ma_phieu_kiem_kho = pkk.ma_phieu_kiem_kho
                WHERE 1 = 1
                """);

    List<Object> params = new ArrayList<>();

    if (maKho != null) {
      sql.append(" AND pkk.ma_kho = ? ");
      params.add(maKho);
    }

    if (trangThai != null && !trangThai.isBlank()) {
      sql.append(" AND pkk.trang_thai = ? ");
      params.add(trangThai);
    }

    if (fromDate != null) {
      sql.append(" AND pkk.ngay_kiem_kho >= ? ");
      params.add(Timestamp.valueOf(fromDate));
    }

    if (toDate != null) {
      sql.append(" AND pkk.ngay_kiem_kho <= ? ");
      params.add(Timestamp.valueOf(toDate));
    }

    if (keyword != null && !keyword.isBlank()) {
      sql.append(
          """
                    AND (
                        LOWER(k.ten_kho) LIKE ?
                        OR LOWER(pkk.trang_thai) LIKE ?
                        OR LOWER(pkk.ghi_chu) LIKE ?
                        OR LOWER(nd.ten_dang_nhap) LIKE ?
                    )
                    """);

      String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
      params.add(like);
      params.add(like);
      params.add(like);
      params.add(like);
    }

    sql.append(
        """
                GROUP BY
                    pkk.ma_phieu_kiem_kho,
                    pkk.ma_kho,
                    k.ten_kho,
                    pkk.ngay_kiem_kho,
                    pkk.nguoi_kiem,
                    nd.ten_dang_nhap,
                    pkk.trang_thai,
                    pkk.ghi_chu
                ORDER BY pkk.ngay_kiem_kho DESC, pkk.ma_phieu_kiem_kho DESC
                """);

    return jdbcTemplate.query(sql.toString(), stocktakeMapper, params.toArray());
  }

  public Optional<StocktakeResponse> findById(Long id, Long forcedMaKho) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    pkk.ma_phieu_kiem_kho,
                    pkk.ma_kho,
                    k.ten_kho,
                    pkk.ngay_kiem_kho,
                    pkk.nguoi_kiem,
                    nd.ten_dang_nhap AS ten_nguoi_kiem,
                    pkk.trang_thai,
                    pkk.ghi_chu,
                    COUNT(ct.ma_ct_phieu_kiem_kho) AS so_dong_chi_tiet
                FROM PHIEUKIEMKHO pkk
                JOIN KHO k ON k.ma_kho = pkk.ma_kho
                LEFT JOIN NGUOIDUNG nd ON nd.ma_nguoi_dung = pkk.nguoi_kiem
                LEFT JOIN CHITIETPHIEUKIEMKHO ct
                    ON ct.ma_phieu_kiem_kho = pkk.ma_phieu_kiem_kho
                WHERE pkk.ma_phieu_kiem_kho = ?
                """);

    List<Object> params = new ArrayList<>();
    params.add(id);

    if (forcedMaKho != null) {
      sql.append(" AND pkk.ma_kho = ? ");
      params.add(forcedMaKho);
    }

    sql.append(
        """
                GROUP BY
                    pkk.ma_phieu_kiem_kho,
                    pkk.ma_kho,
                    k.ten_kho,
                    pkk.ngay_kiem_kho,
                    pkk.nguoi_kiem,
                    nd.ten_dang_nhap,
                    pkk.trang_thai,
                    pkk.ghi_chu
                """);

    List<StocktakeResponse> rows =
        jdbcTemplate.query(sql.toString(), stocktakeMapper, params.toArray());
    return rows.stream().findFirst();
  }

  public List<StocktakeItemResponse> findItems(Long maPhieuKiemKho) {
    String sql =
        """
                SELECT
                    ct.ma_ct_phieu_kiem_kho,
                    ct.ma_phieu_kiem_kho,
                    ct.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    ct.ma_lo_hang,
                    ct.so_luong_he_thong,
                    ct.so_luong_thuc_te,
                    ct.so_luong_chenh_lech,
                    ct.ty_le_chenh_lech,
                    ct.ly_do_chenh_lech,
                    ct.huong_xu_ly
                FROM CHITIETPHIEUKIEMKHO ct
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = ct.ma_nguyen_lieu
                JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE ct.ma_phieu_kiem_kho = ?
                ORDER BY ct.ma_ct_phieu_kiem_kho
                """;

    return jdbcTemplate.query(sql, itemMapper, maPhieuKiemKho);
  }

  public HeaderLock findHeaderForUpdate(Long id) {
    String sql =
        """
                SELECT
                    ma_phieu_kiem_kho,
                    ma_kho,
                    trang_thai
                FROM PHIEUKIEMKHO
                WHERE ma_phieu_kiem_kho = ?
                FOR UPDATE
                """;

    List<HeaderLock> rows =
        jdbcTemplate.query(
            sql,
            (rs, rowNum) ->
                new HeaderLock(
                    rs.getLong("ma_phieu_kiem_kho"),
                    rs.getLong("ma_kho"),
                    rs.getString("trang_thai")),
            id);

    return rows.isEmpty() ? null : rows.get(0);
  }

  public List<StocktakeSystemStockResponse> findSystemStock(Long maKho, Long maNguyenLieu) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    lh.ma_lo_hang,
                    lh.ma_kho,
                    k.ten_kho,
                    lh.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    lh.so_luong_con_lai AS so_luong_he_thong,
                    lh.han_su_dung,
                    lh.trang_thai AS trang_thai_lo
                FROM LOHANG_NGUYENLIEU lh
                JOIN KHO k ON k.ma_kho = lh.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = lh.ma_nguyen_lieu
                JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE lh.ma_kho = ?
                  AND lh.so_luong_con_lai > 0
                  AND lh.trang_thai IN ('ACTIVE', 'EXPIRED')
                """);

    List<Object> params = new ArrayList<>();
    params.add(maKho);

    if (maNguyenLieu != null) {
      sql.append(" AND lh.ma_nguyen_lieu = ? ");
      params.add(maNguyenLieu);
    }

    sql.append(" ORDER BY nl.ten_nguyen_lieu, lh.han_su_dung NULLS LAST, lh.ma_lo_hang ");

    return jdbcTemplate.query(sql.toString(), systemStockMapper, params.toArray());
  }

  public LotLock findLotForUpdate(Long maLoHang) {
    String sql =
        """
                SELECT
                    ma_lo_hang,
                    ma_kho,
                    ma_nguyen_lieu,
                    so_luong_con_lai,
                    trang_thai,
                    han_su_dung
                FROM LOHANG_NGUYENLIEU
                WHERE ma_lo_hang = ?
                FOR UPDATE
                """;

    List<LotLock> rows =
        jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
              Date hsd = rs.getDate("han_su_dung");
              LocalDate hanSuDung = hsd == null ? null : hsd.toLocalDate();

              return new LotLock(
                  rs.getLong("ma_lo_hang"),
                  rs.getLong("ma_kho"),
                  rs.getLong("ma_nguyen_lieu"),
                  rs.getBigDecimal("so_luong_con_lai"),
                  rs.getString("trang_thai"),
                  hanSuDung);
            },
            maLoHang);

    return rows.isEmpty() ? null : rows.get(0);
  }

  public StockLock findStockForUpdate(Long maKho, Long maNguyenLieu) {
    String sql =
        """
                SELECT
                    ma_ton_kho,
                    ma_kho,
                    ma_nguyen_lieu,
                    so_luong_ton
                FROM TONKHO
                WHERE ma_kho = ?
                  AND ma_nguyen_lieu = ?
                FOR UPDATE
                """;

    List<StockLock> rows =
        jdbcTemplate.query(
            sql,
            (rs, rowNum) ->
                new StockLock(
                    rs.getLong("ma_ton_kho"),
                    rs.getLong("ma_kho"),
                    rs.getLong("ma_nguyen_lieu"),
                    rs.getBigDecimal("so_luong_ton")),
            maKho,
            maNguyenLieu);

    return rows.isEmpty() ? null : rows.get(0);
  }

  public void updateLotQuantity(Long maLoHang, BigDecimal newQuantity) {
    String sql =
        """
                UPDATE LOHANG_NGUYENLIEU
                SET so_luong_con_lai = ?,
                    trang_thai = CASE
                        WHEN ? <= 0 THEN 'USED_UP'
                        WHEN han_su_dung IS NOT NULL AND han_su_dung < TRUNC(SYSDATE) THEN 'EXPIRED'
                        ELSE 'ACTIVE'
                    END
                WHERE ma_lo_hang = ?
                """;

    jdbcTemplate.update(sql, newQuantity, newQuantity, maLoHang);
  }

  public void updateStockQuantity(Long maTonKho, BigDecimal newQuantity) {
    jdbcTemplate.update(
        "UPDATE TONKHO SET so_luong_ton = ? WHERE ma_ton_kho = ?", newQuantity, maTonKho);
  }

  public void insertInventoryLog(
      Long maKho,
      Long maNguyenLieu,
      Long maLoHang,
      String loaiGiaoDich,
      String tenChungTu,
      Long maChungTu,
      BigDecimal soLuongThayDoi,
      BigDecimal soLuongTruoc,
      BigDecimal soLuongSau,
      Long nguoiThaoTac) {
    String sql =
        """
                INSERT INTO NHATKY_KHO (
                    ma_kho,
                    ma_nguyen_lieu,
                    ma_lo_hang,
                    loai_giao_dich,
                    ten_chung_tu,
                    ma_chung_tu,
                    so_luong_thay_doi,
                    so_luong_truoc,
                    so_luong_sau,
                    nguoi_thao_tac
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

    jdbcTemplate.update(
        sql,
        maKho,
        maNguyenLieu,
        maLoHang,
        loaiGiaoDich,
        tenChungTu,
        maChungTu,
        soLuongThayDoi,
        soLuongTruoc,
        soLuongSau,
        nguoiThaoTac);
  }

  public Long insertWastageFromStocktake(
      Long maKho,
      Long maNguyenLieu,
      Long maLoHang,
      BigDecimal soLuongHaoHut,
      String ghiChu,
      Long nguoiBaoCao) {
    Map<String, Object> params = new HashMap<>();
    params.put("ma_kho", maKho);
    params.put("ma_nguyen_lieu", maNguyenLieu);
    params.put("ma_lo_hang", maLoHang);
    params.put("so_luong_hao_hut", soLuongHaoHut);
    params.put("loai_hao_hut", "LOST");
    params.put("ghi_chu", ghiChu);
    params.put("nguoi_bao_cao", nguoiBaoCao);

    Number key = wastageInsert.executeAndReturnKey(params);
    return key.longValue();
  }

  public List<StocktakeLookupResponse.OptionDto> findWarehouseOptions(Long forcedMaKho) {
    StringBuilder sql =
        new StringBuilder(
            """
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

    return jdbcTemplate.query(
        sql.toString(),
        (rs, rowNum) ->
            new StocktakeLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description")),
        params.toArray());
  }

  public List<StocktakeLookupResponse.OptionDto> findIngredientOptions() {
    String sql =
        """
                SELECT
                    nl.ma_nguyen_lieu AS id,
                    nl.trang_thai AS code,
                    nl.ten_nguyen_lieu AS name,
                    dvt.ky_hieu AS description
                FROM NGUYENLIEU nl
                JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE nl.trang_thai = 'ACTIVE'
                ORDER BY nl.ten_nguyen_lieu
                """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new StocktakeLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description")));
  }

  public Long findActiveWarehouseIdByBranchId(Long maChiNhanh) {
    String sql =
        """
                SELECT ma_kho
                FROM KHO
                WHERE ma_chi_nhanh = ?
                  AND trang_thai = 'ACTIVE'
                """;

    List<Long> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("ma_kho"), maChiNhanh);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public static class HeaderLock {
    private final Long maPhieuKiemKho;
    private final Long maKho;
    private final String trangThai;

    public HeaderLock(Long maPhieuKiemKho, Long maKho, String trangThai) {
      this.maPhieuKiemKho = maPhieuKiemKho;
      this.maKho = maKho;
      this.trangThai = trangThai;
    }

    public Long getMaPhieuKiemKho() {
      return maPhieuKiemKho;
    }

    public Long getMaKho() {
      return maKho;
    }

    public String getTrangThai() {
      return trangThai;
    }
  }

  public static class LotLock {
    private final Long maLoHang;
    private final Long maKho;
    private final Long maNguyenLieu;
    private final BigDecimal soLuongConLai;
    private final String trangThai;
    private final LocalDate hanSuDung;

    public LotLock(
        Long maLoHang,
        Long maKho,
        Long maNguyenLieu,
        BigDecimal soLuongConLai,
        String trangThai,
        LocalDate hanSuDung) {
      this.maLoHang = maLoHang;
      this.maKho = maKho;
      this.maNguyenLieu = maNguyenLieu;
      this.soLuongConLai = soLuongConLai;
      this.trangThai = trangThai;
      this.hanSuDung = hanSuDung;
    }

    public Long getMaLoHang() {
      return maLoHang;
    }

    public Long getMaKho() {
      return maKho;
    }

    public Long getMaNguyenLieu() {
      return maNguyenLieu;
    }

    public BigDecimal getSoLuongConLai() {
      return soLuongConLai;
    }

    public String getTrangThai() {
      return trangThai;
    }

    public LocalDate getHanSuDung() {
      return hanSuDung;
    }
  }

  public static class StockLock {
    private final Long maTonKho;
    private final Long maKho;
    private final Long maNguyenLieu;
    private final BigDecimal soLuongTon;

    public StockLock(Long maTonKho, Long maKho, Long maNguyenLieu, BigDecimal soLuongTon) {
      this.maTonKho = maTonKho;
      this.maKho = maKho;
      this.maNguyenLieu = maNguyenLieu;
      this.soLuongTon = soLuongTon;
    }

    public Long getMaTonKho() {
      return maTonKho;
    }

    public Long getMaKho() {
      return maKho;
    }

    public Long getMaNguyenLieu() {
      return maNguyenLieu;
    }

    public BigDecimal getSoLuongTon() {
      return soLuongTon;
    }
  }
}
