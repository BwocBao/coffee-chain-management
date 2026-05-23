package com.coffeechain.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvoicePdfRepository {
  private final JdbcTemplate jdbcTemplate;

  public InvoicePdfRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<InvoiceHeaderRow> findInvoiceHeader(Long maHoaDon) {
    String sql =
        """
                SELECT
                    hd.ma_hoa_don,
                    hd.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    cn.dia_chi,
                    hd.ma_pos,
                    hd.ma_nguoi_dung,
                    nd.ten_dang_nhap,
                    hd.trang_thai_hoa_don,
                    hd.trang_thai_thanh_toan,
                    hd.phuong_thuc_thanh_toan,
                    hd.tong_thanh_toan,
                    hd.thoi_gian_tao_hoa_don,
                    hd.thoi_gian_thanh_toan
                FROM HOADON hd
                JOIN CHINHANH cn
                    ON cn.ma_chi_nhanh = hd.ma_chi_nhanh
                JOIN NGUOIDUNG nd
                    ON nd.ma_nguoi_dung = hd.ma_nguoi_dung
                WHERE hd.ma_hoa_don = ?
                """;

    List<InvoiceHeaderRow> rows =
        jdbcTemplate.query(
            sql,
            (rs, rowNum) ->
                new InvoiceHeaderRow(
                    rs.getLong("ma_hoa_don"),
                    rs.getLong("ma_chi_nhanh"),
                    rs.getString("ten_chi_nhanh"),
                    rs.getString("dia_chi"),
                    rs.getLong("ma_pos"),
                    rs.getLong("ma_nguoi_dung"),
                    rs.getString("ten_dang_nhap"),
                    rs.getString("trang_thai_hoa_don"),
                    rs.getString("trang_thai_thanh_toan"),
                    rs.getString("phuong_thuc_thanh_toan"),
                    rs.getBigDecimal("tong_thanh_toan"),
                    toLocalDateTime(rs.getTimestamp("thoi_gian_tao_hoa_don")),
                    toLocalDateTime(rs.getTimestamp("thoi_gian_thanh_toan"))),
            maHoaDon);

    return rows.stream().findFirst();
  }

  public List<InvoiceItemRow> findInvoiceItems(Long maHoaDon) {
    String sql =
        """
                SELECT
                    cthd.ma_ct_hoa_don,
                    cthd.ma_san_pham,
                    sp.ten_san_pham,
                    cthd.so_luong,
                    cthd.don_gia_ban,
                    cthd.thanh_tien_dong
                FROM CHITIETHOADON cthd
                JOIN SANPHAM sp
                    ON sp.ma_san_pham = cthd.ma_san_pham
                WHERE cthd.ma_hoa_don = ?
                ORDER BY cthd.ma_ct_hoa_don
                """;

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) ->
            new InvoiceItemRow(
                rs.getLong("ma_ct_hoa_don"),
                rs.getLong("ma_san_pham"),
                rs.getString("ten_san_pham"),
                rs.getInt("so_luong"),
                rs.getBigDecimal("don_gia_ban"),
                rs.getBigDecimal("thanh_tien_dong")),
        maHoaDon);
  }

  private LocalDateTime toLocalDateTime(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toLocalDateTime();
  }

  public record InvoiceHeaderRow(
      Long maHoaDon,
      Long maChiNhanh,
      String tenChiNhanh,
      String diaChi,
      Long maPos,
      Long maNguoiDung,
      String tenDangNhap,
      String trangThaiHoaDon,
      String trangThaiThanhToan,
      String phuongThucThanhToan,
      BigDecimal tongThanhToan,
      LocalDateTime thoiGianTaoHoaDon,
      LocalDateTime thoiGianThanhToan) {}

  public record InvoiceItemRow(
      Long maCtHoaDon,
      Long maSanPham,
      String tenSanPham,
      Integer soLuong,
      BigDecimal donGiaBan,
      BigDecimal thanhTienDong) {}
}
