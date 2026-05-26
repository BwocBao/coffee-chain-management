package com.coffeechain.ui;

import com.coffeechain.service.ExpiryApiClient;
import com.coffeechain.service.ExpiryApiClient.ExpiryLookupDto;
import com.coffeechain.service.ExpiryApiClient.ExpiryLotDto;
import com.coffeechain.service.ExpiryApiClient.ExpiryRefreshDto;
import com.coffeechain.service.ExpiryApiClient.ExpiryStatisticsDto;
import com.coffeechain.service.ExpiryApiClient.OptionDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.RoundedButton;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

public class TheoDoiHanSuDungFrame extends JFrame {
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
  private static final Color FIELD_FILL = Color.WHITE;
  private static final Color OK = Color.decode("#3C8C5A");
  private static final Color WARN = Color.decode("#C67C4E");
  private static final Color DANGER = Color.decode("#BE3C2D");
  private static final Color NEUTRAL = Color.decode("#6F6E7C");

  private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.##");

  private final JPanel root = new JPanel(null);
  private final ExpiryApiClient apiClient = new ExpiryApiClient();

  private final JTextField keywordField = new JTextField();
  private final JTextField warningDaysField = new JTextField("30");
  private final JCheckBox onlyAvailableCheck = new JCheckBox("Chỉ lô còn tồn");

  private final JComboBox<FilterOption> warehouseCombo = new JComboBox<>();
  private final JComboBox<FilterOption> ingredientCombo = new JComboBox<>();
  private final JComboBox<FilterOption> warningCombo = new JComboBox<>();

  private final JLabel totalLotsValue = metricValue();
  private final JLabel activeLotsValue = metricValue();
  private final JLabel expiringLotsValue = metricValue();
  private final JLabel expiredLotsValue = metricValue();
  private final JLabel messageLabel = new JLabel(" ");

  private final DefaultTableModel lotTableModel =
      new DefaultTableModel(
          new Object[] {
            "Mã lô", "Kho", "Nguyên liệu", "DVT", "Còn lại", "Hạn sử dụng", "Còn ngày", "Cảnh báo"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable lotTable = new JTable(lotTableModel);

  private List<ExpiryLotDto> loadedLots = new ArrayList<>();
  private boolean loading;

  public TheoDoiHanSuDungFrame() {
    setTitle("Phụng Lộc - Theo dõi hạn sử dụng");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);

    root.setBackground(PAGE_BG);
    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    setContentPane(root);

    buildHeader();
    buildSummaryCards();
    buildFilters();
    buildTable();
    buildFooter();

    pack();
    setLocationRelativeTo(null);

    loadData(true);
  }

  private void buildHeader() {
    JLabel title = new JLabel("Theo dõi hạn sử dụng");
    title.setBounds(44, 26, 520, 40);
    title.setForeground(PRIMARY_DARK);
    title.setFont(UiTheme.bold(30));
    root.add(title);

    JLabel subtitle =
        new JLabel("Theo dõi lô nguyên liệu theo FEFO, cảnh báo sắp hết hạn và rà soát lô quá hạn");
    subtitle.setBounds(44, 68, 760, 24);
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
    addMetricCard("Tổng số lô", totalLotsValue, 44, 112);
    addMetricCard("Đang hoạt động", activeLotsValue, 381, 112);
    addMetricCard("Sắp hết hạn", expiringLotsValue, 717, 112);
    addMetricCard("Đã hết hạn", expiredLotsValue, 1053, 112);
  }

  private void addMetricCard(String label, JLabel valueLabel, int x, int y) {
    RoundedCard card = new RoundedCard(18, CARD_BG);
    card.setLayout(null);
    card.setBounds(x, y, 260, 92);
    root.add(card);

    JLabel labelView = new JLabel(label);
    labelView.setBounds(20, 16, 170, 22);
    labelView.setForeground(MUTED);
    labelView.setFont(UiTheme.regular(13));
    card.add(labelView);

    valueLabel.setBounds(20, 42, 170, 32);
    card.add(valueLabel);
  }

  private void buildFilters() {
    RoundedCard card = new RoundedCard(18, CARD_BG);
    card.setLayout(null);
    card.setBounds(44, 222, 1272, 104);
    root.add(card);

    addLabel(card, "Tìm kiếm", 24, 12, 120, 20);
    addFieldPanel(card, keywordField, 24, 38, 260, 34);

    addLabel(card, "Kho", 310, 12, 120, 20);
    addCombo(card, warehouseCombo, 310, 38, 250, 34);

    addLabel(card, "Nguyên liệu", 586, 12, 120, 20);
    addCombo(card, ingredientCombo, 586, 38, 250, 34);

    addLabel(card, "Ngưỡng cảnh báo", 862, 12, 140, 20);
    addFieldPanel(card, warningDaysField, 862, 38, 96, 34);

    onlyAvailableCheck.setBounds(862, 74, 160, 24);
    onlyAvailableCheck.setOpaque(false);
    onlyAvailableCheck.setFocusPainted(false);
    onlyAvailableCheck.setSelected(true);
    onlyAvailableCheck.setForeground(TEXT);
    onlyAvailableCheck.setFont(UiTheme.regular(13));
    card.add(onlyAvailableCheck);

    addLabel(card, "Hạn sử dụng", 994, 12, 120, 20);
    addCombo(card, warningCombo, 994, 38, 160, 34);

    RoundedButton filterButton = primaryButton("Lọc");
    filterButton.setBounds(1176, 38, 72, 34);
    filterButton.addActionListener(e -> loadData(false));
    card.add(filterButton);

    RoundedButton resetButton = secondaryButton("Reset");
    resetButton.setBounds(1176, 76, 72, 24);
    resetButton.addActionListener(e -> resetFilters());
    card.add(resetButton);

    addDefaultComboItems();
  }

  private void buildTable() {
    JLabel tableTitle = sectionTitle("Danh sách lô theo hạn sử dụng");
    tableTitle.setBounds(44, 352, 420, 28);
    root.add(tableTitle);

    configureTable(lotTable);
    JScrollPane scrollPane = hiddenScroll(lotTable);
    scrollPane.setBounds(44, 386, 1272, 334);
    root.add(scrollPane);
  }

  private void buildFooter() {
    messageLabel.setBounds(44, 734, 900, 24);
    messageLabel.setForeground(MUTED);
    messageLabel.setFont(UiTheme.regular(13));
    root.add(messageLabel);
  }

  private void loadData(boolean loadLookups) {
    if (loading) {
      return;
    }

    Integer warningDays = parseInteger(warningDaysField.getText(), "Ngưỡng cảnh báo");
    if (warningDays == null) {
      return;
    }

    loading = true;
    showMessage("Đang tải dữ liệu hạn sử dụng...", false);

    Long maKho = selectedId(warehouseCombo);
    Long maNguyenLieu = selectedId(ingredientCombo);
    String trangThai = null;
    String mucCanhBao = selectedCode(warningCombo);
    Boolean onlyAvailable = onlyAvailableCheck.isSelected();

    new SwingWorker<ExpiryScreenData, Void>() {
      @Override
      protected ExpiryScreenData doInBackground() throws Exception {
        ExpiryLookupDto lookups = loadLookups ? apiClient.getLookups() : null;
        List<ExpiryLotDto> lots =
            apiClient.searchLots(
                maKho, maNguyenLieu, trangThai, mucCanhBao, null, onlyAvailable, warningDays);
        ExpiryStatisticsDto statistics = apiClient.getStatistics(maKho, warningDays);
        return new ExpiryScreenData(lookups, lots, statistics);
      }

      @Override
      protected void done() {
        try {
          ExpiryScreenData data = get();
          if (data.lookups() != null) {
            populateLookups(data.lookups());
          }
          loadedLots = data.lots() == null ? new ArrayList<>() : data.lots();
          populateLotTable(filterByKeyword(loadedLots));
          updateSummary(data.statistics());
          showMessage("Đã tải " + lotTableModel.getRowCount() + " lô theo hạn sử dụng", false);
        } catch (Exception ex) {
          showMessage("Không tải được dữ liệu: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              TheoDoiHanSuDungFrame.this,
              "Không tải được dữ liệu hạn sử dụng:\n" + unwrapMessage(ex),
              "Lỗi",
              JOptionPane.ERROR_MESSAGE);
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private void refreshExpiredLots() {
    if (loading) {
      return;
    }

    loading = true;
    showMessage("Đang rà soát hạn sử dụng...", false);

    new SwingWorker<ExpiryRefreshDto, Void>() {
      @Override
      protected ExpiryRefreshDto doInBackground() throws Exception {
        return apiClient.refreshExpiredLots();
      }

      @Override
      protected void done() {
        try {
          ExpiryRefreshDto response = get();
          showMessage(valueOrDefault(response.getMessage(), "Đã rà soát hạn sử dụng"), false);
          loading = false;
          loadData(false);
          return;
        } catch (Exception ex) {
          showMessage("Không rà soát được HSD: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              TheoDoiHanSuDungFrame.this,
              "Không rà soát được hạn sử dụng:\n" + unwrapMessage(ex),
              "Lỗi",
              JOptionPane.ERROR_MESSAGE);
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private void populateLookups(ExpiryLookupDto lookups) {
    populateCombo(warehouseCombo, "Tất cả kho", lookups.getWarehouses(), true);
    populateCombo(ingredientCombo, "Tất cả nguyên liệu", lookups.getIngredients(), true);
    populateCombo(warningCombo, "Tất cả HSD", lookups.getWarningLevels(), false);
  }

  private void addDefaultComboItems() {
    warehouseCombo.addItem(new FilterOption(null, null, "Tất cả kho"));
    ingredientCombo.addItem(new FilterOption(null, null, "Tất cả nguyên liệu"));
    warningCombo.addItem(new FilterOption(null, null, "Tất cả HSD"));
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

  private void populateLotTable(List<ExpiryLotDto> rows) {
    lotTableModel.setRowCount(0);
    for (ExpiryLotDto item : rows) {
      lotTableModel.addRow(
          new Object[] {
            item.getMaLoHang(),
            valueOrDash(item.getTenKho()),
            valueOrDash(item.getTenNguyenLieu()),
            valueOrDash(item.getDonViTinh()),
            formatNumber(item.getSoLuongConLai()),
            valueOrDash(item.getHanSuDung()),
            daysLeftLabel(item.getSoNgayConLai(), item.getMucCanhBao()),
            warningLabel(item.getMucCanhBao())
          });
    }
  }

  private List<ExpiryLotDto> filterByKeyword(List<ExpiryLotDto> rows) {
    String keyword = keywordField.getText().trim().toLowerCase(Locale.ROOT);
    if (keyword.isBlank()) {
      return rows;
    }

    List<ExpiryLotDto> filtered = new ArrayList<>();
    for (ExpiryLotDto item : rows) {
      String haystack =
          (String.valueOf(item.getMaLoHang())
                  + " "
                  + valueOrDash(item.getTenKho())
                  + " "
                  + valueOrDash(item.getTenNguyenLieu())
                  + " "
                  + lotStatusLabel(item.getTrangThai())
                  + " "
                  + warningLabel(item.getMucCanhBao()))
              .toLowerCase(Locale.ROOT);
      if (haystack.contains(keyword)) {
        filtered.add(item);
      }
    }
    return filtered;
  }

  private void updateSummary(ExpiryStatisticsDto statistics) {
    if (statistics == null) {
      totalLotsValue.setText("0");
      activeLotsValue.setText("0");
      expiringLotsValue.setText("0");
      expiredLotsValue.setText("0");
      return;
    }
    totalLotsValue.setText(String.valueOf(defaultInt(statistics.getTongSoLo())));
    activeLotsValue.setText(String.valueOf(defaultInt(statistics.getSoLoDangHoatDong())));
    expiringLotsValue.setText(String.valueOf(defaultInt(statistics.getSoLoSapHetHan())));
    expiredLotsValue.setText(String.valueOf(defaultInt(statistics.getSoLoDaHetHan())));
  }

  private void resetFilters() {
    keywordField.setText("");
    warningDaysField.setText("30");
    onlyAvailableCheck.setSelected(true);
    warehouseCombo.setSelectedIndex(0);
    ingredientCombo.setSelectedIndex(0);
    warningCombo.setSelectedIndex(0);
    loadData(false);
  }

  private void configureTable(JTable table) {
    table.setRowHeight(36);
    table.setFont(UiTheme.regular(13));
    table.getTableHeader().setFont(UiTheme.bold(13));
    table.getTableHeader().setBackground(TABLE_HEAD);
    table.getTableHeader().setForeground(TEXT);
    table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    table.setSelectionBackground(Color.decode("#F8DCC6"));
    table.setSelectionForeground(TEXT);
    table.setGridColor(BORDER);
    table.setShowGrid(true);

    DefaultTableCellRenderer renderer =
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
              if (column == 6 || column == 7) {
                component.setForeground(warningColor(String.valueOf(value)));
              }
            }
            return component;
          }
        };
    table.setDefaultRenderer(Object.class, renderer);
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

  private void addFieldPanel(JPanel parent, JTextField field, int x, int y, int w, int h) {
    styleField(field);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(field, BorderLayout.CENTER);
    parent.add(panel);
  }

  private void addCombo(JPanel parent, JComboBox<FilterOption> combo, int x, int y, int w, int h) {
    styleCombo(combo);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(combo, BorderLayout.CENTER);
    parent.add(panel);
  }

  private void styleField(JTextField field) {
    field.setFont(UiTheme.regular(14));
    field.setForeground(TEXT);
    field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    field.setOpaque(false);
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
    return option == null ? null : option.id();
  }

  private String selectedCode(JComboBox<FilterOption> combo) {
    FilterOption option = (FilterOption) combo.getSelectedItem();
    return option == null ? null : option.code();
  }

  private Integer parseInteger(String raw, String label) {
    try {
      int value = Integer.parseInt(raw.trim());
      if (value < 0) {
        throw new NumberFormatException();
      }
      return value;
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(
          this,
          label + " phải là số nguyên không âm",
          "Dữ liệu chưa hợp lệ",
          JOptionPane.WARNING_MESSAGE);
      return null;
    }
  }

  private Integer parseOptionalInteger(String raw, String label) {
    if (raw == null || raw.trim().isEmpty()) {
      return null;
    }
    Integer value = parseInteger(raw, label);
    return value == null ? Integer.MIN_VALUE : value;
  }

  private String daysLeftLabel(Integer days, String warningCode) {
    if ("KHONG_CO_HSD".equalsIgnoreCase(warningCode)) {
      return "Không HSD";
    }
    if (days == null) {
      return "-";
    }
    if (days < 0) {
      return "Quá " + Math.abs(days) + " ngày";
    }
    if (days == 0) {
      return "Hết hôm nay";
    }
    return days + " ngày";
  }

  private String lotStatusLabel(String status) {
    if ("ACTIVE".equalsIgnoreCase(status)) {
      return "Đang hoạt động";
    }
    if ("EXPIRED".equalsIgnoreCase(status)) {
      return "Đã hết hạn";
    }
    if ("USED_UP".equalsIgnoreCase(status)) {
      return "Đã dùng hết";
    }
    return valueOrDash(status);
  }

  private String warningLabel(String warning) {
    if ("DA_HET_HAN".equalsIgnoreCase(warning)) {
      return "Đã hết hạn";
    }
    if ("SAP_HET_HAN".equalsIgnoreCase(warning)) {
      return "Sắp hết hạn";
    }
    if ("BINH_THUONG".equalsIgnoreCase(warning)) {
      return "Bình thường";
    }
    if ("KHONG_CO_HSD".equalsIgnoreCase(warning)) {
      return "Không có HSD";
    }
    if ("HET_HANG".equalsIgnoreCase(warning)) {
      return "Hết hàng";
    }
    return valueOrDash(warning);
  }

  private Color warningColor(String label) {
    if (label.contains("Đã hết hạn") || label.startsWith("Quá ")) {
      return DANGER;
    }
    if (label.contains("Sắp hết hạn") || label.contains("Hết hôm nay")) {
      return WARN;
    }
    if (label.contains("Bình thường")) {
      return OK;
    }
    return NEUTRAL;
  }

  private Color statusColor(String label) {
    if (label.contains("hết hạn")) {
      return DANGER;
    }
    if (label.contains("dùng hết")) {
      return NEUTRAL;
    }
    if (label.contains("hoạt động")) {
      return OK;
    }
    return TEXT;
  }

  private String formatNumber(BigDecimal value) {
    return value == null ? "0" : NUMBER_FORMAT.format(value);
  }

  private int defaultInt(Integer value) {
    return value == null ? 0 : value;
  }

  private String valueOrDash(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }

  private String valueOrDefault(String value, String defaultValue) {
    return value == null || value.isBlank() ? defaultValue : value;
  }

  private String unwrapMessage(Exception ex) {
    Throwable cause = ex;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause.getMessage() == null ? ex.getMessage() : cause.getMessage();
  }

  private void showMessage(String message, boolean error) {
    messageLabel.setForeground(error ? DANGER : MUTED);
    messageLabel.setText(message);
  }

  private record FilterOption(Long id, String code, String label) {
    @Override
    public String toString() {
      return label == null ? "" : label;
    }
  }

  private record ExpiryScreenData(
      ExpiryLookupDto lookups, List<ExpiryLotDto> lots, ExpiryStatisticsDto statistics) {}

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
      label.setFont(UiTheme.regular(13));
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
      label.setForeground(TEXT);
      label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : FIELD_FILL);
      label.setPreferredSize(new Dimension(0, 28));
      return label;
    }
  }
}
