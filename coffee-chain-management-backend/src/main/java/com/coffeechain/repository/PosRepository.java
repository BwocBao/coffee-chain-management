package com.coffeechain.repository;

import com.coffeechain.dto.request.PosOrderItemRequest;
import com.coffeechain.dto.response.PosOrderItemResponse;
import com.coffeechain.dto.response.PosLookupResponse;
import com.coffeechain.dto.response.PosOrderSummaryResponse;
import com.coffeechain.dto.response.PosPaymentResponse;
import com.coffeechain.dto.response.PosProductResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class PosRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert orderInsert;
  private final SimpleJdbcInsert orderDetailInsert;
  private final SimpleJdbcInsert saleDeductionInsert;
  private final SimpleJdbcInsert payOsPaymentInsert;

  public PosRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;

    this.orderInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("HOADON")
            .usingColumns(
                "MA_CHI_NHANH",
                "MA_POS",
                "MA_NGUOI_DUNG",
                "TRANG_THAI_HOA_DON",
                "TONG_THANH_TOAN",
                "TRANG_THAI_THANH_TOAN")
            .usingGeneratedKeyColumns("MA_HOA_DON");

    this.orderDetailInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("CHITIETHOADON")
            .usingColumns("MA_HOA_DON", "MA_SAN_PHAM", "SO_LUONG", "DON_GIA_BAN")
            .usingGeneratedKeyColumns("MA_CT_HOA_DON");

    this.saleDeductionInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("BANHANG_TRUKHO")
            .usingColumns(
                "MA_HOA_DON",
                "MA_CT_HOA_DON",
                "MA_KHO",
                "MA_NGUYEN_LIEU",
                "MA_LO_HANG",
                "SO_LUONG_NGUYEN_LIEU_MOI_SP",
                "TONG_SO_LUONG_TRU",
                "TRANG_THAI")
            .usingGeneratedKeyColumns("MA_BAN_HANG_TRU_KHO");

    this.payOsPaymentInsert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("THANHTOAN_PAYOS")
            .usingColumns(
                "MA_HOA_DON",
                "ORDER_CODE",
                "PAYMENT_LINK_ID",
                "CHECKOUT_URL",
                "QR_CODE",
                "SO_TIEN",
                "MO_TA",
                "TRANG_THAI")
            .usingGeneratedKeyColumns("MA_THANH_TOAN");
  }


  public List<PosLookupResponse.OptionDto> findBranchOptions(Long forcedMaChiNhanh) {
    List<Object> params = new ArrayList<>();
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT ma_chi_nhanh, ten_chi_nhanh, so_dien_thoai
                FROM CHINHANH
                WHERE trang_thai = 'ACTIVE'
                """);
    if (forcedMaChiNhanh != null) {
      sql.append(" AND ma_chi_nhanh = ?");
      params.add(forcedMaChiNhanh);
    }
    sql.append(" ORDER BY ten_chi_nhanh");

    return jdbcTemplate.query(
        sql.toString(),
        (rs, rowNum) ->
            new PosLookupResponse.OptionDto(
                rs.getLong("ma_chi_nhanh"),
                rs.getString("so_dien_thoai"),
                rs.getString("ten_chi_nhanh"),
                null),
        params.toArray());
  }

  public List<PosLookupResponse.OptionDto> findPosOptions(Long forcedMaChiNhanh) {
    List<Object> params = new ArrayList<>();
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT p.ma_pos, p.ma_thiet_bi, p.trang_thai_thiet_bi, cn.ma_chi_nhanh, cn.ten_chi_nhanh
                FROM MAYPOS p
                JOIN CHINHANH cn ON cn.ma_chi_nhanh = p.ma_chi_nhanh
                WHERE p.trang_thai_thiet_bi <> 'DISABLED'
                  AND cn.trang_thai = 'ACTIVE'
                """);
    if (forcedMaChiNhanh != null) {
      sql.append(" AND p.ma_chi_nhanh = ?");
      params.add(forcedMaChiNhanh);
    }
    sql.append(" ORDER BY cn.ten_chi_nhanh, p.ma_pos");

    return jdbcTemplate.query(
        sql.toString(),
        (rs, rowNum) ->
            new PosLookupResponse.OptionDto(
                rs.getLong("ma_pos"),
                String.valueOf(rs.getLong("ma_chi_nhanh")),
                rs.getString("ma_thiet_bi"),
                rs.getString("ten_chi_nhanh") + " - " + rs.getString("trang_thai_thiet_bi")),
        params.toArray());
  }
  public List<PosProductResponse> findPosProducts() {
    return jdbcTemplate.query(
        """
                SELECT
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    sp.hinh_anh,
                    sp.gia_ban_hien_tai
                FROM SANPHAM sp
                WHERE sp.trang_thai = 'AVAILABLE'
                  AND sp.gia_ban_hien_tai IS NOT NULL
                  AND EXISTS (
                      SELECT 1
                      FROM CONGTHUC_SANPHAM ct
                      WHERE ct.ma_san_pham = sp.ma_san_pham
                  )
                ORDER BY sp.ten_san_pham
                """,
        (rs, rowNum) ->
            new PosProductResponse(
                rs.getLong("ma_san_pham"),
                rs.getString("ten_san_pham"),
                rs.getString("hinh_anh"),
                rs.getBigDecimal("gia_ban_hien_tai")));
  }

  public ProductOrderRow findProductForOrder(Long maSanPham) {
    List<ProductOrderRow> rows =
        jdbcTemplate.query(
            """
                SELECT
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    sp.gia_ban_hien_tai
                FROM SANPHAM sp
                WHERE sp.ma_san_pham = ?
                  AND sp.trang_thai = 'AVAILABLE'
                  AND sp.gia_ban_hien_tai IS NOT NULL
                  AND EXISTS (
                      SELECT 1
                      FROM CONGTHUC_SANPHAM ct
                      WHERE ct.ma_san_pham = sp.ma_san_pham
                  )
                """,
            (rs, rowNum) ->
                new ProductOrderRow(
                    rs.getLong("ma_san_pham"),
                    rs.getString("ten_san_pham"),
                    rs.getBigDecimal("gia_ban_hien_tai")),
            maSanPham);

    return rows.isEmpty() ? null : rows.get(0);
  }

  public boolean branchExists(Long maChiNhanh) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM CHINHANH
                WHERE ma_chi_nhanh = ?
                  AND trang_thai = 'ACTIVE'
                """,
            Integer.class,
            maChiNhanh);

    return count != null && count > 0;
  }

  public boolean posBelongsToBranch(Long maPos, Long maChiNhanh) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM MAYPOS
                WHERE ma_pos = ?
                  AND ma_chi_nhanh = ?
                  AND trang_thai_thiet_bi <> 'DISABLED'
                """,
            Integer.class,
            maPos,
            maChiNhanh);

    return count != null && count > 0;
  }

  public Long createOrder(Long maChiNhanh, Long maPos, Long maNguoiDung, BigDecimal total) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("MA_CHI_NHANH", maChiNhanh);
    params.put("MA_POS", maPos);
    params.put("MA_NGUOI_DUNG", maNguoiDung);
    params.put("TRANG_THAI_HOA_DON", "PENDING");
    params.put("TONG_THANH_TOAN", total);
    params.put("TRANG_THAI_THANH_TOAN", "PENDING");

    return orderInsert.executeAndReturnKey(params).longValue();
  }

  public Long createOrderDetail(Long maHoaDon, PosOrderItemRequest item, BigDecimal donGiaBan) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("MA_HOA_DON", maHoaDon);
    params.put("MA_SAN_PHAM", item.maSanPham());
    params.put("SO_LUONG", item.soLuong());
    params.put("DON_GIA_BAN", donGiaBan);

    return orderDetailInsert.executeAndReturnKey(params).longValue();
  }


  public List<PosOrderSummaryResponse> searchOrders(
      Long forcedMaChiNhanh, Long maChiNhanh, String keyword, String status, int limit) {
    List<Object> params = new ArrayList<>();
    StringBuilder sql =
        new StringBuilder(
            """
                SELECT
                    hd.ma_hoa_don,
                    hd.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    hd.ma_pos,
                    hd.trang_thai_hoa_don,
                    hd.trang_thai_thanh_toan,
                    hd.phuong_thuc_thanh_toan,
                    hd.tong_thanh_toan,
                    hd.thoi_gian_tao_hoa_don,
                    COUNT(ct.ma_ct_hoa_don) AS so_dong
                FROM HOADON hd
                JOIN CHINHANH cn ON cn.ma_chi_nhanh = hd.ma_chi_nhanh
                LEFT JOIN CHITIETHOADON ct ON ct.ma_hoa_don = hd.ma_hoa_don
                WHERE 1 = 1
                """);

    if (forcedMaChiNhanh != null) {
      sql.append(" AND hd.ma_chi_nhanh = ?");
      params.add(forcedMaChiNhanh);
    } else if (maChiNhanh != null) {
      sql.append(" AND hd.ma_chi_nhanh = ?");
      params.add(maChiNhanh);
    }

    if (keyword != null && !keyword.trim().isEmpty()) {
      sql.append(" AND (TO_CHAR(hd.ma_hoa_don) LIKE ? OR LOWER(cn.ten_chi_nhanh) LIKE ?)");
      String text = "%" + keyword.trim().toLowerCase() + "%";
      params.add(text);
      params.add(text);
    }

    if (status != null && !status.trim().isEmpty()) {
      sql.append(" AND hd.trang_thai_hoa_don = ?");
      params.add(status.trim().toUpperCase());
    }

    sql.append(
        """
                GROUP BY
                    hd.ma_hoa_don, hd.ma_chi_nhanh, cn.ten_chi_nhanh, hd.ma_pos,
                    hd.trang_thai_hoa_don, hd.trang_thai_thanh_toan,
                    hd.phuong_thuc_thanh_toan, hd.tong_thanh_toan, hd.thoi_gian_tao_hoa_don
                ORDER BY hd.thoi_gian_tao_hoa_don DESC, hd.ma_hoa_don DESC
                FETCH FIRST ? ROWS ONLY
                """);
    params.add(Math.max(1, Math.min(limit, 200)));

    return jdbcTemplate.query(
        sql.toString(),
        (rs, rowNum) ->
            new PosOrderSummaryResponse(
                rs.getLong("ma_hoa_don"),
                rs.getLong("ma_chi_nhanh"),
                rs.getString("ten_chi_nhanh"),
                rs.getLong("ma_pos"),
                rs.getString("trang_thai_hoa_don"),
                rs.getString("trang_thai_thanh_toan"),
                rs.getString("phuong_thuc_thanh_toan"),
                rs.getBigDecimal("tong_thanh_toan"),
                toLocalDateTime(rs.getTimestamp("thoi_gian_tao_hoa_don")),
                rs.getInt("so_dong")),
        params.toArray());
  }
  public Optional<OrderRow> findOrder(Long maHoaDon) {
    List<OrderRow> rows =
        jdbcTemplate.query(
            """
                SELECT
                    hd.ma_hoa_don,
                    hd.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    hd.ma_pos,
                    hd.ma_nguoi_dung,
                    hd.trang_thai_hoa_don,
                    hd.trang_thai_thanh_toan,
                    hd.phuong_thuc_thanh_toan,
                    hd.tong_thanh_toan,
                    hd.thoi_gian_tao_hoa_don,
                    hd.thoi_gian_thanh_toan
                FROM HOADON hd
                JOIN CHINHANH cn ON cn.ma_chi_nhanh = hd.ma_chi_nhanh
                WHERE hd.ma_hoa_don = ?
                """,
            (rs, rowNum) -> mapOrderRow(rs),
            maHoaDon);

    return rows.stream().findFirst();
  }

  public Optional<OrderRow> lockOrder(Long maHoaDon) {
    List<OrderRow> rows =
        jdbcTemplate.query(
            """
                SELECT
                    hd.ma_hoa_don,
                    hd.ma_chi_nhanh,
                    cn.ten_chi_nhanh,
                    hd.ma_pos,
                    hd.ma_nguoi_dung,
                    hd.trang_thai_hoa_don,
                    hd.trang_thai_thanh_toan,
                    hd.phuong_thuc_thanh_toan,
                    hd.tong_thanh_toan,
                    hd.thoi_gian_tao_hoa_don,
                    hd.thoi_gian_thanh_toan
                FROM HOADON hd
                JOIN CHINHANH cn ON cn.ma_chi_nhanh = hd.ma_chi_nhanh
                WHERE hd.ma_hoa_don = ?
                FOR UPDATE
                """,
            (rs, rowNum) -> mapOrderRow(rs),
            maHoaDon);

    return rows.stream().findFirst();
  }

  public List<PosOrderItemResponse> findOrderItems(Long maHoaDon) {
    return jdbcTemplate.query(
        """
                SELECT
                    cthd.ma_ct_hoa_don,
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    cthd.so_luong,
                    cthd.don_gia_ban,
                    cthd.thanh_tien_dong
                FROM CHITIETHOADON cthd
                JOIN SANPHAM sp ON sp.ma_san_pham = cthd.ma_san_pham
                WHERE cthd.ma_hoa_don = ?
                ORDER BY cthd.ma_ct_hoa_don
                """,
        (rs, rowNum) ->
            new PosOrderItemResponse(
                rs.getLong("ma_ct_hoa_don"),
                rs.getLong("ma_san_pham"),
                rs.getString("ten_san_pham"),
                rs.getInt("so_luong"),
                rs.getBigDecimal("don_gia_ban"),
                rs.getBigDecimal("thanh_tien_dong")),
        maHoaDon);
  }

  public Optional<PosPaymentResponse> findLatestPayOsPayment(Long maHoaDon) {
    List<PosPaymentResponse> rows =
        jdbcTemplate.query(
            """
                SELECT
                    order_code,
                    so_tien,
                    mo_ta,
                    checkout_url,
                    qr_code,
                    trang_thai
                FROM THANHTOAN_PAYOS
                WHERE ma_hoa_don = ?
                ORDER BY ma_thanh_toan DESC
                FETCH FIRST 1 ROWS ONLY
                """,
            (rs, rowNum) ->
                new PosPaymentResponse(
                    rs.getLong("order_code"),
                    rs.getBigDecimal("so_tien"),
                    rs.getString("mo_ta"),
                    rs.getString("checkout_url"),
                    rs.getString("qr_code"),
                    rs.getString("trang_thai")),
            maHoaDon);

    return rows.stream().findFirst();
  }

  public void markOrderPaid(Long maHoaDon, String paymentMethod) {
    jdbcTemplate.update(
        """
                UPDATE HOADON
                SET phuong_thuc_thanh_toan = ?,
                    trang_thai_thanh_toan = 'SUCCESS'
                WHERE ma_hoa_don = ?
                """,
        paymentMethod,
        maHoaDon);
  }

  public void markOrderCompleted(Long maHoaDon) {
    jdbcTemplate.update(
        """
                UPDATE HOADON
                SET trang_thai_hoa_don = 'COMPLETED'
                WHERE ma_hoa_don = ?
                """,
        maHoaDon);
  }

  public Long findBranchWarehouse(Long maChiNhanh) {
    List<Long> rows =
        jdbcTemplate.query(
            """
                SELECT ma_kho
                FROM KHO
                WHERE ma_chi_nhanh = ?
                  AND loai_kho = 'BRANCH'
                  AND trang_thai = 'ACTIVE'
                """,
            (rs, rowNum) -> rs.getLong("ma_kho"),
            maChiNhanh);

    return rows.isEmpty() ? null : rows.get(0);
  }

  public List<OrderDetailForDeductionRow> findOrderDetailsForDeduction(Long maHoaDon) {
    return jdbcTemplate.query(
        """
                SELECT
                    ma_ct_hoa_don,
                    ma_san_pham,
                    so_luong
                FROM CHITIETHOADON
                WHERE ma_hoa_don = ?
                ORDER BY ma_ct_hoa_don
                """,
        (rs, rowNum) ->
            new OrderDetailForDeductionRow(
                rs.getLong("ma_ct_hoa_don"), rs.getLong("ma_san_pham"), rs.getInt("so_luong")),
        maHoaDon);
  }

  public List<RecipeLineRow> findRecipeLines(Long maSanPham) {
    return jdbcTemplate.query(
        """
                SELECT
                    ma_nguyen_lieu,
                    so_luong_can
                FROM CONGTHUC_SANPHAM
                WHERE ma_san_pham = ?
                ORDER BY ma_cong_thuc
                """,
        (rs, rowNum) ->
            new RecipeLineRow(rs.getLong("ma_nguyen_lieu"), rs.getBigDecimal("so_luong_can")),
        maSanPham);
  }

  public List<LotRow> lockLotsForSale(Long maKho, Long maNguyenLieu) {
    return jdbcTemplate.query(
        """
                SELECT
                    ma_lo_hang,
                    ma_kho,
                    ma_nguyen_lieu,
                    so_luong_con_lai
                FROM LOHANG_NGUYENLIEU
                WHERE ma_kho = ?
                  AND ma_nguyen_lieu = ?
                  AND trang_thai = 'ACTIVE'
                  AND so_luong_con_lai > 0
                  AND (
                      han_su_dung IS NULL
                      OR han_su_dung >= TRUNC(SYSDATE)
                  )
                ORDER BY
                    CASE WHEN han_su_dung IS NULL THEN 1 ELSE 0 END,
                    han_su_dung ASC,
                    ngay_tao ASC,
                    ma_lo_hang ASC
                FOR UPDATE
                """,
        (rs, rowNum) ->
            new LotRow(
                rs.getLong("ma_lo_hang"),
                rs.getLong("ma_kho"),
                rs.getLong("ma_nguyen_lieu"),
                rs.getBigDecimal("so_luong_con_lai")),
        maKho,
        maNguyenLieu);
  }

  public BigDecimal lockStock(Long maKho, Long maNguyenLieu) {
    List<BigDecimal> rows =
        jdbcTemplate.query(
            """
                SELECT so_luong_ton
                FROM TONKHO
                WHERE ma_kho = ?
                  AND ma_nguyen_lieu = ?
                FOR UPDATE
                """,
            (rs, rowNum) -> rs.getBigDecimal("so_luong_ton"),
            maKho,
            maNguyenLieu);

    return rows.isEmpty() ? BigDecimal.ZERO : rows.get(0);
  }

  public void decreaseLot(Long maLoHang, BigDecimal quantity) {
    jdbcTemplate.update(
        """
                UPDATE LOHANG_NGUYENLIEU
                SET so_luong_con_lai = so_luong_con_lai - ?
                WHERE ma_lo_hang = ?
                  AND so_luong_con_lai >= ?
                """,
        quantity,
        maLoHang,
        quantity);
  }

  public void decreaseStock(Long maKho, Long maNguyenLieu, BigDecimal quantity) {
    jdbcTemplate.update(
        """
                UPDATE TONKHO
                SET so_luong_ton = so_luong_ton - ?
                WHERE ma_kho = ?
                  AND ma_nguyen_lieu = ?
                  AND so_luong_ton >= ?
                """,
        quantity,
        maKho,
        maNguyenLieu,
        quantity);
  }

  public Long createSaleDeduction(
      Long maHoaDon,
      Long maCtHoaDon,
      Long maKho,
      Long maNguyenLieu,
      Long maLoHang,
      BigDecimal soLuongMoiSp,
      BigDecimal tongSoLuongTru) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("MA_HOA_DON", maHoaDon);
    params.put("MA_CT_HOA_DON", maCtHoaDon);
    params.put("MA_KHO", maKho);
    params.put("MA_NGUYEN_LIEU", maNguyenLieu);
    params.put("MA_LO_HANG", maLoHang);
    params.put("SO_LUONG_NGUYEN_LIEU_MOI_SP", soLuongMoiSp);
    params.put("TONG_SO_LUONG_TRU", tongSoLuongTru);
    params.put("TRANG_THAI", "DEDUCTED");

    return saleDeductionInsert.executeAndReturnKey(params).longValue();
  }

  public void createInventoryJournal(
      Long maKho,
      Long maNguyenLieu,
      Long maLoHang,
      Long maHoaDon,
      BigDecimal soLuongThayDoi,
      BigDecimal soLuongTruoc,
      BigDecimal soLuongSau,
      Long nguoiThaoTac) {
    jdbcTemplate.update(
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
                VALUES (?, ?, ?, 'SALE_DEDUCT', 'HOADON', ?, ?, ?, ?, ?)
                """,
        maKho,
        maNguyenLieu,
        maLoHang,
        maHoaDon,
        soLuongThayDoi,
        soLuongTruoc,
        soLuongSau,
        nguoiThaoTac);
  }

  public boolean hasSaleDeduction(Long maHoaDon) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM BANHANG_TRUKHO
                WHERE ma_hoa_don = ?
                  AND trang_thai = 'DEDUCTED'
                """,
            Integer.class,
            maHoaDon);

    return count != null && count > 0;
  }

  public Optional<PayOsPaymentRow> findPayOsPaymentForOrder(Long maHoaDon) {
    List<PayOsPaymentRow> rows =
        jdbcTemplate.query(
            """
                SELECT
                    ma_thanh_toan,
                    ma_hoa_don,
                    order_code,
                    payment_link_id,
                    checkout_url,
                    qr_code,
                    so_tien,
                    mo_ta,
                    trang_thai,
                    reference_code
                FROM THANHTOAN_PAYOS
                WHERE ma_hoa_don = ?
                ORDER BY ma_thanh_toan DESC
                FETCH FIRST 1 ROWS ONLY
                """,
            (rs, rowNum) -> mapPayOsPaymentRow(rs),
            maHoaDon);

    return rows.stream().findFirst();
  }

  public Optional<PayOsPaymentRow> findPayOsPaymentById(Long maThanhToan) {
    List<PayOsPaymentRow> rows =
        jdbcTemplate.query(
            """
                SELECT
                    ma_thanh_toan,
                    ma_hoa_don,
                    order_code,
                    payment_link_id,
                    checkout_url,
                    qr_code,
                    so_tien,
                    mo_ta,
                    trang_thai,
                    reference_code
                FROM THANHTOAN_PAYOS
                WHERE ma_thanh_toan = ?
                """,
            (rs, rowNum) -> mapPayOsPaymentRow(rs),
            maThanhToan);

    return rows.stream().findFirst();
  }

  public Optional<PayOsPaymentRow> lockPayOsPaymentByOrderCode(Long orderCode) {
    List<PayOsPaymentRow> rows =
        jdbcTemplate.query(
            """
                SELECT
                    ma_thanh_toan,
                    ma_hoa_don,
                    order_code,
                    payment_link_id,
                    checkout_url,
                    qr_code,
                    so_tien,
                    mo_ta,
                    trang_thai,
                    reference_code
                FROM THANHTOAN_PAYOS
                WHERE order_code = ?
                FOR UPDATE
                """,
            (rs, rowNum) -> mapPayOsPaymentRow(rs),
            orderCode);

    return rows.stream().findFirst();
  }

  public Long createPayOsPayment(
      Long maHoaDon,
      Long orderCode,
      String paymentLinkId,
      String checkoutUrl,
      String qrCode,
      BigDecimal soTien,
      String moTa,
      String trangThai) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("MA_HOA_DON", maHoaDon);
    params.put("ORDER_CODE", orderCode);
    params.put("PAYMENT_LINK_ID", paymentLinkId);
    params.put("CHECKOUT_URL", checkoutUrl);
    params.put("QR_CODE", qrCode);
    params.put("SO_TIEN", soTien);
    params.put("MO_TA", moTa);
    params.put("TRANG_THAI", trangThai);
    return payOsPaymentInsert.executeAndReturnKey(params).longValue();
  }

  public void markPayOsPaid(Long maThanhToan, String referenceCode, String rawWebhook) {
    jdbcTemplate.update(
        """
                UPDATE THANHTOAN_PAYOS
                SET trang_thai = 'PAID',
                    reference_code = ?,
                    raw_webhook = ?
                WHERE ma_thanh_toan = ?
                """,
        referenceCode,
        rawWebhook,
        maThanhToan);
  }

  public void markPayOsCancelled(Long maThanhToan, String referenceCode, String rawWebhook) {
    jdbcTemplate.update(
        """
                UPDATE THANHTOAN_PAYOS
                SET trang_thai = 'CANCELLED',
                    reference_code = ?,
                    raw_webhook = ?
                WHERE ma_thanh_toan = ?
                """,
        referenceCode,
        rawWebhook,
        maThanhToan);
  }

  public void markPayOsFailed(Long maThanhToan, String referenceCode, String rawWebhook) {
    jdbcTemplate.update(
        """
                UPDATE THANHTOAN_PAYOS
                SET trang_thai = 'FAILED',
                    reference_code = ?,
                    raw_webhook = ?
                WHERE ma_thanh_toan = ?
                """,
        referenceCode,
        rawWebhook,
        maThanhToan);
  }

  public void updatePayOsRawWebhook(Long maThanhToan, String referenceCode, String rawWebhook) {
    jdbcTemplate.update(
        """
                UPDATE THANHTOAN_PAYOS
                SET reference_code = COALESCE(?, reference_code),
                    raw_webhook = ?
                WHERE ma_thanh_toan = ?
                """,
        referenceCode,
        rawWebhook,
        maThanhToan);
  }

  public void cancelPendingPayOsPaymentForOrder(Long maHoaDon) {
    jdbcTemplate.update(
        """
                UPDATE THANHTOAN_PAYOS
                SET trang_thai = 'CANCELLED'
                WHERE ma_hoa_don = ?
                  AND trang_thai = 'PENDING'
                """,
        maHoaDon);
  }

  public void cancelOrder(Long maHoaDon) {
    jdbcTemplate.update(
        """
                UPDATE HOADON
                SET trang_thai_hoa_don = 'CANCELLED',
                    trang_thai_thanh_toan = 'FAILED'
                WHERE ma_hoa_don = ?
                """,
        maHoaDon);
  }

  private OrderRow mapOrderRow(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new OrderRow(
        rs.getLong("ma_hoa_don"),
        rs.getLong("ma_chi_nhanh"),
        rs.getString("ten_chi_nhanh"),
        rs.getLong("ma_pos"),
        rs.getLong("ma_nguoi_dung"),
        rs.getString("trang_thai_hoa_don"),
        rs.getString("trang_thai_thanh_toan"),
        rs.getString("phuong_thuc_thanh_toan"),
        rs.getBigDecimal("tong_thanh_toan"),
        toLocalDateTime(rs.getTimestamp("thoi_gian_tao_hoa_don")),
        toLocalDateTime(rs.getTimestamp("thoi_gian_thanh_toan")));
  }

  private PayOsPaymentRow mapPayOsPaymentRow(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new PayOsPaymentRow(
        rs.getLong("ma_thanh_toan"),
        rs.getLong("ma_hoa_don"),
        rs.getLong("order_code"),
        rs.getString("payment_link_id"),
        rs.getString("checkout_url"),
        rs.getString("qr_code"),
        rs.getBigDecimal("so_tien"),
        rs.getString("mo_ta"),
        rs.getString("trang_thai"),
        rs.getString("reference_code"));
  }

  private LocalDateTime toLocalDateTime(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toLocalDateTime();
  }

  public record ProductOrderRow(Long maSanPham, String tenSanPham, BigDecimal giaBanHienTai) {}

  public record OrderRow(
      Long maHoaDon,
      Long maChiNhanh,
      String tenChiNhanh,
      Long maPos,
      Long maNguoiDung,
      String trangThaiHoaDon,
      String trangThaiThanhToan,
      String phuongThucThanhToan,
      BigDecimal tongThanhToan,
      LocalDateTime thoiGianTaoHoaDon,
      LocalDateTime thoiGianThanhToan) {}

  public record OrderDetailForDeductionRow(Long maCtHoaDon, Long maSanPham, Integer soLuong) {}

  public record RecipeLineRow(Long maNguyenLieu, BigDecimal soLuongCan) {}

  public record LotRow(Long maLoHang, Long maKho, Long maNguyenLieu, BigDecimal soLuongConLai) {}

  public record PayOsPaymentRow(
      Long maThanhToan,
      Long maHoaDon,
      Long orderCode,
      String paymentLinkId,
      String checkoutUrl,
      String qrCode,
      BigDecimal soTien,
      String moTa,
      String trangThai,
      String referenceCode) {}
}
