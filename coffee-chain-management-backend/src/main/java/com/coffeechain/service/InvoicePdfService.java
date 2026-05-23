package com.coffeechain.service;

import com.coffeechain.exception.AppException;
import com.coffeechain.repository.InvoicePdfRepository;
import com.coffeechain.repository.InvoicePdfRepository.InvoiceHeaderRow;
import com.coffeechain.repository.InvoicePdfRepository.InvoiceItemRow;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class InvoicePdfService {
  private final InvoicePdfRepository invoicePdfRepository;

  @Value("classpath:fonts/DejaVuSans.ttf")
  private Resource vietnameseFontResource;

  private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

  private final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  public InvoicePdfService(InvoicePdfRepository invoicePdfRepository) {
    this.invoicePdfRepository = invoicePdfRepository;
  }

  public byte[] generateInvoicePdf(Long maHoaDon) {
    InvoiceHeaderRow header =
        invoicePdfRepository
            .findInvoiceHeader(maHoaDon)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));

    List<InvoiceItemRow> items = invoicePdfRepository.findInvoiceItems(maHoaDon);

    if (items.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "Hóa đơn không có sản phẩm");
    }

    if (!"COMPLETED".equals(header.trangThaiHoaDon())
        || !"SUCCESS".equals(header.trangThaiThanhToan())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "Chỉ in hóa đơn sau khi thanh toán thành công");
    }

    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      Document document = new Document(PageSize.A6, 18, 18, 18, 18);
      PdfWriter.getInstance(document, out);

      document.open();

      BaseFont baseFont = loadVietnameseBaseFont();

      Font titleFont = new Font(baseFont, 15, Font.BOLD);
      Font boldFont = new Font(baseFont, 9, Font.BOLD);
      Font normalFont = new Font(baseFont, 8, Font.NORMAL);
      Font smallFont = new Font(baseFont, 7, Font.NORMAL);

      addCentered(document, "PHỤNG LỘC COFFEE", titleFont);
      addCentered(document, "HÓA ĐƠN THANH TOÁN", boldFont);
      addEmptyLine(document, 6);

      addInfoLine(document, "Chi nhánh", safe(header.tenChiNhanh()), normalFont);
      addInfoLine(document, "Địa chỉ", safe(header.diaChi()), normalFont);
      addInfoLine(document, "Mã hóa đơn", "#" + header.maHoaDon(), normalFont);
      addInfoLine(document, "Máy POS", String.valueOf(header.maPos()), normalFont);
      addInfoLine(document, "Thu ngân", safe(header.tenDangNhap()), normalFont);
      addInfoLine(document, "Tạo lúc", formatDateTime(header.thoiGianTaoHoaDon()), normalFont);
      addInfoLine(document, "Thanh toán", formatDateTime(header.thoiGianThanhToan()), normalFont);
      addInfoLine(
          document, "Phương thức", formatPaymentMethod(header.phuongThucThanhToan()), normalFont);

      addEmptyLine(document, 8);

      PdfPTable table = new PdfPTable(4);
      table.setWidthPercentage(100);
      table.setWidths(new float[] {4.2f, 1.1f, 2.2f, 2.5f});

      addHeaderCell(table, "Sản phẩm", boldFont);
      addHeaderCell(table, "SL", boldFont);
      addHeaderCell(table, "Đơn giá", boldFont);
      addHeaderCell(table, "Thành tiền", boldFont);

      for (InvoiceItemRow item : items) {
        addBodyCell(table, item.tenSanPham(), normalFont, Element.ALIGN_LEFT);
        addBodyCell(table, String.valueOf(item.soLuong()), normalFont, Element.ALIGN_CENTER);
        addBodyCell(table, formatMoney(item.donGiaBan()), normalFont, Element.ALIGN_RIGHT);
        addBodyCell(table, formatMoney(item.thanhTienDong()), normalFont, Element.ALIGN_RIGHT);
      }

      document.add(table);

      addEmptyLine(document, 8);

      PdfPTable totalTable = new PdfPTable(2);
      totalTable.setWidthPercentage(100);
      totalTable.setWidths(new float[] {5f, 5f});

      PdfPCell totalLabel = new PdfPCell(new Phrase("TỔNG THANH TOÁN", boldFont));
      totalLabel.setBorder(Rectangle.NO_BORDER);
      totalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
      totalLabel.setPadding(4);

      PdfPCell totalValue = new PdfPCell(new Phrase(formatMoney(header.tongThanhToan()), boldFont));
      totalValue.setBorder(Rectangle.NO_BORDER);
      totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
      totalValue.setPadding(4);

      totalTable.addCell(totalLabel);
      totalTable.addCell(totalValue);

      document.add(totalTable);

      addEmptyLine(document, 10);
      addCentered(document, "Cảm ơn quý khách!", normalFont);
      addCentered(document, "Hẹn gặp lại.", smallFont);

      document.close();

      return out.toByteArray();
    } catch (AppException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Không tạo được PDF hóa đơn: " + ex.getMessage());
    }
  }

  private BaseFont loadVietnameseBaseFont() {
    try {
      byte[] fontBytes = vietnameseFontResource.getInputStream().readAllBytes();

      return BaseFont.createFont(
          "DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
    } catch (Exception ex) {
      try {
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
      } catch (Exception e) {
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Không load được font PDF");
      }
    }
  }

  private void addCentered(Document document, String text, Font font) throws DocumentException {
    Paragraph paragraph = new Paragraph(text, font);
    paragraph.setAlignment(Element.ALIGN_CENTER);
    paragraph.setSpacingAfter(2);
    document.add(paragraph);
  }

  private void addInfoLine(Document document, String label, String value, Font font)
      throws DocumentException {
    Paragraph paragraph = new Paragraph(label + ": " + value, font);
    paragraph.setSpacingAfter(1);
    document.add(paragraph);
  }

  private void addEmptyLine(Document document, float spacing) throws DocumentException {
    Paragraph paragraph = new Paragraph(" ");
    paragraph.setSpacingAfter(spacing);
    document.add(paragraph);
  }

  private void addHeaderCell(PdfPTable table, String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(4);
    table.addCell(cell);
  }

  private void addBodyCell(PdfPTable table, String text, Font font, int alignment) {
    PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
    cell.setHorizontalAlignment(alignment);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(4);
    table.addCell(cell);
  }

  private String formatMoney(BigDecimal value) {
    if (value == null) {
      return "0 ₫";
    }

    return moneyFormat.format(value);
  }

  private String formatDateTime(java.time.LocalDateTime value) {
    if (value == null) {
      return "";
    }

    return value.format(dateTimeFormatter);
  }

  private String formatPaymentMethod(String method) {
    if (method == null) {
      return "";
    }

    return switch (method) {
      case "CASH" -> "Tiền mặt";
      case "BANK_TRANSFER" -> "Chuyển khoản";
      case "EWALLET" -> "Ví điện tử";
      default -> method;
    };
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }
}
