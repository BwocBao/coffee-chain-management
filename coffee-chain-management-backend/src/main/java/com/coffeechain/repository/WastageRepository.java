package com.coffeechain.repository;

import com.coffeechain.dto.response.WastageLookupResponse;
import com.coffeechain.dto.response.WastageLotResponse;
import com.coffeechain.dto.response.WastageResponse;
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
public class WastageRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert wastageInsert;

  public WastageRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
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

  private final RowMapper<WastageResponse> wastageMapper =
      (rs, rowNum) -> {
        Long maLoHang = rs.getObject("ma_lo_hang") == null ? null : rs.getLong("ma_lo_hang");
        Long maNguoiBaoCao =
            rs.getObject("nguoi_bao_cao") == null ? null : rs.getLong("nguoi_bao_cao");

        Timestamp ts = rs.getTimestamp("ngay_hao_hut");
        LocalDateTime ngayHaoHut = ts == null ? null : ts.toLocalDateTime();

        return new WastageResponse(
            rs.getLong("ma_phieu_hao_hut"),
            rs.getLong("ma_kho"),
            rs.getString("ten_kho"),
            rs.getLong("ma_nguyen_lieu"),
            rs.getString("ten_nguyen_lieu"),
            rs.getString("don_vi_tinh"),
            maLoHang,
            rs.getBigDecimal("so_luong_hao_hut"),
            rs.getString("loai_hao_hut"),
            ngayHaoHut,
            rs.getString("ghi_chu"),
            maNguoiBaoCao,
            rs.getString("ten_nguoi_bao_cao"));
      };

  private final RowMapper<WastageLotResponse> lotMapper =
      (rs, rowNum) -> {
        Date hsd = rs.getDate("han_su_dung");
        LocalDate hanSuDung = hsd == null ? null : hsd.toLocalDate();

        return new WastageLotResponse(
            rs.getLong("ma_lo_hang"),
            rs.getLong("ma_kho"),
            rs.getString("ten_kho"),
            rs.getLong("ma_nguyen_lieu"),
            rs.getString("ten_nguyen_lieu"),
            rs.getString("don_vi_tinh"),
            rs.getBigDecimal("so_luong_con_lai"),
            hanSuDung,
            rs.getString("trang_thai"));
      };

  public List<WastageResponse> searchWastages(
      Long maKho,
      Long maNguyenLieu,
      String loaiHaoHut,
      LocalDateTime fromDate,
      LocalDateTime toDate,
      String keyword) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    phh.ma_phieu_hao_hut,
                    phh.ma_kho,
                    k.ten_kho,
                    phh.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    phh.ma_lo_hang,
                    phh.so_luong_hao_hut,
                    phh.loai_hao_hut,
                    phh.ngay_hao_hut,
                    phh.ghi_chu,
                    phh.nguoi_bao_cao,
                    nd.ten_dang_nhap AS ten_nguoi_bao_cao
                FROM PHIEUHAOHUT phh
                JOIN KHO k ON k.ma_kho = phh.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = phh.ma_nguyen_lieu
                JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                LEFT JOIN NGUOIDUNG nd ON nd.ma_nguoi_dung = phh.nguoi_bao_cao
                WHERE 1 = 1
                """);

    List<Object> params = new ArrayList<>();

    if (maKho != null) {
      sql.append(" AND phh.ma_kho = ? ");
      params.add(maKho);
    }

    if (maNguyenLieu != null) {
      sql.append(" AND phh.ma_nguyen_lieu = ? ");
      params.add(maNguyenLieu);
    }

    if (loaiHaoHut != null && !loaiHaoHut.isBlank()) {
      sql.append(" AND phh.loai_hao_hut = ? ");
      params.add(loaiHaoHut);
    }

    if (fromDate != null) {
      sql.append(" AND phh.ngay_hao_hut >= ? ");
      params.add(Timestamp.valueOf(fromDate));
    }

    if (toDate != null) {
      sql.append(" AND phh.ngay_hao_hut <= ? ");
      params.add(Timestamp.valueOf(toDate));
    }

    if (keyword != null && !keyword.isBlank()) {
      sql.append(
          """
                    AND (
                        LOWER(k.ten_kho) LIKE ?
                        OR LOWER(nl.ten_nguyen_lieu) LIKE ?
                        OR LOWER(phh.loai_hao_hut) LIKE ?
                        OR LOWER(phh.ghi_chu) LIKE ?
                        OR LOWER(nd.ten_dang_nhap) LIKE ?
                    )
                    """);

      String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
      params.add(like);
      params.add(like);
      params.add(like);
      params.add(like);
      params.add(like);
    }

    sql.append(" ORDER BY phh.ngay_hao_hut DESC, phh.ma_phieu_hao_hut DESC ");

    return jdbcTemplate.query(sql.toString(), wastageMapper, params.toArray());
  }

  public Optional<WastageResponse> findById(Long id, Long forcedMaKho) {
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    phh.ma_phieu_hao_hut,
                    phh.ma_kho,
                    k.ten_kho,
                    phh.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    phh.ma_lo_hang,
                    phh.so_luong_hao_hut,
                    phh.loai_hao_hut,
                    phh.ngay_hao_hut,
                    phh.ghi_chu,
                    phh.nguoi_bao_cao,
                    nd.ten_dang_nhap AS ten_nguoi_bao_cao
                FROM PHIEUHAOHUT phh
                JOIN KHO k ON k.ma_kho = phh.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = phh.ma_nguyen_lieu
                JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                LEFT JOIN NGUOIDUNG nd ON nd.ma_nguoi_dung = phh.nguoi_bao_cao
                WHERE phh.ma_phieu_hao_hut = ?
                """);

    List<Object> params = new ArrayList<>();
    params.add(id);

    if (forcedMaKho != null) {
      sql.append(" AND phh.ma_kho = ? ");
      params.add(forcedMaKho);
    }

    List<WastageResponse> rows =
        jdbcTemplate.query(sql.toString(), wastageMapper, params.toArray());
    return rows.stream().findFirst();
  }

  public List<WastageLotResponse> findAvailableLots(Long maKho, Long maNguyenLieu) {
    String sql =
        """
                SELECT
                    lh.ma_lo_hang,
                    lh.ma_kho,
                    k.ten_kho,
                    lh.ma_nguyen_lieu,
                    nl.ten_nguyen_lieu,
                    dvt.ky_hieu AS don_vi_tinh,
                    lh.so_luong_con_lai,
                    lh.han_su_dung,
                    lh.trang_thai
                FROM LOHANG_NGUYENLIEU lh
                JOIN KHO k ON k.ma_kho = lh.ma_kho
                JOIN NGUYENLIEU nl ON nl.ma_nguyen_lieu = lh.ma_nguyen_lieu
                JOIN DONVITINH dvt ON dvt.ma_don_vi_tinh = nl.ma_don_vi_tinh
                WHERE lh.ma_kho = ?
                  AND lh.ma_nguyen_lieu = ?
                  AND lh.so_luong_con_lai > 0
                  AND lh.trang_thai IN ('ACTIVE', 'EXPIRED')
                ORDER BY lh.han_su_dung NULLS LAST, lh.ma_lo_hang
                """;

    return jdbcTemplate.query(sql, lotMapper, maKho, maNguyenLieu);
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

  public Long insertWastage(
      Long maKho,
      Long maNguyenLieu,
      Long maLoHang,
      BigDecimal soLuongHaoHut,
      String loaiHaoHut,
      String ghiChu,
      Long nguoiBaoCao) {
    Map<String, Object> params = new HashMap<>();
    params.put("ma_kho", maKho);
    params.put("ma_nguyen_lieu", maNguyenLieu);
    params.put("ma_lo_hang", maLoHang);
    params.put("so_luong_hao_hut", soLuongHaoHut);
    params.put("loai_hao_hut", loaiHaoHut);
    params.put("ghi_chu", ghiChu);
    params.put("nguoi_bao_cao", nguoiBaoCao);

    Number key = wastageInsert.executeAndReturnKey(params);
    return key.longValue();
  }

  public void updateLotAfterWastage(Long maLoHang, BigDecimal newQuantity) {
    String sql =
        """
                UPDATE LOHANG_NGUYENLIEU
                SET so_luong_con_lai = ?,
                    trang_thai = CASE
                        WHEN ? <= 0 THEN 'USED_UP'
                        WHEN han_su_dung IS NOT NULL AND han_su_dung < TRUNC(SYSDATE) THEN 'EXPIRED'
                        ELSE trang_thai
                    END
                WHERE ma_lo_hang = ?
                """;

    jdbcTemplate.update(sql, newQuantity, newQuantity, maLoHang);
  }

  public void updateStockAfterWastage(Long maTonKho, BigDecimal newQuantity) {
    String sql =
        """
                UPDATE TONKHO
                SET so_luong_ton = ?
                WHERE ma_ton_kho = ?
                """;

    jdbcTemplate.update(sql, newQuantity, maTonKho);
  }

  public void insertInventoryLog(
      Long maKho,
      Long maNguyenLieu,
      Long maLoHang,
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
                VALUES (?, ?, ?, 'WASTAGE', 'PHIEUHAOHUT', ?, ?, ?, ?, ?)
                """;

    jdbcTemplate.update(
        sql,
        maKho,
        maNguyenLieu,
        maLoHang,
        maChungTu,
        soLuongThayDoi,
        soLuongTruoc,
        soLuongSau,
        nguoiThaoTac);
  }

  public List<WastageLookupResponse.OptionDto> findWarehouseOptions(Long forcedMaKho) {
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
            new WastageLookupResponse.OptionDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description")),
        params.toArray());
  }

  public List<WastageLookupResponse.OptionDto> findIngredientOptions() {
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
            new WastageLookupResponse.OptionDto(
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
