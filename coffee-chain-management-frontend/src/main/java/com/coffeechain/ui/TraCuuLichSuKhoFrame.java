package com.coffeechain.ui;

import com.coffeechain.service.InventoryHistoryApiClient;
import com.coffeechain.service.InventoryHistoryApiClient.InventoryHistoryDto;
import com.coffeechain.service.InventoryHistoryApiClient.InventoryHistoryLookupDto;
import com.coffeechain.service.InventoryHistoryApiClient.InventoryHistorySummaryDto;
import com.coffeechain.service.InventoryHistoryApiClient.OptionDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.RoundedPanel;
import com.coffeechain.ui.common.UiTheme;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TraCuuLichSuKhoFrame extends JFrame {
  private static final int ROOT_W = 1440;
  private static final int ROOT_H = 780;

  private static final Color WHITE = Color.WHITE;
  private static final Color PRIMARY = UiTheme.PRIMARY;
  private static final Color PRIMARY_DARK = UiTheme.PRIMARY_DARK;
  private static final Color TEXT = UiTheme.TEXT_DARK;
  private static final Color MUTED = UiTheme.TEXT_MUTED;
  private static final Color BORDER = Color.decode("#B9B9B9");
  private static final Color SOFT_BORDER = Color.decode("#E6E6E6");
  private static final Color TABLE_HEAD = Color.decode("#D9D9D9");
  private static final Color FIELD_FILL = Color.WHITE;
  private static final Color FIELD_BORDER = Color.decode("#B9B9B9");
  private static final Color OK = Color.decode("#3C8C5A");
  private static final Color DANGER = Color.decode("#BE3C2D");
  private static final Color WARN = Color.decode("#C67C4E");

  private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.###");

  private final JPanel root = new JPanel(null);
  private final InventoryHistoryApiClient apiClient = new InventoryHistoryApiClient();

  private final JComboBox<FilterOption> warehouseCombo = new JComboBox<>();
  private final JComboBox<FilterOption> ingredientCombo = new JComboBox<>();
  private final JComboBox<FilterOption> transactionTypeCombo = new JComboBox<>();
  private final JTextField lotField = new JTextField();
  private final JTextField fromDateField = new JTextField();
  private final JTextField toDateField = new JTextField();
  private final JTextField keywordField = new JTextField();
  private final JLabel statusLabel = new JLabel("Đang tải dữ liệu...");

  private final JLabel transactionCountValue = metricValue();
  private final JLabel importValue = metricValue();
  private final JLabel exportValue = metricValue();
  private final JLabel transferValue = metricValue();
  private final JLabel netChangeValue = metricValue();

  private final DefaultTableModel historyTableModel =
      new DefaultTableModel(
          new Object[] {
            "Mã",
            "Thời gian",
            "Kho",
            "Nguyên liệu",
            "DVT",
            "Lô",
            "Loại giao dịch",
            "Chứng từ",
            "Thay đổi",
            "Trước",
            "Sau",
            "Người thao tác"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable historyTable = new JTable(historyTableModel);

  private boolean loading;
  private boolean lookupsLoaded;

  public TraCuuLichSuKhoFrame() {
    setTitle("Phụng Lộc - Tra cứu lịch sử kho");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);

    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    root.setBackground(WHITE);
    setContentPane(root);

    buildHeader();
    buildFilterCard();
    buildSummaryCards();
    buildHistoryTable();
    buildFooter();

    pack();
    setLocationRelativeTo(null);

    loadData(true);
  }

  private void buildHeader() {
    JLabel title = new JLabel("TRA CỨU LỊCH SỬ KHO");
    title.setBounds(44, 22, 520, 40);
    title.setForeground(PRIMARY);
    title.setFont(UiTheme.bold(32));
    root.add(title);

    JLabel subtitle =
        new JLabel(
            "Theo dõi nhật ký nhập, xuất, điều chuyển, hao hụt, bán hàng và điều chỉnh kiểm kho");
    subtitle.setBounds(44, 62, 760, 24);
    subtitle.setForeground(MUTED);
    subtitle.setFont(UiTheme.regular(14));
    root.add(subtitle);

    RoundedButton backButton = primaryButton("Quay lại");
    backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    backButton.setIconTextGap(8);
    backButton.setHorizontalAlignment(SwingConstants.CENTER);
    backButton.setBounds(1285, 30, 110, 34);
    backButton.addActionListener(
        e -> {
          new KhoMenuFrame().setVisible(true);
          dispose();
        });
    root.add(backButton);
  }

  private void buildFilterCard() {
    JLabel filterTitle = new JLabel("Bộ lọc lịch sử");
    filterTitle.setBounds(44, 94, 220, 24);
    filterTitle.setForeground(TEXT);
    filterTitle.setFont(UiTheme.bold(16));
    root.add(filterTitle);

    RoundedPanel card = new RoundedPanel(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(44, 124, 1352, 140);
    root.add(card);

    addLabel(card, "Kho", 20, 12, 100, 18);
    addCombo(card, warehouseCombo, 20, 34, 240, 34);

    addLabel(card, "Nguyên liệu", 290, 12, 120, 18);
    addCombo(card, ingredientCombo, 290, 34, 240, 34);

    addLabel(card, "Loại giao dịch", 560, 12, 140, 18);
    addCombo(card, transactionTypeCombo, 560, 34, 240, 34);

    addLabel(card, "Mã lô", 830, 12, 80, 18);
    addField(card, lotField, 830, 34, 120, 34, "VD: 23");

    addLabel(card, "Từ ngày", 980, 12, 90, 18);
    addField(card, fromDateField, 980, 34, 130, 34, "yyyy-MM-dd");

    addLabel(card, "Đến ngày", 1140, 12, 90, 18);
    addField(card, toDateField, 1140, 34, 130, 34, "yyyy-MM-dd");

    addLabel(card, "Từ khóa", 20, 76, 100, 18);
    addField(card, keywordField, 20, 98, 520, 34, "Chứng từ, người thao tác, kho, nguyên liệu...");

    RoundedButton filterButton = primaryButton("Lọc");
    filterButton.setBounds(1120, 92, 82, 34);
    filterButton.addActionListener(e -> loadData(false));
    card.add(filterButton);

    RoundedButton resetButton = secondaryButton("Reset");
    resetButton.setBounds(1220, 92, 82, 34);
    resetButton.addActionListener(e -> resetFilters());
    card.add(resetButton);

    addDefaultComboItems();
  }

  private void buildSummaryCards() {
    addMetricCard("Số giao dịch", transactionCountValue, 44, 280);
    addMetricCard("Tổng nhập", importValue, 318, 280);
    addMetricCard("Tổng xuất", exportValue, 598, 280);
    addMetricCard("Điều chuyển", transferValue, 878, 280);
    addMetricCard("Biến động ròng", netChangeValue, 1158, 280);
  }

  private void addMetricCard(String label, JLabel valueLabel, int x, int y) {
    RoundedPanel card = new RoundedPanel(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(x, y, 240, 80);
    root.add(card);

    JLabel labelView = new JLabel(label);
    labelView.setBounds(20, 14, 190, 22);
    labelView.setForeground(MUTED);
    labelView.setFont(UiTheme.regular(13));
    card.add(labelView);

    valueLabel.setBounds(20, 38, 190, 30);
    card.add(valueLabel);
  }

  private void buildHistoryTable() {
    JLabel tableTitle = new JLabel("NHẬT KÝ KHO");
    tableTitle.setBounds(44, 380, 280, 24);
    tableTitle.setForeground(TEXT);
    tableTitle.setFont(UiTheme.bold(15));
    root.add(tableTitle);

    configureTable(historyTable);
    JScrollPane scrollPane = hiddenScrollPane(historyTable);
    scrollPane.setBounds(44, 408, 1352, 300);
    root.add(scrollPane);
  }

  private void buildFooter() {
    statusLabel.setBounds(44, 726, 780, 24);
    statusLabel.setForeground(MUTED);
    statusLabel.setFont(UiTheme.regular(13));
    root.add(statusLabel);
  }

  private void loadData(boolean loadLookups) {
    if (loading) {
      return;
    }

    QueryDates dates = parseDates();
    if (dates == null) {
      return;
    }

    Long maLoHang = parseOptionalLong(lotField.getText(), "Mã lô phải là số nguyên dương");
    if (maLoHang != null && maLoHang <= 0) {
      return;
    }

    loading = true;
    showMessage("Đang tải lịch sử kho...", false);

    Long maKho = selectedId(warehouseCombo);
    Long maNguyenLieu = selectedId(ingredientCombo);
    String loaiGiaoDich = selectedCode(transactionTypeCombo);
    String keyword = keywordField.getText().trim();

    new SwingWorker<HistoryScreenData, Void>() {
      @Override
      protected HistoryScreenData doInBackground() throws Exception {
        InventoryHistoryLookupDto lookups =
            loadLookups || !lookupsLoaded ? apiClient.getLookups() : null;
        List<InventoryHistoryDto> history =
            apiClient.searchHistory(
                maKho,
                maNguyenLieu,
                maLoHang,
                loaiGiaoDich,
                dates.fromDateTime(),
                dates.toDateTime(),
                keyword);
        List<InventoryHistorySummaryDto> summary =
            apiClient.getSummary(maKho, maNguyenLieu, dates.fromDateTime(), dates.toDateTime());
        return new HistoryScreenData(lookups, history, summary);
      }

      @Override
      protected void done() {
        try {
          HistoryScreenData data = get();
          if (data.lookups() != null) {
            populateLookups(data.lookups());
            lookupsLoaded = true;
          }
          populateHistoryTable(data.history());
          updateSummary(data.summary(), data.history());
          showMessage("Đã tải " + data.history().size() + " dòng nhật ký kho", false);
        } catch (Exception ex) {
          showMessage("Không tải được lịch sử kho: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              TraCuuLichSuKhoFrame.this,
              "Không tải được lịch sử kho:\n" + unwrapMessage(ex),
              "Lỗi",
              JOptionPane.ERROR_MESSAGE);
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private void populateLookups(InventoryHistoryLookupDto lookups) {
    populateCombo(warehouseCombo, "Tất cả kho", lookups.getWarehouses(), true);
    populateCombo(ingredientCombo, "Tất cả nguyên liệu", lookups.getIngredients(), true);
    populateCombo(transactionTypeCombo, "Tất cả giao dịch", lookups.getTransactionTypes(), false);
  }

  private void addDefaultComboItems() {
    warehouseCombo.addItem(new FilterOption(null, null, "Tất cả kho"));
    ingredientCombo.addItem(new FilterOption(null, null, "Tất cả nguyên liệu"));
    transactionTypeCombo.addItem(new FilterOption(null, null, "Tất cả giao dịch"));
  }

  private void populateCombo(
      JComboBox<FilterOption> combo, String allLabel, List<OptionDto> options, boolean useId) {
    FilterOption selected = (FilterOption) combo.getSelectedItem();
    combo.removeAllItems();
    combo.addItem(new FilterOption(null, null, allLabel));
    if (options != null) {
      for (OptionDto option : options) {
        combo.addItem(
            new FilterOption(
                useId ? option.getId() : null, useId ? null : option.getCode(), option.getName()));
      }
    }
    selectComboOption(combo, selected);
  }

  private void selectComboOption(JComboBox<FilterOption> combo, FilterOption selected) {
    if (selected == null || (selected.id() == null && selected.code() == null)) {
      combo.setSelectedIndex(0);
      return;
    }
    for (int i = 0; i < combo.getItemCount(); i++) {
      FilterOption option = combo.getItemAt(i);
      if (selected.id() != null && selected.id().equals(option.id())) {
        combo.setSelectedIndex(i);
        return;
      }
      if (selected.code() != null && selected.code().equals(option.code())) {
        combo.setSelectedIndex(i);
        return;
      }
    }
    combo.setSelectedIndex(0);
  }

  private void populateHistoryTable(List<InventoryHistoryDto> rows) {
    historyTableModel.setRowCount(0);
    if (rows == null) {
      return;
    }
    for (InventoryHistoryDto item : rows) {
      historyTableModel.addRow(
          new Object[] {
            item.getMaNhatKyKho(),
            formatDateTime(item.getThoiGian()),
            valueOrDash(item.getTenKho()),
            valueOrDash(item.getTenNguyenLieu()),
            valueOrDash(item.getDonViTinh()),
            item.getMaLoHang() == null ? "-" : item.getMaLoHang(),
            transactionLabel(item.getLoaiGiaoDich()),
            documentLabel(item.getTenChungTu(), item.getMaChungTu()),
            signedNumber(item.getSoLuongThayDoi()),
            formatNumber(item.getSoLuongTruoc()),
            formatNumber(item.getSoLuongSau()),
            valueOrDash(item.getTenNguoiThaoTac())
          });
    }
  }

  private void updateSummary(
      List<InventoryHistorySummaryDto> summaryRows, List<InventoryHistoryDto> historyRows) {
    BigDecimal totalImport = BigDecimal.ZERO;
    BigDecimal totalExport = BigDecimal.ZERO;
    BigDecimal totalTransfer = BigDecimal.ZERO;
    BigDecimal netChange = BigDecimal.ZERO;
    int transactionCount = historyRows == null ? 0 : historyRows.size();

    if (summaryRows != null) {
      for (InventoryHistorySummaryDto row : summaryRows) {
        totalImport = totalImport.add(defaultDecimal(row.getTongNhap()));
        totalExport =
            totalExport
                .add(defaultDecimal(row.getTongXuat()))
                .add(defaultDecimal(row.getTongHaoHut()))
                .add(defaultDecimal(row.getTongBanHangTruKho()));
        totalTransfer =
            totalTransfer
                .add(defaultDecimal(row.getTongDieuChuyenVao()))
                .add(defaultDecimal(row.getTongDieuChuyenRa()));
        netChange = netChange.add(defaultDecimal(row.getBienDongRong()));
      }
    }

    transactionCountValue.setText(String.valueOf(transactionCount));
    importValue.setText(formatNumber(totalImport));
    exportValue.setText(formatNumber(totalExport));
    transferValue.setText(formatNumber(totalTransfer));
    netChangeValue.setText(signedNumber(netChange));
    netChangeValue.setForeground(netChange.signum() < 0 ? DANGER : OK);
  }

  private void resetFilters() {
    lotField.setText("");
    fromDateField.setText("");
    toDateField.setText("");
    keywordField.setText("");
    warehouseCombo.setSelectedIndex(0);
    ingredientCombo.setSelectedIndex(0);
    transactionTypeCombo.setSelectedIndex(0);
    loadData(false);
  }

  private QueryDates parseDates() {
    LocalDate from = parseOptionalDate(fromDateField.getText(), "Từ ngày");
    if (from == LocalDate.MIN) {
      return null;
    }
    LocalDate to = parseOptionalDate(toDateField.getText(), "Đến ngày");
    if (to == LocalDate.MIN) {
      return null;
    }
    if (from != null && to != null && from.isAfter(to)) {
      JOptionPane.showMessageDialog(
          this, "Từ ngày không được lớn hơn đến ngày", "Lịch sử kho", JOptionPane.WARNING_MESSAGE);
      return null;
    }
    return new QueryDates(
        from == null ? null : from + "T00:00:00", to == null ? null : to + "T23:59:59");
  }

  private LocalDate parseOptionalDate(String raw, String label) {
    if (raw == null || raw.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDate.parse(raw.trim());
    } catch (DateTimeParseException ex) {
      JOptionPane.showMessageDialog(
          this, label + " phải có dạng yyyy-MM-dd", "Lịch sử kho", JOptionPane.WARNING_MESSAGE);
      return LocalDate.MIN;
    }
  }

  private Long parseOptionalLong(String raw, String message) {
    if (raw == null || raw.trim().isEmpty()) {
      return null;
    }
    try {
      long value = Long.parseLong(raw.trim());
      if (value <= 0) {
        JOptionPane.showMessageDialog(this, message, "Lịch sử kho", JOptionPane.WARNING_MESSAGE);
        return 0L;
      }
      return value;
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, message, "Lịch sử kho", JOptionPane.WARNING_MESSAGE);
      return 0L;
    }
  }

  private void configureTable(JTable table) {
    table.setRowHeight(34);
    table.setFont(UiTheme.regular(13));
    table.getTableHeader().setFont(UiTheme.bold(13));
    table.getTableHeader().setBackground(TABLE_HEAD);
    table.getTableHeader().setForeground(TEXT);
    table.getTableHeader().setPreferredSize(new Dimension(0, 34));
    table.setSelectionBackground(Color.decode("#F8DCC6"));
    table.setSelectionForeground(TEXT);
    table.setGridColor(BORDER);
    table.setShowGrid(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    int[] widths = {70, 150, 210, 190, 70, 70, 150, 140, 110, 100, 100, 150};
    for (int i = 0; i < widths.length; i++) {
      table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    table.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable table,
              Object value,
              boolean isSelected,
              boolean hasFocus,
              int row,
              int column) {
            Component component =
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected) {
              component.setForeground(TEXT);
              if (column == 6) {
                component.setForeground(transactionColor(String.valueOf(value)));
              }
              if (column == 8) {
                component.setForeground(String.valueOf(value).startsWith("-") ? DANGER : OK);
              }
            }
            return component;
          }
        });
  }

  private JScrollPane hiddenScrollPane(JTable table) {
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(BorderFactory.createLineBorder(SOFT_BORDER));
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
    scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
    scrollPane.getVerticalScrollBar().setUnitIncrement(34);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(34);
    return scrollPane;
  }

  private void addCombo(JPanel parent, JComboBox<FilterOption> combo, int x, int y, int w, int h) {
    styleCombo(combo);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(combo, BorderLayout.CENTER);
    parent.add(panel);
  }

  private void addField(
      JPanel parent, JTextField field, int x, int y, int w, int h, String tooltip) {
    styleField(field, tooltip);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(field, BorderLayout.CENTER);
    parent.add(panel);
  }

  private void addLabel(JPanel parent, String text, int x, int y, int w, int h) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, w, h);
    label.setForeground(MUTED);
    label.setFont(UiTheme.regular(13));
    parent.add(label);
  }

  private void styleField(JTextField field, String tooltip) {
    field.setFont(UiTheme.regular(14));
    field.setForeground(TEXT);
    field.setToolTipText(tooltip);
    field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    field.setOpaque(false);
  }

  private void styleCombo(JComboBox<FilterOption> combo) {
    combo.setUI(new DesignComboBoxUI());
    combo.setRenderer(new DesignComboBoxRenderer());
    combo.setFont(UiTheme.regular(14));
    combo.setBackground(WHITE);
    combo.setForeground(TEXT);
    combo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
    combo.setOpaque(false);
    combo.setFocusable(false);
    combo.setMaximumRowCount(8);
  }

  private static RoundedButton primaryButton(String text) {
    return new RoundedButton(text).background(PRIMARY).hover(PRIMARY_DARK).radius(10);
  }

  private static RoundedButton secondaryButton(String text) {
    RoundedButton button =
        new RoundedButton(text)
            .background(Color.decode("#B9B9B9"))
            .hover(Color.decode("#A8A8A8"))
            .radius(10);
    button.setForeground(TEXT);
    return button;
  }

  private static JLabel metricValue() {
    JLabel label = new JLabel("0");
    label.setForeground(TEXT);
    label.setFont(UiTheme.bold(22));
    return label;
  }

  private Long selectedId(JComboBox<FilterOption> combo) {
    FilterOption option = (FilterOption) combo.getSelectedItem();
    return option == null ? null : option.id();
  }

  private String selectedCode(JComboBox<FilterOption> combo) {
    FilterOption option = (FilterOption) combo.getSelectedItem();
    return option == null ? null : option.code();
  }

  private String transactionLabel(String code) {
    if ("IMPORT".equalsIgnoreCase(code)) return "Nhập kho";
    if ("EXPORT".equalsIgnoreCase(code)) return "Xuất kho";
    if ("TRANSFER_IN".equalsIgnoreCase(code)) return "Điều chuyển vào";
    if ("TRANSFER_OUT".equalsIgnoreCase(code)) return "Điều chuyển ra";
    if ("WASTAGE".equalsIgnoreCase(code)) return "Hao hụt";
    if ("SALE_DEDUCT".equalsIgnoreCase(code)) return "Bán hàng trừ kho";
    if ("SALE_REVERSE".equalsIgnoreCase(code)) return "Hoàn trừ kho";
    if ("STOCKTAKE_ADJUST".equalsIgnoreCase(code)) return "Điều chỉnh kiểm kho";
    return valueOrDash(code);
  }

  private Color transactionColor(String label) {
    if (label.contains("Nhập") || label.contains("vào") || label.contains("Hoàn")) return OK;
    if (label.contains("Xuất")
        || label.contains("ra")
        || label.contains("Hao")
        || label.contains("Bán")) return DANGER;
    if (label.contains("Điều chỉnh")) return WARN;
    return TEXT;
  }

  private String documentLabel(String name, Long id) {
    if ((name == null || name.isBlank()) && id == null) return "-";
    if (id == null) return name;
    if (name == null || name.isBlank()) return String.valueOf(id);
    return name + " #" + id;
  }

  private String formatDateTime(String value) {
    if (value == null || value.isBlank()) return "-";
    return value.length() > 19 ? value.substring(0, 19).replace('T', ' ') : value.replace('T', ' ');
  }

  private String signedNumber(BigDecimal value) {
    if (value == null) return "0";
    String formatted = NUMBER_FORMAT.format(value);
    return value.signum() > 0 ? "+" + formatted : formatted;
  }

  private String formatNumber(BigDecimal value) {
    return value == null ? "0" : NUMBER_FORMAT.format(value);
  }

  private BigDecimal defaultDecimal(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String valueOrDash(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }

  private void showMessage(String message, boolean error) {
    statusLabel.setForeground(error ? DANGER : MUTED);
    statusLabel.setText(message);
  }

  private String unwrapMessage(Exception ex) {
    Throwable current = ex;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return current.getMessage() == null ? "Không xử lý được yêu cầu" : current.getMessage();
  }

  private record FilterOption(Long id, String code, String label) {
    @Override
    public String toString() {
      return label == null ? "" : label;
    }
  }

  private record QueryDates(String fromDateTime, String toDateTime) {}

  private record HistoryScreenData(
      InventoryHistoryLookupDto lookups,
      List<InventoryHistoryDto> history,
      List<InventoryHistorySummaryDto> summary) {}

  private static class DesignComboBoxUI extends BasicComboBoxUI {
    @Override
    public void installUI(JComponent c) {
      super.installUI(c);
      c.setOpaque(false);
    }

    @Override
    protected JButton createArrowButton() {
      JButton button =
          new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
              Graphics2D g2 = (Graphics2D) g.create();
              g2.setRenderingHint(
                  RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
              g2.setColor(TEXT);
              int cx = getWidth() / 2;
              int cy = getHeight() / 2 + 1;
              g2.drawLine(cx - 5, cy - 3, cx, cy + 2);
              g2.drawLine(cx, cy + 2, cx + 5, cy - 3);
              g2.dispose();
            }
          };
      button.setBorder(BorderFactory.createEmptyBorder());
      button.setContentAreaFilled(false);
      button.setFocusPainted(false);
      button.setOpaque(false);
      button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      return button;
    }

    @Override
    public void paintCurrentValueBackground(
        Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {}
  }

  private static class DesignComboBoxRenderer extends javax.swing.DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof FilterOption option) {
        label.setText(option.label());
      }
      label.setFont(UiTheme.regular(14));
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
      label.setForeground(TEXT);
      label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : WHITE);
      return label;
    }
  }

  private static class OutlinedInputPanel extends JPanel {
    OutlinedInputPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(FIELD_FILL);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
      g2.dispose();
      super.paintComponent(g);
    }

    @Override
    protected void paintChildren(Graphics g) {
      super.paintChildren(g);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(FIELD_BORDER);
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
      g2.dispose();
    }
  }
}
