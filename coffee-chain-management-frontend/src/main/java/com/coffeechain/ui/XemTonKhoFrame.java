package com.coffeechain.ui;

import com.coffeechain.service.InventoryApiClient;
import com.coffeechain.service.InventoryApiClient.BatchInventoryDto;
import com.coffeechain.service.InventoryApiClient.InventoryDto;
import com.coffeechain.service.InventoryApiClient.PageResponseDto;
import com.coffeechain.service.InventoryApiClient.StockSummaryDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Man hinh xem ton kho hien tai va ton theo lo. Du lieu lay tu cac API /api/inventory/stock cua
 * backend.
 */
public class XemTonKhoFrame extends JFrame {

  private static final int ROOT_W = 1360;
  private static final int ROOT_H = 780;

  private static final Color PAGE_BG = Color.decode("#F7EBD5");
  private static final Color CARD_BG = Color.WHITE;
  private static final Color PRIMARY = Color.decode("#C67C4E");
  private static final Color PRIMARY_DARK = Color.decode("#B66F43");
  private static final Color TEXT = Color.decode("#1F1F1F");
  private static final Color MUTED = Color.decode("#8B8AA5");
  private static final Color BORDER = Color.decode("#E8DFD5");
  private static final Color TABLE_HEAD = Color.decode("#F4E8DA");
  private static final Color FIELD_FILL = Color.decode("#FFFFFF");
  private static final Color OK = Color.decode("#3C8C5A");
  private static final Color WARN = Color.decode("#C67C4E");
  private static final Color DANGER = Color.decode("#BE3C2D");

  private static final int PAGE_SIZE = 100;
  private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.##");

  private final JPanel root = new JPanel(null);
  private final InventoryApiClient apiClient = new InventoryApiClient();

  private final JTextField keywordField = new JTextField();
  private final JComboBox<FilterOption> warehouseCombo = new JComboBox<>();
  private final JComboBox<FilterOption> ingredientCombo = new JComboBox<>();
  private final JComboBox<FilterOption> statusCombo = new JComboBox<>();
  private final JLabel messageLabel = new JLabel(" ");
  private final JLabel pageLabel = new JLabel("Trang 1/1");

  private final JLabel totalQtyValue = metricValue();
  private final JLabel stableStockValue = metricValue();
  private final JLabel lowStockValue = metricValue();
  private final JLabel outStockValue = metricValue();

  private final DefaultTableModel stockTableModel =
      new DefaultTableModel(
          new Object[] {
            "Kho",
            "Nguyên liệu",
            "DVT",
            "Tồn",
            "Tối thiểu",
            "Trạng thái",
            "Cập nhật",
            "maKho",
            "maNguyenLieu"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

  private final JTable stockTable = new JTable(stockTableModel);

  private final DefaultTableModel lotTableModel =
      new DefaultTableModel(
          new Object[] {
            "Mã lô", "Kho", "Nguyên liệu", "DVT", "Còn lại", "Trạng thái", "Hạn sử dụng", "Ngày tạo"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable lotTable = new JTable(lotTableModel);

  private int currentPage = 0;
  private int totalPages = 1;
  private boolean loading;
  private boolean filtersLoaded;

  public XemTonKhoFrame() {
    setTitle("Phụng Lộc - Xem tồn kho");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);

    root.setBackground(PAGE_BG);
    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    setContentPane(root);

    buildHeader();
    buildSummaryCards();
    buildFilters();
    buildTables();
    buildFooter();

    pack();
    setLocationRelativeTo(null);

    loadData(true);
  }

  private void buildHeader() {
    JLabel title = new JLabel("Xem tồn kho");
    title.setBounds(44, 26, 420, 40);
    title.setForeground(PRIMARY_DARK);
    title.setFont(UiTheme.bold(30));
    root.add(title);

    JLabel subtitle =
        new JLabel("Theo dõi tồn hiện tại, cảnh báo tồn thấp và danh sách lô theo FEFO");
    subtitle.setBounds(44, 68, 650, 24);
    subtitle.setForeground(MUTED);
    subtitle.setFont(UiTheme.regular(14));
    root.add(subtitle);

    RoundedButton backButton = primaryButton("Quay lại");
    backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    backButton.setIconTextGap(8);
    backButton.setHorizontalAlignment(SwingConstants.CENTER);
    backButton.setBounds(1204, 34, 112, 34);
    backButton.addActionListener(
        e -> {
          new KhoMenuFrame().setVisible(true);
          dispose();
        });
    root.add(backButton);
  }

  private void buildSummaryCards() {
    addMetricCard("Tổng số lượng", totalQtyValue, 44, 112);
    addMetricCard("Ổn định", stableStockValue, 373, 112);
    addMetricCard("Tồn thấp", lowStockValue, 709, 112);
    addMetricCard("Hết hàng", outStockValue, 1045, 112);
  }

  private void addMetricCard(String label, JLabel valueLabel, int x, int y) {
    RoundedCard card = new RoundedCard(18, CARD_BG);
    card.setLayout(null);
    card.setBounds(x, y, 272, 92);
    root.add(card);

    JLabel labelView = new JLabel(label);
    labelView.setBounds(22, 16, 220, 22);
    labelView.setForeground(MUTED);
    labelView.setFont(UiTheme.regular(13));
    card.add(labelView);

    valueLabel.setBounds(22, 42, 220, 32);
    card.add(valueLabel);
  }

  private void buildFilters() {
    RoundedCard card = new RoundedCard(18, CARD_BG);
    card.setLayout(null);
    card.setBounds(44, 222, 1272, 96);
    root.add(card);

    addLabel(card, "Tìm kiếm", 24, 14, 120, 20);
    styleField(keywordField);
    keywordField.setBounds(24, 40, 290, 34);
    card.add(keywordField);

    addLabel(card, "Kho", 340, 14, 120, 20);
    styleCombo(warehouseCombo);
    addCombo(card, warehouseCombo, 340, 40, 260, 34);

    addLabel(card, "Nguyên liệu", 626, 14, 120, 20);
    styleCombo(ingredientCombo);
    addCombo(card, ingredientCombo, 626, 40, 260, 34);

    addLabel(card, "Trạng thái", 912, 14, 120, 20);
    styleCombo(statusCombo);
    statusCombo.addItem(new FilterOption(null, "Tất cả"));
    statusCombo.addItem(new FilterOption("ON_DINH", "Ổn định"));
    statusCombo.addItem(new FilterOption("TON_THAP", "Tồn thấp"));
    statusCombo.addItem(new FilterOption("HET_HANG", "Hết hàng"));
    addCombo(card, statusCombo, 912, 40, 170, 34);

    RoundedButton filterButton = primaryButton("Lọc");
    filterButton.setBounds(1106, 40, 70, 34);
    filterButton.addActionListener(
        e -> {
          currentPage = 0;
          loadData(false);
        });
    card.add(filterButton);

    RoundedButton resetButton = secondaryButton("Reset");
    resetButton.setBounds(1190, 40, 62, 34);
    resetButton.addActionListener(e -> resetFilters());
    card.add(resetButton);

    warehouseCombo.addItem(new FilterOption(null, "Tất cả kho"));
    ingredientCombo.addItem(new FilterOption(null, "Tất cả nguyên liệu"));
  }

  private void buildTables() {
    JLabel stockTitle = sectionTitle("Danh sách tồn kho");
    stockTitle.setBounds(44, 338, 320, 28);
    root.add(stockTitle);

    configureTable(stockTable);
    hideModelColumn(stockTable, 8);
    hideModelColumn(stockTable, 7);
    stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    stockTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                loadLotsForSelectedStock();
              }
            });

    JScrollPane stockScroll = hiddenScroll(stockTable);
    stockScroll.setBounds(44, 372, 1272, 172);
    root.add(stockScroll);

    JLabel lotTitle = sectionTitle("Lô hàng còn tồn");
    lotTitle.setBounds(44, 560, 320, 28);
    root.add(lotTitle);

    configureTable(lotTable);
    JScrollPane lotScroll = hiddenScroll(lotTable);
    lotScroll.setBounds(44, 592, 1272, 136);
    root.add(lotScroll);
  }

  private void buildFooter() {
    messageLabel.setBounds(44, 738, 520, 24);
    messageLabel.setForeground(MUTED);
    messageLabel.setFont(UiTheme.regular(13));
    root.add(messageLabel);

    RoundedButton prevButton = secondaryButton("Trước");
    prevButton.setBounds(1030, 738, 82, 30);
    prevButton.addActionListener(
        e -> {
          if (currentPage > 0) {
            currentPage--;
            loadData(false);
          }
        });
    root.add(prevButton);

    pageLabel.setBounds(1122, 738, 86, 30);
    pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    pageLabel.setForeground(TEXT);
    pageLabel.setFont(UiTheme.regular(13));
    root.add(pageLabel);

    RoundedButton nextButton = primaryButton("Sau");
    nextButton.setBounds(1218, 738, 72, 30);
    nextButton.addActionListener(
        e -> {
          if (currentPage + 1 < totalPages) {
            currentPage++;
            loadData(false);
          }
        });
    root.add(nextButton);
  }

  private void loadData(boolean refreshFilters) {
    if (loading) {
      return;
    }

    loading = true;
    showMessage("Đang tải dữ liệu tồn kho...", false);

    Long maKho = selectedId(warehouseCombo);
    Long maNguyenLieu = selectedId(ingredientCombo);
    String tuKhoa = keywordField.getText().trim();
    String trangThai = selectedIdAsString(statusCombo);

    new SwingWorker<StockScreenData, Void>() {
      @Override
      protected StockScreenData doInBackground() throws Exception {
        PageResponseDto<InventoryDto> stock =
            apiClient.getStock(maKho, maNguyenLieu, tuKhoa, trangThai, currentPage, PAGE_SIZE);
        StockSummaryDto summary = apiClient.getStockSummary(maKho);
        List<BatchInventoryDto> lots = apiClient.getStockLots(maKho, maNguyenLieu);
        return new StockScreenData(stock, summary, lots);
      }

      @Override
      protected void done() {
        try {
          StockScreenData data = get();
          populateStockTable(data.stock().getContent());
          populateLotTable(data.lots());
          updateSummary(data.summary());
          currentPage = data.stock().getPage();
          totalPages = Math.max(1, data.stock().getTotalPages());
          pageLabel.setText("Trang " + (currentPage + 1) + "/" + totalPages);
          if (refreshFilters || !filtersLoaded) {
            populateFilters(data.stock().getContent());
            filtersLoaded = true;
          }
          showMessage("Đã tải " + data.stock().getTotalElements() + " dòng tồn kho", false);
        } catch (Exception ex) {
          showMessage("Không tải được dữ liệu: " + ex.getMessage(), true);
          JOptionPane.showMessageDialog(
              XemTonKhoFrame.this,
              "Không tải được tồn kho:\n" + ex.getMessage(),
              "Lỗi",
              JOptionPane.ERROR_MESSAGE);
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private void loadLotsForSelectedStock() {
    int viewRow = stockTable.getSelectedRow();
    if (viewRow < 0 || viewRow >= stockTable.getRowCount()) {
      return;
    }

    int row = stockTable.convertRowIndexToModel(viewRow);
    Long maKho = (Long) stockTableModel.getValueAt(row, 7);
    Long maNguyenLieu = (Long) stockTableModel.getValueAt(row, 8);

    new SwingWorker<List<BatchInventoryDto>, Void>() {
      @Override
      protected List<BatchInventoryDto> doInBackground() throws Exception {
        return apiClient.getStockLots(maKho, maNguyenLieu);
      }

      @Override
      protected void done() {
        try {
          populateLotTable(get());
        } catch (Exception ex) {
          showMessage("Không tải được lô: " + ex.getMessage(), true);
        }
      }
    }.execute();
  }

  private void populateStockTable(List<InventoryDto> rows) {
    stockTableModel.setRowCount(0);
    for (InventoryDto item : rows) {
      stockTableModel.addRow(
          new Object[] {
            item.getTenKho(),
            item.getTenNguyenLieu(),
            valueOrDash(item.getKyHieu()),
            formatNumber(item.getSoLuongTon()),
            formatNumber(item.getMucTonToiThieu()),
            stockStatusLabel(item.getTrangThaiTonKho()),
            formatDateTime(item.getLanCapNhatCuoi()),
            item.getMaKho(),
            item.getMaNguyenLieu()
          });
    }
  }

  private void populateLotTable(List<BatchInventoryDto> rows) {
    lotTableModel.setRowCount(0);
    for (BatchInventoryDto item : rows) {
      lotTableModel.addRow(
          new Object[] {
            item.getMaLoHang(),
            item.getTenKho(),
            item.getTenNguyenLieu(),
            valueOrDash(item.getKyHieu()),
            formatNumber(item.getSoLuongConLai()),
            valueOrDash(item.getTrangThai()),
            valueOrDash(item.getHanSuDung()),
            formatDateTime(item.getNgayTao())
          });
    }
  }

  private void updateSummary(StockSummaryDto summary) {
    if (summary == null) {
      totalQtyValue.setText("0");
      stableStockValue.setText("0");
      lowStockValue.setText("0");
      outStockValue.setText("0");
      return;
    }

    totalQtyValue.setText(formatNumber(summary.getTongSoLuongTon()));
    stableStockValue.setText(String.valueOf(defaultLong(summary.getSoNguyenLieuOnDinh())));
    lowStockValue.setText(String.valueOf(defaultLong(summary.getSoNguyenLieuTonThap())));
    outStockValue.setText(String.valueOf(defaultLong(summary.getSoNguyenLieuHetHang())));
  }

  private void populateFilters(List<InventoryDto> rows) {
    FilterOption selectedWarehouse = (FilterOption) warehouseCombo.getSelectedItem();
    FilterOption selectedIngredient = (FilterOption) ingredientCombo.getSelectedItem();

    Map<Long, String> warehouses = new LinkedHashMap<>();
    Map<Long, String> ingredients = new LinkedHashMap<>();

    for (InventoryDto item : rows) {
      if (item.getMaKho() != null && item.getTenKho() != null) {
        warehouses.putIfAbsent(item.getMaKho(), item.getTenKho());
      }
      if (item.getMaNguyenLieu() != null && item.getTenNguyenLieu() != null) {
        ingredients.putIfAbsent(item.getMaNguyenLieu(), item.getTenNguyenLieu());
      }
    }

    warehouseCombo.removeAllItems();
    warehouseCombo.addItem(new FilterOption(null, "Tất cả kho"));
    warehouses.forEach(
        (id, name) -> warehouseCombo.addItem(new FilterOption(String.valueOf(id), name)));
    selectOption(warehouseCombo, selectedWarehouse);

    ingredientCombo.removeAllItems();
    ingredientCombo.addItem(new FilterOption(null, "Tất cả nguyên liệu"));
    ingredients.forEach(
        (id, name) -> ingredientCombo.addItem(new FilterOption(String.valueOf(id), name)));
    selectOption(ingredientCombo, selectedIngredient);
  }

  private void resetFilters() {
    keywordField.setText("");
    warehouseCombo.setSelectedIndex(0);
    ingredientCombo.setSelectedIndex(0);
    statusCombo.setSelectedIndex(0);
    currentPage = 0;
    loadData(false);
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

    DefaultTableCellRenderer renderer =
        new DefaultTableCellRenderer() {
          @Override
          public java.awt.Component getTableCellRendererComponent(
              JTable table,
              Object value,
              boolean isSelected,
              boolean hasFocus,
              int row,
              int column) {
            java.awt.Component component =
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected && "Hết hàng".equals(value)) {
              component.setForeground(DANGER);
            } else if (!isSelected && "Tồn thấp".equals(value)) {
              component.setForeground(WARN);
            } else if (!isSelected && "Ổn định".equals(value)) {
              component.setForeground(OK);
            } else if (!isSelected) {
              component.setForeground(TEXT);
            }
            return component;
          }
        };
    table.setDefaultRenderer(Object.class, renderer);
  }

  private void hideModelColumn(JTable table, int modelIndex) {
    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
      if (table.getColumnModel().getColumn(i).getModelIndex() == modelIndex) {
        table.removeColumn(table.getColumnModel().getColumn(i));
        return;
      }
    }
  }

  private JScrollPane hiddenScroll(JTable table) {
    JScrollPane pane = new JScrollPane(table);
    pane.setBorder(BorderFactory.createLineBorder(BORDER));
    pane.setWheelScrollingEnabled(true);
    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
    pane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
    return pane;
  }

  private void styleField(JTextField field) {
    field.setFont(UiTheme.regular(14));
    field.setForeground(TEXT);
    field.setBackground(FIELD_FILL);
    field.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(0, 12, 0, 12)));
  }

  private void styleCombo(JComboBox<FilterOption> combo) {
    combo.setUI(new DesignComboBoxUI());
    combo.setRenderer(new DesignComboBoxRenderer());
    combo.setFont(UiTheme.regular(13));
    combo.setForeground(TEXT);
    combo.setBackground(FIELD_FILL);
    combo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    combo.setOpaque(false);
    combo.setFocusable(false);
    combo.setMaximumRowCount(8);
  }

  private void addCombo(JPanel parent, JComboBox<FilterOption> combo, int x, int y, int w, int h) {
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new java.awt.BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(combo, java.awt.BorderLayout.CENTER);
    parent.add(panel);
  }

  private void addLabel(JPanel parent, String text, int x, int y, int w, int h) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, w, h);
    label.setForeground(MUTED);
    label.setFont(UiTheme.regular(13));
    parent.add(label);
  }

  private JLabel sectionTitle(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(PRIMARY_DARK);
    label.setFont(UiTheme.bold(18));
    return label;
  }

  private static JLabel metricValue() {
    JLabel label = new JLabel("0");
    label.setForeground(TEXT);
    label.setFont(UiTheme.bold(25));
    return label;
  }

  private RoundedButton primaryButton(String text) {
    return new RoundedButton(text).background(PRIMARY).hover(PRIMARY_DARK).radius(10);
  }

  private RoundedButton secondaryButton(String text) {
    RoundedButton button =
        new RoundedButton(text)
            .background(Color.decode("#B9B9B9"))
            .hover(Color.decode("#A8A8A8"))
            .radius(10);
    button.setForeground(TEXT);
    return button;
  }

  private Long selectedId(JComboBox<FilterOption> combo) {
    FilterOption option = (FilterOption) combo.getSelectedItem();
    if (option == null || option.id() == null) {
      return null;
    }
    return Long.valueOf(option.id());
  }

  private String selectedIdAsString(JComboBox<FilterOption> combo) {
    FilterOption option = (FilterOption) combo.getSelectedItem();
    return option == null ? null : option.id();
  }

  private void selectOption(JComboBox<FilterOption> combo, FilterOption target) {
    if (target == null || target.id() == null) {
      combo.setSelectedIndex(0);
      return;
    }
    for (int i = 0; i < combo.getItemCount(); i++) {
      FilterOption item = combo.getItemAt(i);
      if (target.id().equals(item.id())) {
        combo.setSelectedIndex(i);
        return;
      }
    }
    combo.setSelectedIndex(0);
  }

  private String stockStatusLabel(String status) {
    if ("HET_HANG".equalsIgnoreCase(status)) {
      return "Hết hàng";
    }
    if ("TON_THAP".equalsIgnoreCase(status)) {
      return "Tồn thấp";
    }
    if ("ON_DINH".equalsIgnoreCase(status)) {
      return "Ổn định";
    }
    return valueOrDash(status);
  }

  private String formatNumber(BigDecimal value) {
    return value == null ? "0" : NUMBER_FORMAT.format(value);
  }

  private String formatDateTime(String value) {
    if (value == null || value.isBlank()) {
      return "-";
    }
    return value.length() > 19 ? value.substring(0, 19).replace('T', ' ') : value.replace('T', ' ');
  }

  private String valueOrDash(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }

  private long defaultLong(Long value) {
    return value == null ? 0L : value;
  }

  private void showMessage(String message, boolean error) {
    messageLabel.setForeground(error ? DANGER : MUTED);
    messageLabel.setText(message);
  }

  private record FilterOption(String id, String label) {

    @Override
    public String toString() {
      return label;
    }
  }

  private record StockScreenData(
      PageResponseDto<InventoryDto> stock, StockSummaryDto summary, List<BatchInventoryDto> lots) {}

  private static class RoundedCard extends JPanel {

    private final int radius;
    private final Color fill;

    RoundedCard(int radius, Color fill) {
      this.radius = radius;
      this.fill = fill;
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(fill);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
      g2.dispose();
      super.paintComponent(g);
    }
  }

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
        Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
      // Custom background handled by parent panel
    }
  }

  private static class DesignComboBoxRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof FilterOption option) {
        label.setText(option.label());
      }
      label.setFont(UiTheme.regular(13));
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
      label.setForeground(TEXT);
      label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : FIELD_FILL);
      label.setPreferredSize(new Dimension(0, 28));
      return label;
    }
  }

  private static class OutlinedInputPanel extends JPanel {

    OutlinedInputPanel() {
      setOpaque(false);
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
      g2.setColor(BORDER);
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
      g2.dispose();
    }
  }
}
